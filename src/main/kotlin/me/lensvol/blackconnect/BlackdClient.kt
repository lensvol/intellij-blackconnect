package me.lensvol.blackconnect

import com.intellij.openapi.diagnostic.Logger
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.SSLException

sealed class BlackdResponse {
    object NoChangesMade : BlackdResponse()
    data class SyntaxError(val reason: String) : BlackdResponse()
    data class InternalError(val reason: String) : BlackdResponse()
    data class InvalidRequest(val reason: String) : BlackdResponse()
    data class Blackened(val sourceCode: String) : BlackdResponse()
    data class UnknownStatus(val code: Int, val responseText: String) : BlackdResponse()
}

class BlackdClient(hostname: String, port: Int, useSsl: Boolean = false) {
    private val logger = Logger.getInstance(BlackdClient::class.java.name)
    private val protocol = if (useSsl) "https" else "http"
    val blackdUrl = URL("$protocol://$hostname:$port")

    fun checkConnection(): Result<String, String> {
        with(blackdUrl.openConnection() as HttpURLConnection) {
            requestMethod = "POST"

            // Sometimes SSL negotiation can hang and we need to handle this by bailing quickly
            connectTimeout = Constants.DEFAULT_CONNECTION_TIMEOUT
            readTimeout = Constants.DEFAULT_READ_TIMEOUT

            setRequestProperty("X-Protocol-Version", "1")

            // "A man is not dead while his name is still spoken."
            // - Going Postal, Chapter 4 prologue
            setRequestProperty("X-Clacks-Overhead", "GNU Terry Pratchett")

            try {
                connect()
            } catch (e: IOException) {
                return Failure(retrieveIoExceptionMessage(e))
            }

            return try {
                val version = getHeaderField("X-Black-Version")
                if (version != null) {
                    return Success(version)
                }
                return Failure("Someone is listening on that address, but it is not blackd.")
            } catch (e: IOException) {
                Failure(errorStream.readBytes().toString())
            }
        }
    }

    private fun retrieveIoExceptionMessage(e: IOException): String {
        val defaultMessage = when (e) {
            is ConnectException -> "Connection failed."
            is SSLException -> "Failed to connect using SSL."
            is SocketTimeoutException -> "Read timed out."
            else -> "Something went wrong."
        }
        return (e.message ?: defaultMessage).replaceFirstChar { c -> c.uppercaseChar() }
    }

    fun reformat(
        sourceCode: String,
        pyi: Boolean = false,
        lineLength: Int = 88,
        fastMode: Boolean = false,
        skipStringNormalization: Boolean = false,
        skipMagicTrailingComma: Boolean = false,
        previewMode: Boolean = false,
        targetPythonVersions: String = "",
        connectTimeout: Int = 0,
        readTimeout: Int = 0,
    ): Result<BlackdResponse, String> {

        with(blackdUrl.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            doOutput = true
            this.connectTimeout = connectTimeout
            this.readTimeout = readTimeout

            setRequestProperty("X-Protocol-Version", "1")
            setRequestProperty("X-Fast-Or-Safe", if (fastMode) "fast" else "safe")
            setRequestProperty("X-Line-Length", lineLength.toString())

            if (pyi) {
                setRequestProperty("X-Python-Variant", "pyi")
            } else {
                if (targetPythonVersions.isNotEmpty()) {
                    setRequestProperty("X-Python-Variant", targetPythonVersions)
                }
            }

            if (skipStringNormalization) {
                setRequestProperty("X-Skip-String-Normalization", "yes")
            }

            if (skipMagicTrailingComma) {
                setRequestProperty("X-Skip-Magic-Trailing-Comma", "yes")
            }

            if (previewMode) {
                setRequestProperty("X-Preview", "yes")
            }

            return try {
                connect()

                outputStream.use { os ->
                    val input: ByteArray = sourceCode.toByteArray()
                    os.write(input, 0, input.size)
                }

                val versionParts = getHeaderField("X-Black-Version").split('.')
                if (previewMode && (
                        versionParts[0].toInt() < 22 || versionParts[0].toInt() == 22 && versionParts[1].toInt() < 8)
                ) {
                    Failure("The version of <b>blackd</b> you are using does not support the 'preview' option.")
                }

                Success(parseBlackdResponse(this))
            } catch (e: IOException) {
                Failure(retrieveIoExceptionMessage(e))
            }
        }
    }

    private fun parseBlackdResponse(connection: HttpURLConnection): BlackdResponse {
        return when (connection.responseCode) {
            Constants.HTTP_RESPONSE_OK -> {
                logger.debug("200 OK: Code should be reformatted")
                val reformattedCode = connection.inputStream.bufferedReader().readText()
                /*
                IDEA internally uses "\n" as a separator, and attempt to set any document's
                text to something with "\r\n" inside will trip internal assertion check.
                */
                BlackdResponse.Blackened(reformattedCode.replace("\r\n", "\n"))
            }
            Constants.HTTP_NO_CHANGES_NEEDED -> {
                logger.debug("204: No changes to formatting, move along.")
                BlackdResponse.NoChangesMade
            }
            Constants.HTTP_INVALID_REQUEST -> {
                val errorText = connection.errorStream.bufferedReader().readText()

                /*
                 Heuristics are bad, but "blackd behind ngingx/whatever" setup can also reply with 400
                 if you are sending request to an SSL port over non-SSL connections, so we to somehow
                 distinguish between them.
                 */
                if (errorText.startsWith("Cannot parse")) {
                    logger.debug("400: Code contained syntax errors.")
                    BlackdResponse.SyntaxError(errorText)
                } else {
                    logger.debug("400: Invalid request was sent:\n$errorText")
                    BlackdResponse.InvalidRequest(errorText)
                }
            }
            Constants.HTTP_INTERNAL_ERROR -> {
                val errorText = connection.errorStream.bufferedReader().readText()
                logger.debug("500: Something unexpected happened:\n$errorText")
                BlackdResponse.InternalError(errorText)
            }
            else -> {
                logger.debug("Unexpected status code received: ${connection.responseCode} $connection")
                BlackdResponse.UnknownStatus(
                    connection.responseCode,
                    connection.inputStream.bufferedReader().readText()
                )
            }
        }
    }
}
