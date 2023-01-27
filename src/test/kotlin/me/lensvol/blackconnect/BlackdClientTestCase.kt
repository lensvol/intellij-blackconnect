package me.lensvol.blackconnect

import org.junit.Assert
import org.junit.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import java.net.URL

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
                    .withHeader("X-Black-Version", "22.8.0")
                    .withBody("with function(x) as f:\r\n    pass")
            )
        }

        val blackdClient = BlackdClient("localhost", 45484)
        val response = blackdClient.reformat("with function(x) as f:\r\n    pass")

        Assert.assertTrue(response is Success)
        Assert.assertTrue((response as Success).value is BlackdResponse.Blackened)

        val blackenedCode = response.value as BlackdResponse.Blackened
        Assert.assertFalse(blackenedCode.sourceCode.contains("\r\n"))
    }

    @Test
    fun test_python311_support_errors_out_on_less_22_6() {
        mockServer.apply {
            this.`when`(
                HttpRequest.request()
                    .withMethod("POST")
                    .withPath("/")
            ).respond(
                HttpResponse.response()
                    .withStatusCode(200)
                    .withHeader("X-Black-Version", "22.3.0")
                    .withBody("with function(x) as f:\r\n    pass")
            )
        }

        val blackdClient = BlackdClient("localhost", 45484)
        val response = blackdClient.reformat("with function(x) as f:\r\n    pass", targetPythonVersions = "py311")

        Assert.assertTrue(response is Failure)
        Assert.assertTrue((response as Failure).reason.contains("Python 3.11"))
    }

    @Test
    fun test_python311_support_is_okay_on_more_22_6() {
        mockServer.apply {
            this.`when`(
                HttpRequest.request()
                    .withMethod("POST")
                    .withPath("/")
            ).respond(
                HttpResponse.response()
                    .withStatusCode(200)
                    .withHeader("X-Black-Version", "22.6.0")
                    .withBody("with function(x) as f:\r\n    pass")
            )
        }

        val blackdClient = BlackdClient("localhost", 45484)
        val response = blackdClient.reformat("with function(x) as f:\r\n    pass", targetPythonVersions = "py311")

        Assert.assertTrue(response is Success)
    }

    @Test
    fun test_python311_support_is_checked_upon_request() {
        mockServer.apply {
            this.`when`(
                HttpRequest.request()
                    .withMethod("POST")
                    .withPath("/")
            ).respond(
                HttpResponse.response()
                    .withStatusCode(200)
                    .withHeader("X-Black-Version", "22.3.0")
                    .withBody("with function(x) as f:\r\n    pass")
            )
        }

        val blackdClient = BlackdClient("localhost", 45484)
        val response = blackdClient.reformat("with function(x) as f:\r\n    pass", targetPythonVersions = "py311")

        Assert.assertTrue(response is Failure)
        Assert.assertTrue((response as Failure).reason.contains("Python 3.11"))
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
                    .withHeader("X-Black-Version", "22.8.0")
                    .withBody("with function(x) as f:\n    pass")
            )
        }

        val blackdClient = BlackdClient("localhost", 45484)
        val response = blackdClient.reformat("with function(x) as f:\n    pass")

        Assert.assertTrue(response is Success)
        Assert.assertTrue((response as Success).value is BlackdResponse.Blackened)

        val blackenedCode = response.value as BlackdResponse.Blackened
        Assert.assertEquals(blackenedCode.sourceCode, "with function(x) as f:\n    pass")
    }

    @Test
    fun test_blackd_client_can_be_used_with_ssl_enabled() {

        val blackdClient = BlackdClient("remote.host", 1337, useSsl = true)

        Assert.assertEquals(blackdClient.blackdUrl, URL("https://remote.host:1337"))
    }

    @Test
    fun test_blackd_client_can_be_used_with_ssl_disabled() {

        val blackdClient = BlackdClient("localhost", 45484, useSsl = false)

        Assert.assertEquals(blackdClient.blackdUrl, URL("http://localhost:45484"))
    }

    private fun mockVersion(version: String) {
        mockServer.reset()
        mockServer.apply {
            this.`when`(
                HttpRequest.request()
                    .withMethod("POST")
                    .withPath("/")
            ).respond(
                HttpResponse.response()
                    .withStatusCode(204)
                    .withHeader("X-Black-Version", version)
            )
        }
    }
    @Test
    fun `test blackd client checks version with preview`() {
        val blackdClient = BlackdClient("localhost", 45484)
        val format = { blackdClient.reformat("", previewMode = true) }

        val successes = listOf("22.8.0", "23.7.0", "23.8.0")
        val failures = listOf("21.7.0", "21.8.0", "22.7.0")

        for (version in successes) {
            mockVersion(version)
            Assert.assertTrue(format() is Success)
        }
        for (version in failures) {
            mockVersion(version)
            Assert.assertTrue(format() is Failure)
        }
    }
}
