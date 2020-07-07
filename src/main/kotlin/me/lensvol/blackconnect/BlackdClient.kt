package me.lensvol.blackconnect

import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL

class BlackdClient(val hostname: String, val port: Int) {
    fun checkConnection(): Pair<Boolean, String> {
        val url = URL("http://${hostname}:${port}")

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            setRequestProperty("X-Protocol-Version", "1")

            try {
                connect()
            } catch (e: ConnectException) {
                return Pair(false, e.message ?: "Connection failed.")
            }

            return try {
                val version = getHeaderField("X-Black-Version")
                Pair(true, version)
            } catch (e: IOException) {
                Pair(false, errorStream.readBytes().toString())
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
    ): Pair<Int, String> {
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
                return Pair(-1, e.message ?: "Connection failed.")
            }

            try {
                outputStream.use { os ->
                    val input: ByteArray = sourceCode.toByteArray()
                    os.write(input, 0, input.size)
                }

                inputStream.bufferedReader().use { return Pair(responseCode, it.readText()) }
            } catch (e: IOException) {
                return Pair(responseCode, errorStream.readBytes().toString())
            }
        }
    }
}