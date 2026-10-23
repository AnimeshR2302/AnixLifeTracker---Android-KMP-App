package com.anix.android.anixlifetracker.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.anix.android.anixlifetracker.model.ThemeMode

@Immutable
data class FinancialColors(
    val income: Color,
    val onIncome: Color,
    val incomeContainer: Color,
    val expense: Color,
    val onExpense: Color,
    val expenseContainer: Color,
    val neutralTransfer: Color,
    val onNeutralTransfer: Color,
    val neutralTransferContainer: Color,
    val investment: Color,
    val onInvestment: Color,
    val investmentContainer: Color
)

val LightFinancialColors = FinancialColors(
    income = Color(0xFF2E7D32),
    onIncome = Color(0xFFFFFFFF),
    incomeContainer = Color(0xFFE8F5E9),
    expense = Color(0xFFC62828),
    onExpense = Color(0xFFFFFFFF),
    expenseContainer = Color(0xFFFFEBEE),
    neutralTransfer = Color(0xFF0277BD),
    onNeutralTransfer = Color(0xFFFFFFFF),
    neutralTransferContainer = Color(0xFFE1F5FE),
    investment = Color(0xFF6A1B9A),
    onInvestment = Color(0xFFFFFFFF),
    investmentContainer = Color(0xFFF3E5F5)
)

val DarkFinancialColors = FinancialColors(
    income = Color(0xFF81C784),
    onIncome = Color(0xFF00390A),
    incomeContainer = Color(0xFF1B4D20),
    expense = Color(0xFFE57373),
    onExpense = Color(0xFF410002),
    expenseContainer = Color(0xFF621012),
    neutralTransfer = Color(0xFF4FC3F7),
    onNeutralTransfer = Color(0xFF00344F),
    neutralTransferContainer = Color(0xFF004C6D),
    investment = Color(0xFFBA68C8),
    onInvestment = Color(0xFF3B0054),
    investmentContainer = Color(0xFF521469)
)

val LocalFinancialColors = staticCompositionLocalOf { LightFinancialColors }

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535F70),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD7E3F7),
    onSecondaryContainer = Color(0xFF101C2B),
    tertiary = Color(0xFF6B5778),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF2DAFF),
    onTertiaryContainer = Color(0xFF251431),
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFF8F9FF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = Color(0xFFD7E3F7),
    tertiary = Color(0xFFD6BEE4),
    onTertiary = Color(0xFF3B2947),
    tertiaryContainer = Color(0xFF533F5F),
    onTertiaryContainer = Color(0xFFF2DAFF),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE1E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE1E2E9),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C6CF)
)

@Composable
fun AnixTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val financialColors = if (darkTheme) DarkFinancialColors else LightFinancialColors

    CompositionLocalProvider(
        LocalFinancialColors provides financialColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content
        )
    }
}
