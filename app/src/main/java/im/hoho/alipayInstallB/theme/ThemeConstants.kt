package im.hoho.alipayInstallB.theme

/**
 * 主题中心常量定义
 */
object ThemeConstants {

    // 支付宝内部存储路径
    const val INTERNAL_STORAGE_PATH = "/data/data/com.eg.android.AlipayGphone/files/skin_center_dir"

    // 外部存储路径（SD卡）
    // 使用硬编码路径，避免在 Xposed Hook 环境下 Environment.getExternalStorageDirectory() 调用失败
    const val EXTERNAL_STORAGE_PATH = "/storage/emulated/0/Android/media/com.eg.android.AlipayGphone/000_HOHO_THEME_CENTER"

    // 主题文件夹路径
    const val THEMES_FOLDER = "themes"

    // 操作标记文件路径
    const val EXPORT_PATH = "$EXTERNAL_STORAGE_PATH/export"
    const val DELETE_PATH = "$EXTERNAL_STORAGE_PATH/delete"
    const val UPDATE_PATH = "$EXTERNAL_STORAGE_PATH/update"

    // 选中的主题文件
    const val SELECTED_THEME_FILE = "selected_theme"

    // SharedPreferences 名称
    const val PREFS_NAME = "theme_prefs"
    const val KEY_FIRST_RUN = "theme_first_run"

    // 导出目录
    const val EXPORTED_THEMES_FOLDER = "exported_themes"
}

/**
 * 主题操作类型
 */
enum class ThemeOperation(val displayName: String, val filePath: String) {
    EXPORT("导出主题", ThemeConstants.EXPORT_PATH),
    DELETE("删除主题缓存", ThemeConstants.DELETE_PATH),
    UPDATE("更新主题缓存", ThemeConstants.UPDATE_PATH)
}
