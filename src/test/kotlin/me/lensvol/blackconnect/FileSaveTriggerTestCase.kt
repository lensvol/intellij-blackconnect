package me.lensvol.blackconnect

import org.junit.Test

class FileSaveTriggerTestCase : BlackConnectTestCase() {
    @Test
    fun test_file_reformatted_on_save_if_enabled() {
        val unformattedFile = openFileInEditor("unformatted.py")
        val document = documentForFile(unformattedFile)
        setupBlackdResponse(BlackdResponse.Blackened("print(\"123\")"))

        pluginConfiguration.triggerOnEachSave = true
        FileSaveListener(myFixture.project).beforeDocumentSaving(document)

        myFixture.checkResult("print(\"123\")")
    }

    @Test
    fun test_file_is_not_reformatted_on_save_if_disabled() {
        val unformattedFile = openFileInEditor("unformatted.py")
        val document = documentForFile(unformattedFile)
        setupBlackdResponse(BlackdResponse.Blackened("print(\"123\")"))

        pluginConfiguration.triggerOnEachSave = false
        FileSaveListener(myFixture.project).beforeDocumentSaving(document)

        myFixture.checkResultByFile("unformatted.py")
    }

    @Test
    fun test_unsupported_file_is_not_reformatted_on_save_even_if_enabled() {
        val unformattedFile = openFileInEditor("not_python.txt")
        val document = documentForFile(unformattedFile)
        setupBlackdResponse(BlackdResponse.Blackened("print(\"123\")"))

        pluginConfiguration.triggerOnEachSave = true
        FileSaveListener(myFixture.project).beforeDocumentSaving(document)

        myFixture.checkResultByFile("not_python.txt")
    }
}
