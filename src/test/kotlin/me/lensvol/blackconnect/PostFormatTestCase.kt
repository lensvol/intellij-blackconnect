package me.lensvol.blackconnect

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.RuleChain
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.runInEdtAndWait
import me.lensvol.blackconnect.BlackSettingsRule.Companion.blackSettings
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test

/**
 * Tests running blackd as a post-format processor.
 */
class PostFormatTestCase {
    companion object {
        const val TEST_PORT = Constants.DEFAULT_BLACKD_PORT - 1

        @ClassRule
        @JvmField
        val blackd = BlackdProcessRule(TEST_PORT)
    }

    private val ideaTest = IdeaTestRule()
    private val fixture: CodeInsightTestFixture
        get() = ideaTest.fixture

    @Rule
    @JvmField
    val chain: RuleChain = RuleChain(
        ideaTest,
        BlackSettingsRule(ideaTest, TEST_PORT),
    )

    @Test
    fun `test reformat with default Python formatter`() = runInEdtAndWait {
        fixture.configureByText(
            "a.py",
            """
            f(1,2,3,)
            """.trimIndent()
        )
        fixture.performEditorAction(IdeActions.ACTION_EDITOR_REFORMAT)
        fixture.checkResult(
            """
            f(1, 2, 3, )
            
            """.trimIndent()
        )
    }

    @Test
    fun `test reformat whole file with Black`() = runInEdtAndWait {
        fixture.blackSettings.triggerOnReformat = true
        fixture.configureByText(
            "a.py",
            """
            f(1,2,3,)
            """.trimIndent()
        )
        fixture.performEditorAction(IdeActions.ACTION_EDITOR_REFORMAT)
        fixture.checkResult(
            """
            f(
                1,
                2,
                3,
            )

            """.trimIndent()
        )
    }

    @Test
    fun `test reformat fragment with Black`() = runInEdtAndWait {
        fixture.blackSettings.triggerOnReformat = true
        fixture.configureByText(
            "a.py",
            """
            f(1,2,3,)
            <selection>g(1,2,3,)</selection>
            """.trimIndent()
        )
        fixture.performEditorAction(IdeActions.ACTION_EDITOR_REFORMAT)
        fixture.checkResult(
            """
            f(1,2,3,)
            g(
                1,
                2,
                3,
            )

            """.trimIndent()
        )
    }

    @Test
    fun `test reformat fragment with Black end of file, respects indents`() = runInEdtAndWait {
        fixture.blackSettings.triggerOnReformat = true
        fixture.configureByText(
            "a.py",
            """
            class Foo:
                def bar(self):
                    <selection>g(1,2,3,)</selection>
 
            """.trimIndent()
        )
        fixture.performEditorAction(IdeActions.ACTION_EDITOR_REFORMAT)
        fixture.checkResult(
            """
            class Foo:
                def bar(self):
                    g(
                        1,
                        2,
                        3,
                    )
 
            """.trimIndent()
        )
    }
}
