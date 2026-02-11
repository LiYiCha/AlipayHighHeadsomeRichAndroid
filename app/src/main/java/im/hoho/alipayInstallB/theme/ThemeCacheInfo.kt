package im.hoho.alipayInstallB.theme

/**
 * 主题缓存信息数据类（Hook层使用）
 *
 * 用于存储预处理的主题信息，包含所有必需字段
 * 这些信息在ThemeManager复制主题时生成，ThemeHook直接读取使用
 *
 * 注意：这个类与 ThemeModels.kt 中的 ThemeInfo（UI层）是不同的类
 */
data class ThemeCacheInfo(
    // 基本信息
    val themeId: String,                    // 主题目录名
    val skinId: String,                     // 皮肤ID（从meta.json读取）
    val userSkinId: String,                 // 用户皮肤ID（通常与themeId相同）
    val userId: String,                     // 用户ID

    // 验证信息
    val md5: String,                        // 主题MD5（计算得出）
    val appSquareMd5: String = md5,         // 应用广场MD5（通常与md5相同）

    // 时间信息
    val cacheTime: Long,                    // 缓存时间戳
    val expireDate: String,                 // 过期日期（格式：yyyy-MM-dd）
    val diyExpiredTime: Long = 0,           // DIY过期时间

    // 版本和类型
    val versionLimit: String,               // 版本限制
    val skinType: String = "INST_UNLIMITED", // 皮肤类型

    // 其他信息
    val name: String,                       // 主题名称
    val materialId: String = "",            // 材料ID
    val isDiySkin: Boolean = false,         // 是否是DIY皮肤
    val usageScene: String = "theme"        // 使用场景
) {
    /**
     * 转换为SCCacheInfoModel所需的Map格式
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "themeId" to themeId,
            "skinId" to skinId,
            "userSkinId" to userSkinId,
            "userId" to userId,
            "md5" to md5,
            "appSquareMd5" to appSquareMd5,
            "cacheTime" to cacheTime,
            "expireDate" to expireDate,
            "diyExpiredTime" to diyExpiredTime,
            "versionLimit" to versionLimit,
            "skinType" to skinType,
            "name" to name,
            "materialId" to materialId,
            "isDiySkin" to isDiySkin,
            "usageScene" to usageScene
        )
    }

    companion object {
        /**
         * 从 Map 构造 ThemeInfo
         * 用于在 Xposed 环境中手动解析 JSON，避免 Jackson 反序列化 Kotlin data class 的问题
         */
        @JvmStatic
        fun fromMap(map: Map<String, Any>): ThemeCacheInfo {
            return ThemeCacheInfo(
                themeId = map["themeId"] as String,
                skinId = map["skinId"] as String,
                userSkinId = map["userSkinId"] as String,
                userId = map["userId"] as String,
                md5 = map["md5"] as String,
                appSquareMd5 = (map["appSquareMd5"] as? String) ?: (map["md5"] as String),
                cacheTime = (map["cacheTime"] as? Number)?.toLong() ?: 0L,
                expireDate = map["expireDate"] as String,
                diyExpiredTime = (map["diyExpiredTime"] as? Number)?.toLong() ?: 0L,
                versionLimit = map["versionLimit"] as String,
                skinType = (map["skinType"] as? String) ?: "INST_UNLIMITED",
                name = map["name"] as String,
                materialId = (map["materialId"] as? String) ?: "",
                isDiySkin = (map["isDiySkin"] as? Boolean) ?: false,
                usageScene = (map["usageScene"] as? String) ?: "theme"
            )
        }
    }
}
