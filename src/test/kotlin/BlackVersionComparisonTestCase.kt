package me.lensvol.blackconnect

import org.junit.Assert.*
import org.junit.Test

class BlackVersionComparisonTestCase {
    @Test
    fun `test that BlackVersion objects describing same version are equal`() {
        val version = BlackVersion(22, 8, 0)

        assertEquals(version, version)
        assertFalse(version < version)
        assertFalse(version > version)
    }

    @Test
    fun `test that BlackVersion objects describing different versions are not equal`() {
        val someVersion = BlackVersion(21, 2, 0)
        val differentVersion = BlackVersion(22, 8, 0)

        assertNotEquals(someVersion, differentVersion)
    }

    @Test
    fun `test that lower version is compared properly with the higher one`() {
        val lowerVersion = BlackVersion(21, 2, 0)
        val higherVersion = BlackVersion(22, 8, 0)

        assertTrue(lowerVersion < higherVersion)
        assertFalse(lowerVersion > higherVersion)
    }
}