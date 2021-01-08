package me.lensvol.blackconnect

import com.intellij.execution.process.OSProcessUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import java.io.File
import java.io.IOException

sealed class ExecutionResult {
    class Failed(val reason: String): ExecutionResult()
    class AlreadyStarted(val pid: Int): ExecutionResult()
    class Started(val pid: Int): ExecutionResult()
}

class BlackdProcess(private val binaryPath: String, private val bindOnHostname: String, private val bindOnPort: Int) {
    private val logger = Logger.getInstance(BlackdProcess::class.java.name)
    private val processBuilder: ProcessBuilder = ProcessBuilder()
    private var blackdProcess: Process? = null

    fun startDaemon(): ExecutionResult {
        if (isRunning()) {
            return ExecutionResult.AlreadyStarted(currentPid())
        }

        processBuilder
            .command(
                binaryPath,
                "--bind-host",
                bindOnHostname,
                "--bind-port",
                bindOnPort.toString()
            )
            .directory(File(System.getProperty("user.home")))
            .inheritIO()
        try {
            blackdProcess = processBuilder.start()
        } catch (e: IOException) {
            return ExecutionResult.Failed(e.message ?: "Unknown error")
        }

        logger.debug("Started $binaryPath on $bindOnHostname:$bindOnPort (PID: ${currentPid()}")
        return ExecutionResult.Started(currentPid())
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
        OSProcessUtil.killProcessTree(blackdProcess!!)
        OSProcessUtil.killProcess(blackdProcess!!)
        blackdProcess = null
    }
}

@Service
class BlackdExecutor : Disposable {
    private val logger = Logger.getInstance(BlackdExecutor::class.java.name)
    private val serversByBinding = HashMap<String, BlackdProcess>()

    override fun dispose() {
        logger.debug("Disposing of the running black[d] instances...")

        serversByBinding.values.map {
            it.stopDaemon()
        }
    }
}