package me.lensvol.blackconnect

import org.junit.Assert
import org.junit.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

class BlackdClientTestCase : BlackConnectTestCase() {
    lateinit var mockServer: ClientAndServer

    override fun setupMocks() {}

    override fun setUp() {
        super.setUp()
        mockServer = ClientAndServer.startClientAndServer(45484)
    }

    override fun tearDown() {
        mockServer.close()
        super.tearDown()
    }

    @Test
    fun test_windows_newlines_are_replaced_in_blackd_response() {
        mockServer.apply {
            this.`when`(
                HttpRequest.request()
                    .withMethod("POST")
                    .withPath("/")
            ).respond(
                HttpResponse.response()
                    .withStatusCode(200)
                    .withBody("with function(x) as f:\r\n    pass")
            )
        }

        val blackdClient = BlackdClient("localhost", 45484, false)
        val response = blackdClient.reformat("with function(x) as f:\r\n    pass")

        Assert.assertTrue(response is Success)
        Assert.assertTrue((response as Success).value is BlackdResponse.Blackened)

        val blackenedCode = response.value as BlackdResponse.Blackened
        Assert.assertFalse(blackenedCode.sourceCode.contains("\r\n"))
    }

    @Test
    fun test_unix_newlines_in_blackd_response_are_left_as_is() {
        mockServer.apply {
            this.`when`(
                HttpRequest.request()
                    .withMethod("POST")
                    .withPath("/")
            ).respond(
                HttpResponse.response()
                    .withStatusCode(200)
                    .withBody("with function(x) as f:\n    pass")
            )
        }

        val blackdClient = BlackdClient("localhost", 45484, false)
        val response = blackdClient.reformat("with function(x) as f:\n    pass")

        Assert.assertTrue(response is Success)
        Assert.assertTrue((response as Success).value is BlackdResponse.Blackened)

        val blackenedCode = response.value as BlackdResponse.Blackened
        Assert.assertEquals(blackenedCode.sourceCode, "with function(x) as f:\n    pass")
    }
}
