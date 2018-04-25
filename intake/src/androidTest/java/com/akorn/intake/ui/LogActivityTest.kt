package com.akorn.intake.ui


import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.akorn.akorn.MockAction
import com.akorn.akorn.MockActionEngine
import com.akorn.akorn.Mockery
import com.akorn.intake.R
import com.akorn.intake.logic.MealLogStateModel
import com.akorn.intake.logic.MealLogTree
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LogActivityTest {
    init {
        Mockery.makeAMockeryOf(MealLogStateModel(actionEngine = MockActionEngine))
    }

    @get:Rule
    val activityTestRule = ActivityTestRule(LogActivity::class.java)


    @Test
    fun mainActivityLoadingStateTest() {
        MockActionEngine.doAction(MockAction(MealLogTree.Loading))

        try {
            Thread.sleep(2000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        onView(withId(R.id.newMealFab)).check(matches(not(isDisplayed())))
    }

    @Test
    fun mainActivityBlackStateTest() {
        MockActionEngine.doAction(MockAction(MealLogTree.Done.Blank))

        try {
            Thread.sleep(2000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        onView(withId(R.id.newMealFab)).check(matches(isDisplayed()))
    }

    @Test
    fun mainActivityCouldNotLoadMealsStateTest() {
        MockActionEngine.doAction(MockAction(MealLogTree.Done.MealLogStateError.CouldNotLoadMeals))

        try {
            Thread.sleep(2000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        onView(allOf(withId(android.support.design.R.id.snackbar_text), withText(R.string.error_getting_meals))).check(matches(isDisplayed()))
    }
}
