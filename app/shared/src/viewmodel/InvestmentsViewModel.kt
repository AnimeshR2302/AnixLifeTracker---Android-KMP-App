package com.anix.android.anixlifetracker.viewmodel

import com.anix.android.anixlifetracker.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class InvestmentsUiState(
    val holdings: List<InvestmentHolding> = DemoDataProvider.investmentHoldings,
    val totalValue: Money = DemoDataProvider.totalInvestmentValue,
    val investedValue: Money = DemoDataProvider.totalInvestmentInvested,
    val totalGain: Money = DemoDataProvider.totalInvestmentGain,
    val returnPercentage: Double = DemoDataProvider.investmentReturnPercentage,
    val marketInsights: List<MarketInsight> = DemoDataProvider.marketInsights,
    val projectionYears: Int = 10,
    val projectedValue: Money = Money.fromMajor(4850000)
)

class InvestmentsViewModel {
    private val _uiState = MutableStateFlow(InvestmentsUiState())
    val uiState: StateFlow<InvestmentsUiState> = _uiState.asStateFlow()

    fun updateProjectionYears(years: Int) {
        val multiplier = 1.0 + (years * 0.12)
        val newProj = (DemoDataProvider.totalInvestmentValue.amountInCents * multiplier).toLong()
        _uiState.value = _uiState.value.copy(
            projectionYears = years,
            projectedValue = Money(newProj)
        )
    }
}
