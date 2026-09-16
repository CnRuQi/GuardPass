package com.example.myandroid.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myandroid.ui.main.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launcherShowsVaultAndFavorites() {
        composeRule.onNodeWithText("密码库", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("收藏", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("分类", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("设置", useUnmergedTree = true).assertIsDisplayed()
    }
}
