package me.lensvol.blackconnect.mocks

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import me.lensvol.blackconnect.BlackdResponse
import me.lensvol.blackconnect.CodeReformatter

@Service
class CodeReformatterMock(project: Project) : CodeReformatter(project) {
    private lateinit var mockedResponse: BlackdResponse

    fun setResponse(response: BlackdResponse) {
        mockedResponse = response
    }

    override fun process(tag: String, sourceCode: String, isPyi: Boolean, receiver: (BlackdResponse) -> Unit) {
        receiver(mockedResponse)
    }
}