package ua.kpi.solarpractice6

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class SolarUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun displaysAnimationAndFilesNavigation() {
        rule.onNodeWithText("ПЗ 21 · Анімація").assertIsDisplayed()
        rule.onNodeWithText("22").performClick()
        rule.onNodeWithText("ПЗ 22 · CSV / JSON").assertIsDisplayed()
    }
}
