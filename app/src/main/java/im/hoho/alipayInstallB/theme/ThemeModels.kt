package im.hoho.alipayInstallB.theme

/**
 * 主题信息（UI层使用）
 *
 * 注意：这个类与 ThemeCacheInfo.kt 中的 ThemeCacheInfo（Hook层）是不同的类
 */
data class ThemeInfo(
    val themeId: String,
    val name: String,
    val description: String,
    val previewImagePath: String? = null,
    val isSelected: Boolean = false
)

/**
 * 主题资源项
 */
data class ThemeResource(
    val position: String,
    val type: String,  // color, image, lottieVideo
    val color: String? = null,
    val image: String? = null,
    val lottie: String? = null,
    val lottieVideo: String? = null,
    val description: String? = null,
    val metaList: List<ThemeResourceMeta>? = null,
    // 暗色模式资源
    val darkColor: String? = null,
    val darkImage: String? = null,
    val darkLottieVideo: String? = null
)

/**
 * 主题资源元数据
 */
data class ThemeResourceMeta(
    val image: String? = null,
    val aspectRatio: Int? = null,
    val darkImage: String? = null
)

/**
 * 主题元数据
 */
data class ThemeMetadata(
    val skinId: String = "",
    val description: String = "",
    val resource: List<ThemeResource> = emptyList()
)

/**
 * 主题状态
 */
data class ThemeState(
    val availableThemes: List<ThemeInfo> = emptyList(),
    val selectedThemeId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 下载状态
 */
sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    data object Success : DownloadState()
    data class Error(val message: String) : DownloadState()
}
