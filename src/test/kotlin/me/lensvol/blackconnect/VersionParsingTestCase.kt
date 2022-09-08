package me.lensvol.blackconnect

import org.junit.Assert.assertEquals
import org.junit.Test

class VersionParsingTestCase {
    @Test
    fun happy_path_parse_modern_style_black_version_spec() {
        assertEquals(
            BlackVersion.parse("22.8.0"),
            BlackVersion(22, 8, 0)
        )
    }

    @Test
    fun happy_path_parse_beta_period_black_version_spec() {
        assertEquals(
            BlackVersion.parse("21.12b6"),
            BlackVersion(21, 12, 6)
        )
    }

    @Test
    fun parsing_empty_string_returns_unknown_version() {
        assertEquals(
            BlackVersion.parse(""),
            BlackVersion(0, 0, 0)
        )
    }

    @Test
    fun parsing_garbage_string_returns_unknown_version() {
        assertEquals(
            BlackVersion.parse("JLKjaajJD"),
            BlackVersion(0, 0, 0)
        )
    }

    @Test
    fun parsing_broken_spec_returns_unknown_version() {
        assertEquals(
            BlackVersion.parse("21.b1"),
            BlackVersion(0, 0, 0)
        )
    }

    @Test
    fun parsing_negative_version_returns_unknown_version() {
        assertEquals(
            BlackVersion.parse("21.-2.10"),
            BlackVersion(0, 0, 0)
        )
    }
}