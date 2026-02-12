package im.hoho.alipayInstallB.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 全局统一主题配置
 *
 * 所有页面统一使用此主题，确保视觉一致性
 * 配色方案：浅绿色渐变蓝
 */

// ==================== 品牌色 ====================

/** 主色 - 清新绿 */
val AppPrimary = Color(0xFF2EC4B6)

/** 主色深 - 深绿 */
val AppPrimaryDark = Color(0xFF1A9E93)

/** 主色浅 - 浅绿 */
val AppPrimaryLight = Color(0xFF5EDDD1)

/** 强调色 - 天蓝 */
val AppAccent = Color(0xFF3B9AE1)

/** 强调色浅 - 浅蓝 */
val AppAccentLight = Color(0xFF6DB8F0)

// ==================== 功能色 ====================

/** 成功色 */
val AppSuccess = Color(0xFF4CAF50)

/** 警告色 */
val AppWarning = Color(0xFFFFA726)

/** 错误/危险色 */
val AppError = Color(0xFFEF5350)

/** 信息色 */
val AppInfo = Color(0xFF42A5F5)

// ==================== 中性色 ====================

/** 标题文字色 */
val AppTextPrimary = Color(0xFF1A1A2E)

/** 正文文字色 */
val AppTextSecondary = Color(0xFF4A4A68)

/** 辅助文字色 */
val AppTextHint = Color(0xFF9E9EB8)

/** 分割线色 */
val AppDivider = Color(0xFFE8E8F0)

/** 卡片背景色 */
val AppCardBackground = Color(0xFFFFFFFF)

/** 容器背景色 */
val AppContainerBackground = Color(0xFFF2F4F8)

// ==================== 渐变色 ====================

/** 页面背景渐变 - 浅绿到浅蓝 */
val AppBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF0FAF8),
        Color(0xFFEBF3FB)
    )
)

/** 头部横幅渐变 - 绿到蓝 */
val AppBannerGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF2EC4B6),
        Color(0xFF3B9AE1)
    )
)

/** 皮肤功能卡片渐变 */
val AppSkinCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF2EC4B6),
        Color(0xFF26A69A)
    )
)

/** 主题功能卡片渐变 */
val AppThemeCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF3B9AE1),
        Color(0xFF5C6BC0)
    )
)

/** 使用说明功能卡片渐变 */
val AppGuideCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF26C6DA),
        Color(0xFF4DD0E1)
    )
)

/** 占位图渐变 */
val AppPlaceholderGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFE0F2F1),
        Color(0xFFE3F2FD)
    )
)

// ==================== 圆角 ====================

/** 大圆角 - 用于卡片 */
val AppShapeLarge = RoundedCornerShape(20.dp)

/** 中圆角 - 用于按钮、输入框 */
val AppShapeMedium = RoundedCornerShape(14.dp)

/** 小圆角 - 用于标签、指示器 */
val AppShapeSmall = RoundedCornerShape(8.dp)

// ==================== Material 配色方案 ====================

private val AppColorScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4F5F0),
    onPrimaryContainer = AppTextPrimary,
    secondary = AppAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6EAFB),
    onSecondaryContainer = AppTextPrimary,
    background = Color(0xFFF0FAF8),
    onBackground = AppTextPrimary,
    surface = AppCardBackground,
    onSurface = AppTextPrimary,
    surfaceVariant = AppContainerBackground,
    onSurfaceVariant = AppTextSecondary,
    error = AppError,
    onError = Color.White
)

/**
 * 统一应用主题
 *
 * 所有 Activity 的 setContent 中统一使用此主题包裹
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content
    )
}
