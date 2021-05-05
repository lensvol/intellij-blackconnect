package me.lensvol.blackconnect

import com.intellij.openapi.components.service
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings

/**
 * Re-formats the selected range or the whole file via blackd as a post-format processor.
 *
 * Note that we have to use network IO synchronously in the EDT here due to the synchronous API of post-format
 * processors. A modal progress dialog might have helped us to avoid UI freezes and explain delays to the user better,
 * but we cannot use it because we are under the write action in the EDT. We have to use network IO timeouts instead.
 */
class BlackPostFormatProcessor : PostFormatProcessor {
    companion object {
        private const val EDT_TIMEOUT = 2_000
    }

    override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement = source

    override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
        if (!isApplicable(source)) {
            return rangeToReformat
        }
        val fixedRange = fixRangeToReformat(source, rangeToReformat)
        val fragment = source.getText(fixedRange) ?: return rangeToReformat
        val formatter = source.project.service<CodeReformatter>()
        val response = formatter.reformatFragment(
            fragment,
            source.name.endsWith(".pyi"),
            connectTimeout = EDT_TIMEOUT,
            readTimeout = EDT_TIMEOUT,
        )
        val blackened = response as? BlackdResponse.Blackened ?: return rangeToReformat
        source.replaceAndCommit(fixedRange, blackened.sourceCode)
        return TextRange.from(fixedRange.startOffset, blackened.sourceCode.length)
    }

    private fun isApplicable(source: PsiFile): Boolean {
        val project = source.project
        if (!BlackConnectProjectSettings.getInstance(project).triggerOnReformat) return false
        val file = source.virtualFile ?: return false
        return project.service<CodeReformatter>().isFileSupported(file)
    }

    /**
     * A workaround for a bug in `PyTrailingBlankLinesPostFormatProcessor` to restore the full range.
     *
     * The trailing blank lines processor wrongly strips one last character if it's followed by a newline.
     *
     * Example:
     *
     * The trailing blank lines processor strips `)` in its return text range for `f(1, 2, 3, )\n`, while it shouldn't
     * do it.
     */
    private fun fixRangeToReformat(source: PsiFile, rangeToReformat: TextRange): TextRange {
        val text = source.text
        var endOffset = rangeToReformat.endOffset
        val nextOffset = endOffset + 1
        if (nextOffset < text.length && text[nextOffset] == '\n') {
            endOffset = nextOffset
        }
        return TextRange.create(rangeToReformat.startOffset, endOffset)
    }

    private fun PsiFile.replaceAndCommit(range: TextRange, s: String) {
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(this) ?: return
        documentManager.doPostponedOperationsAndUnblockDocument(document)
        try {
            document.replaceString(range.startOffset, range.endOffset, s)
        }
        finally {
            documentManager.commitDocument(document)
        }
    }

    private fun PsiFile.getText(range: TextRange): String? =
        PsiDocumentManager.getInstance(project).getDocument(this)?.getText(range)
}
