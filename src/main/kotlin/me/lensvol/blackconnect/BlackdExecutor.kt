package me.lensvol.blackconnect

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import java.io.BufferedReader
import java.io.IOException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

sealed class ExecutionResult {
    class Failed(val reason: String): ExecutionResult()
    class AlreadyStarted(val pid: Int): ExecutionResult()
    class Started(val pid: Int): ExecutionResult()
}

@Service
class BlackdExecutor : Disposable {
    private val logger = Logger.getInstance(BlackdExecutor::class.java.name)
    private val processBuilder: ProcessBuilder = ProcessBuilder()
    private var blackdProcess: Process? = null

    fun startDaemon(binaryPath: String, bindOnHostname: String, bindOnPort: Int): ExecutionResult {
        if (isRunning()) {
            return ExecutionResult.AlreadyStarted(currentPid())
        }

        val cmdLine = GeneralCommandLine(
            binaryPath,
            "--bind-host",
            bindOnHostname,
            "--bind-port",
            bindOnPort.toString()
        )
        try {
            val spawnedProcess = cmdLine.createProcess()
            spawnedProcess.waitFor(2, TimeUnit.SECONDS)

            if (spawnedProcess.isAlive) {
                blackdProcess = spawnedProcess
                logger.debug("Started $binaryPath on $bindOnHostname:$bindOnPort (PID: ${currentPid()}")
                return ExecutionResult.Started(currentPid())
            } else {
                val reason = spawnedProcess.errorStream.bufferedReader().use(BufferedReader::readText)
                return ExecutionResult.Failed(reason)
            }

        } catch (e: IOException) {
            return ExecutionResult.Failed(e.message ?: "Unknown IO error")
        } catch (e: ExecutionException) {
            return ExecutionResult.Failed(e.message ?: "Unknown execution error")
        }
    }

    private fun currentPid(): Int {
        blackdProcess?.let {
            return OSProcessUtil.getProcessID(it)
        }

        return -1
    }

    fun isRunning(): Boolean {
        return blackdProcess?.isAlive ?: false
    }

    fun stopDaemon() {
        blackdProcess?.let {
            OSProcessUtil.killProcessTree(it)
            OSProcessUtil.killProcess(it)
        }
        blackdProcess = null
    }

    override fun dispose() {
        logger.debug("Disposing of the running black[d] instance...")
        stopDaemon()
    }
}