package com.openclassrooms.rebonnte.feature.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.openclassrooms.rebonnte.feature.R

class MedicineRobot(private val composeTestRule: AndroidComposeTestRule<*, *>) {

    private val context = composeTestRule.activity

    fun clickAddMedicine() {
        composeTestRule.onNodeWithContentDescription(context.getString(R.string.medicine_add_description)).performClick()
    }

    fun enterMedicineName(name: String) {
        composeTestRule.onNodeWithText(context.getString(R.string.medicine_new_name_label)).performTextInput(name)
    }

    fun enterInitialStock(stock: String) {
        composeTestRule.onNodeWithText(context.getString(R.string.medicine_new_stock_label)).performTextInput(stock)
    }

    fun clickSaveMedicine() {
        composeTestRule.onNodeWithText(context.getString(R.string.aisle_save)).performClick()
    }

    fun clickMedicine(name: String) {
        composeTestRule.onNodeWithText(name).performClick()
    }

    // Detail Screen Actions
    fun clickIncreaseStock() {
        composeTestRule.onNodeWithContentDescription(context.getString(R.string.medicine_detail_stock_increase)).performClick()
    }

    fun clickDecreaseStock() {
        composeTestRule.onNodeWithContentDescription(context.getString(R.string.medicine_detail_stock_decrease)).performClick()
    }

    fun clickDeleteMedicine() {
        composeTestRule.onNodeWithContentDescription(context.getString(R.string.medicine_detail_delete_description)).performClick()
    }

    fun confirmDelete() {
        composeTestRule.onNodeWithText(context.getString(R.string.medicine_detail_delete_button)).performClick()
    }

    infix fun verify(block: MedicineVerificationRobot.() -> Unit): MedicineVerificationRobot {
        return MedicineVerificationRobot(composeTestRule).apply(block)
    }
}

class MedicineVerificationRobot(private val composeTestRule: AndroidComposeTestRule<*, *>) {
    private val context = composeTestRule.activity

    fun medicineIsDisplayed(name: String) {
        composeTestRule.onNodeWithText(name).assertIsDisplayed()
    }

    fun stockValueIsDisplayed(value: String) {
        composeTestRule.onNodeWithText(value).assertIsDisplayed()
    }
    
    fun detailTitleIsDisplayed(name: String) {
        composeTestRule.onNodeWithText(name).assertIsDisplayed()
    }
}

fun medicineRobot(composeTestRule: AndroidComposeTestRule<*, *>, block: MedicineRobot.() -> Unit) =
    MedicineRobot(composeTestRule).apply(block)
