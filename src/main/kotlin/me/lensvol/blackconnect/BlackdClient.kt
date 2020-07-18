package me.lensvol.blackconnect

import com.intellij.openapi.diagnostic.Logger
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL

sealed class BlackdResponse {
    object NoChangesMade : BlackdResponse()
    data class SyntaxError(val reason: String) : BlackdResponse()
    data class InternalError(val reason: String) : BlackdResponse()
    data class Blackened(val sourceCode: String) : BlackdResponse()
    data class UnknownStatus(val code: Int, val responseText: String) : BlackdResponse()
}

class BlackdClient(val hostname: String, val port: Int) {

    private val logger = Logger.getInstance(CodeReformatter::class.java.name)

    fun checkConnection(): Result<String, String> {
        val url = URL("http://${hostname}:${port}")

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            setRequestProperty("X-Protocol-Version", "1")

            try {
                connect()
            } catch (e: ConnectException) {
                return Failure(e.message ?: "Connection failed.")
            }

            return try {
                val version = getHeaderField("X-Black-Version")
                if (version != null) {
                    return Success(version)
                }
                return Failure("Someone is listenning on that address, but it is not blackd.")
            } catch (e: IOException) {
                Failure(errorStream.readBytes().toString())
            }
        }
    }

    fun reformat(
        sourceCode: String,
        pyi: Boolean = false,
        lineLength: Int = 88,
        fastMode: Boolean = false,
        skipStringNormalization: Boolean = false,
        targetPythonVersions: String = ""
    ): Result<BlackdResponse, Exception> {
        val url = URL("http://${hostname}:${port}")

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            doOutput = true

            setRequestProperty("X-Protocol-Version", "1")
            setRequestProperty("X-Fast-Or-Safe", if (fastMode) "fast" else "safe")
            setRequestProperty("X-Line-Length", lineLength.toString())

            if (pyi) {
                setRequestProperty("X-Python-Variant", "pyi")
            } else {
                if (!targetPythonVersions.isEmpty()) {
                    setRequestProperty("X-Python-Variant", targetPythonVersions)
                }
            }

            if (skipStringNormalization) {
                setRequestProperty("X-Skip-String-Normalization", "yes")
            }

            try {
                connect()
            } catch (e: ConnectException) {
                return Failure(e)
            }

            try {
                outputStream.use { os ->
                    val input: ByteArray = sourceCode.toByteArray()
                    os.write(input, 0, input.size)
                }

                return Success(parseBlackdResponse(this))
            } catch (e: IOException) {
                return Failure(e)
            }
        }
    }

    private fun parseBlackdResponse(connection: HttpURLConnection): BlackdResponse {
        return when (connection.responseCode) {
            200 -> {
                logger.debug("200 OK: Code should be reformatted")
                BlackdResponse.Blackened(connection.inputStream.bufferedReader().readText())
            }
            204 -> {
                logger.debug("204: No changes to formatting, move along.")
                BlackdResponse.NoChangesMade
            }
            400 -> {
                logger.debug("400: Code contained syntax errors.")
                BlackdResponse.SyntaxError(connection.errorStream.bufferedReader().readText())
            }
            500 -> {
                val errorText = connection.errorStream.bufferedReader().readText()
                logger.debug("500: Something unexpected happened:\n${errorText}")
                BlackdResponse.InternalError(errorText)
            }
            else -> {
                logger.debug("Unexpected status code received: ${connection.responseCode} ${connection}")
                BlackdResponse.UnknownStatus(
                    connection.responseCode,
                    connection.inputStream.bufferedReader().readText()
                )
            }
        }
    }
}