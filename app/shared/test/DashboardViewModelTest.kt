package com.anix.android.anixlifetracker

import com.anix.android.anixlifetracker.viewmodel.DashboardViewModel
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DashboardViewModelTest {

    @Test
    fun testDashboardInitialState() {
        val viewModel = DashboardViewModel()
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.totalBalance.amountInCents > 0)
        assertTrue(state.recentTransactions.isNotEmpty())
        assertTrue(state.spendingCategories.isNotEmpty())
    }
}
