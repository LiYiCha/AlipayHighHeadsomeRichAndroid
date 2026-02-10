package im.hoho.alipayInstallB.theme

import com.alibaba.fastjson.JSON
import de.robv.android.xposed.XposedBridge
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 主题管理器
 *
 * 负责处理主题的导出、删除、更新操作
 * 不需要Hook，只需要文件替换
 */
object ThemeManager {

    private const val TAG = "ThemeManager"

    // 支付宝内部存储路径
    private const val INTERNAL_STORAGE_PATH = "/data/data/com.eg.android.AlipayGphone/files/skin_center_dir"

    // 外部存储路径（SD卡）
    private const val EXTERNAL_STORAGE_PATH = "/storage/emulated/0/Android/media/com.eg.android.AlipayGphone/000_HOHO_THEME_CENTER"

    // 操作标记文件路径
    private const val EXPORT_PATH = "$EXTERNAL_STORAGE_PATH/export"
    private const val DELETE_PATH = "$EXTERNAL_STORAGE_PATH/delete"
    private const val UPDATE_PATH = "$EXTERNAL_STORAGE_PATH/update"

    // 主题文件夹路径
    private const val THEMES_FOLDER = "themes"
    private const val EXPORTED_THEMES_FOLDER = "exported_themes"
    private const val SELECTED_THEME_FILE = "selected_theme"

    // 线程安全标志
    private val isOperationRunning = AtomicBoolean(false)

    // 防抖：记录上次日志时间
    private var lastSkipLogTime = 0L

    // 后台监控线程
    private var monitorThread: Thread? = null
    private val isMonitorRunning = AtomicBoolean(false)

    // Context 和 ClassLoader 持有者（从 Hook 环境中设置）
    @Volatile
    private var appContext: android.content.Context? = null

    @Volatile
    private var appClassLoader: ClassLoader? = null

    /**
     * 设置应用上下文（从 Hook 环境调用）
     */
    fun setAppContext(context: android.content.Context?) {
        appContext = context
    }

    /**
     * 设置类加载器（从 Hook 环境调用）
     */
    fun setClassLoader(classLoader: ClassLoader?) {
        appClassLoader = classLoader
    }

    /**
     * 检查监控线程是否正在运行
     */
    fun isMonitorRunning(): Boolean {
        return isMonitorRunning.get()
    }

    /**
     * 启动操作监控线程
     *
     * 在支付宝进程中启动后台线程，定期检查是否有导出请求
     * 如果有，立即执行导出操作
     */
    fun startOperationMonitor() {
        if (isMonitorRunning.compareAndSet(false, true)) {
            monitorThread = Thread {
                XposedBridge.log("[$TAG] 主题操作监控线程已启动")
                try {
                    while (isMonitorRunning.get()) {
                        try {
                            // 检查是否有操作请求
                            handleThemeOperations()

                            // 每2秒检查一次
                            Thread.sleep(2000)
                        } catch (e: InterruptedException) {
                            break
                        } catch (e: Exception) {
                            XposedBridge.log("[$TAG] 监控线程异常: ${e.message}")
                        }
                    }
                } finally {
                    XposedBridge.log("[$TAG] 主题操作监控线程已停止")
                }
            }.apply {
                name = "ThemeOperationMonitor"
                isDaemon = true
                start()
            }
        }
    }

    /**
     * 停止操作监控线程
     */
    fun stopOperationMonitor() {
        if (isMonitorRunning.compareAndSet(true, false)) {
            monitorThread?.interrupt()
            monitorThread = null
        }
    }

    /**
     * 获取当前用户ID
     *
     * 通过扫描 skin_center_dir 目录来确定用户ID
     */
    private fun getCurrentUserId(): String? {
        try {
            val skinCenterDir = File(INTERNAL_STORAGE_PATH)
            if (!skinCenterDir.exists() || !skinCenterDir.isDirectory) {
                return null
            }

            // 查找第一个数字开头的目录（用户ID通常是数字）
            val userDirs = skinCenterDir.listFiles { file ->
                file.isDirectory && file.name.matches(Regex("^\\d+$"))
            }

            if (userDirs != null && userDirs.isNotEmpty()) {
                val foundUserId = userDirs[0].name
                XposedBridge.log("[$TAG] 从文件系统扫描到用户ID: $foundUserId")
                return foundUserId
            }

            XposedBridge.log("[$TAG] 未找到用户ID目录")
            return null
        } catch (e: Exception) {
            XposedBridge.log("[$TAG] 获取用户ID失败: ${e.message}")
            return null
        }
    }

    /**
     * 处理主题操作
     *
     * 在应用启动时检查操作标记文件夹并执行相应操作
     */
    fun handleThemeOperations() {
        // 线程安全保护
        if (!isOperationRunning.compareAndSet(false, true)) {
            // 防抖：只在距离上次日志超过10秒时才打印
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSkipLogTime > 10000) {
                XposedBridge.log("[$TAG] 主题操作正在执行中，跳过")
                lastSkipLogTime = currentTime
            }
            return
        }

        try {
            val userId = getCurrentUserId()
            if (userId == null) {
                XposedBridge.log("[$TAG] 无法获取用户ID，跳过主题操作")
                return
            }

            val userThemeDir = File(INTERNAL_STORAGE_PATH, userId)
            if (!userThemeDir.exists()) {
                XposedBridge.log("[$TAG] 用户主题目录不存在: ${userThemeDir.absolutePath}")
                return
            }

            // 检查并执行操作
            checkAndExecuteOperations(userId, userThemeDir)
        } catch (e: Exception) {
            XposedBridge.log("[$TAG] 处理主题操作时出错: ${e.message}")
            e.printStackTrace()
        } finally {
            isOperationRunning.set(false)
        }
    }

    /**
     * 检查并执行操作
     */
    private fun checkAndExecuteOperations(userId: String, userThemeDir: File) {
        // 检查删除操作
        val deleteFile = File(DELETE_PATH)
        if (deleteFile.exists()) {
            executeDeleteOperation(userThemeDir)
            deleteFile.deleteRecursively()
        }

        // 检查导出操作
        val exportFile = File(EXPORT_PATH)
        if (exportFile.exists()) {
            executeExportOperation(userId, userThemeDir)
            exportFile.deleteRecursively()
        }

        // 检查更新操作
        val updateFile = File(UPDATE_PATH)
        if (updateFile.exists()) {
            executeUpdateOperation(userId, userThemeDir)
            updateFile.deleteRecursively()
        }
    }

    /**
     * 执行删除操作
     *
     * 删除支付宝内部的主题缓存
     */
    private fun executeDeleteOperation(userThemeDir: File) {
        try {
            if (userThemeDir.exists()) {
                userThemeDir.deleteRecursively()
                XposedBridge.log("[$TAG] 主题缓存已删除")
            }
        } catch (e: Exception) {
            XposedBridge.log("[$TAG] 主题删除失败: ${e.message}")
        }
    }

    /**
     * 执行导出操作
     *
     * 将支付宝内部的主题导出到SD卡（追加模式）
     * 每个主题包含自己的 ltp 资源，形成独立的主题包
     */
    private fun executeExportOperation(userId: String, userThemeDir: File) {
        try {
            if (!userThemeDir.exists()) {
                XposedBridge.log("[$TAG] 主题导出失败: 目录不存在")
                showToast("主题导出失败: 目录不存在")
                return
            }

            val exportDir = File(EXTERNAL_STORAGE_PATH, EXPORTED_THEMES_FOLDER)
            exportDir.mkdirs()
            val targetDir = File(exportDir, userId)
            targetDir.mkdirs()

            // 获取 ltp 源目录
            val ltpSourceDir = File(userThemeDir, "ltp")
            val hasLtp = ltpSourceDir.exists() && ltpSourceDir.isDirectory

            // 导出 theme 目录下的每个主题
            val themeSourceDir = File(userThemeDir, "theme")
            if (!themeSourceDir.exists() || !themeSourceDir.isDirectory) {
                XposedBridge.log("[$TAG] 主题导出失败: theme 目录不存在")
                showToast("主题导出失败: theme 目录不存在")
                return
            }

            val themeDirs = themeSourceDir.listFiles { file -> file.isDirectory }
            if (themeDirs == null || themeDirs.isEmpty()) {
                XposedBridge.log("[$TAG] 主题导出失败: 未找到主题目录")
                showToast("主题导出失败: 未找到主题目录")
                return
            }

            var exportedCount = 0
            themeDirs.forEach { themeDir ->
                try {
                    val themeId = themeDir.name
                    val themeTargetDir = File(targetDir, themeId)

                    // 复制主题资源
                    copyDirectory(themeDir, themeTargetDir)

                    // 将 ltp 复制到主题目录中
                    if (hasLtp) {
                        val ltpTargetDir = File(themeTargetDir, "ltp")
                        copyDirectory(ltpSourceDir, ltpTargetDir)
                    }

                    XposedBridge.log("[$TAG] 已导出主题: $themeId")
                    exportedCount++
                } catch (e: Exception) {
                    XposedBridge.log("[$TAG] 导出主题失败 (${themeDir.name}): ${e.message}")
                }
            }

            if (exportedCount > 0) {
                val message = "主题导出成功\n已导出 $exportedCount 个主题"
                XposedBridge.log("[$TAG] $message")
                showToast(message)
            } else {
                val message = "主题导出失败: 没有成功导出任何主题"
                XposedBridge.log("[$TAG] $message")
                showToast(message)
            }
        } catch (e: Exception) {
            val message = "主题导出失败: ${e.message}"
            XposedBridge.log("[$TAG] $message")
            showToast(message)
        }
    }

    /**
     * 执行更新操作
     *
     * 完整模拟支付宝内部切换主题的流程：
     * 1. 删除旧的自定义主题（可选）
     * 2. 导入新主题文件
     * 3. 更新 SharedPreferences
     * 4. 清除内存缓存
     * 5. 重新读取缓存
     * 6. 刷新 UI
     */
    private fun executeUpdateOperation(userId: String, userThemeDir: File) {
        try {
            val selectedThemeFile = File(EXTERNAL_STORAGE_PATH, SELECTED_THEME_FILE)
            if (!selectedThemeFile.exists()) {
                XposedBridge.log("[$TAG] 主题更新失败: 未选择主题")
                showToast("主题更新失败: 未选择主题")
                return
            }

            val selectedThemeId = selectedThemeFile.readText().trim()
            if (selectedThemeId.isEmpty()) {
                XposedBridge.log("[$TAG] 主题更新失败: 主题ID为空")
                showToast("主题更新失败: 主题ID为空")
                return
            }

            val sourceThemeDir = File(EXTERNAL_STORAGE_PATH, "$THEMES_FOLDER/$selectedThemeId")
            if (!sourceThemeDir.exists()) {
                XposedBridge.log("[$TAG] 主题更新失败: 主题不存在")
                showToast("主题更新失败: 主题不存在")
                return
            }

            // 创建theme基础目录
            val themeBaseDir = File(userThemeDir, "theme")
            if (!themeBaseDir.exists()) {
                themeBaseDir.mkdirs()
            }

            // 步骤1: 删除旧的自定义主题（有 theme_info.json 的主题）
            try {
                val existingCustomThemes = themeBaseDir.listFiles { file ->
                    file.isDirectory && File(file, "theme_info.json").exists()
                }
                existingCustomThemes?.forEach { oldTheme ->
                    if (oldTheme.name != selectedThemeId) {
                        oldTheme.deleteRecursively()
                        XposedBridge.log("[$TAG] 已删除旧的自定义主题: ${oldTheme.name}")
                    }
                }
            } catch (e: Exception) {
                XposedBridge.log("[$TAG] 删除旧主题失败: ${e.message}")
            }

            // 步骤2: 导入新主题文件
            val targetThemeDir = File(themeBaseDir, selectedThemeId)
            try {
                if (targetThemeDir.exists()) {
                    targetThemeDir.deleteRecursively()
                }

                copyDirectory(sourceThemeDir, targetThemeDir)
                XposedBridge.log("[$TAG] 已复制主题文件到: ${targetThemeDir.absolutePath}")

                // 读取并更新 theme_info.json
                val themeInfoFile = File(targetThemeDir, "theme_info.json")
                if (!themeInfoFile.exists()) {
                    XposedBridge.log("[$TAG] theme_info.json 不存在")
                    showToast("主题更新失败: theme_info.json 不存在")
                    return
                }

                // 手动解析 JSON，避免 Jackson 反序列化 Kotlin data class 的问题
                @Suppress("UNCHECKED_CAST")
                val json = JSON.parseObject(themeInfoFile.readText(), Map::class.java) as Map<String, Any>
                val themeInfo = ThemeCacheInfo.fromMap(json)
                val updatedThemeInfo = themeInfo.copy(
                    userId = userId,
                    cacheTime = System.currentTimeMillis() / 1000
                )
                themeInfoFile.writeText(JSON.toJSONString(updatedThemeInfo))

                XposedBridge.log("[$TAG] 主题信息:")
                XposedBridge.log("[$TAG]    名称: ${updatedThemeInfo.name}")
                XposedBridge.log("[$TAG]    主题ID: $selectedThemeId")
                XposedBridge.log("[$TAG]    MD5: ${updatedThemeInfo.md5}")

                // 步骤3: 更新 SharedPreferences
                updateSharedPreferences(userId, selectedThemeId, updatedThemeInfo)

                // 步骤4: 清除内存缓存
                clearMemoryCache()

                // 步骤5: 重新读取缓存
                reloadCache()

                // 步骤6: 刷新 UI
                notifySkinChanged()

                XposedBridge.log("[$TAG] 主题切换成功: ${updatedThemeInfo.name}")
                showToast("主题已切换: ${updatedThemeInfo.name}")

            } catch (e: Exception) {
                XposedBridge.log("[$TAG] 主题更新失败: ${e.message}")
                XposedBridge.log(e)
                showToast("主题更新失败: ${e.message}")
            }
        } catch (e: Exception) {
            XposedBridge.log("[$TAG] 主题更新失败: ${e.message}")
            XposedBridge.log(e)
            showToast("主题更新失败: ${e.message}")
        }
    }

    /**
     * 复制目录内容
     *
     * 将源目录的所有文件复制到目标目录（不包括子目录）
     */
    private fun copyDirectoryContents(source: File, destination: File) {
        if (!source.exists() || !source.isDirectory) {
            return
        }

        if (!destination.exists()) {
            destination.mkdirs()
        }

        source.listFiles()?.forEach { file ->
            if (file.isFile) {
                val destFile = File(destination, file.name)
                copyFile(file, destFile)
            }
        }
    }

    /**
     * 复制目录
     *
     * 递归复制整个目录及其内容
     */
    private fun copyDirectory(source: File, destination: File) {
        if (!source.exists()) return

        if (source.isDirectory) {
            // 创建目标目录
            if (!destination.exists()) {
                destination.mkdirs()
            }

            // 复制所有子文件和子目录
            source.listFiles()?.forEach { file ->
                val destFile = File(destination, file.name)
                if (file.isDirectory) {
                    copyDirectory(file, destFile)
                } else {
                    copyFile(file, destFile)
                }
            }
        } else {
            // 复制单个文件
            copyFile(source, destination)
        }
    }

    /**
     * 复制文件
     *
     * 将源文件复制到目标位置
     * 使用缓冲区优化大文件复制性能
     */
    private fun copyFile(source: File, destination: File) {
        source.inputStream().use { input ->
            destination.outputStream().use { output ->
                // 使用 32KB 缓冲区，提升大文件复制性能
                input.copyTo(output, bufferSize = 32 * 1024)
            }
        }
    }

    /**
     * 显示 Toast 提示
     *
     * @param message 提示消息
     */
    private fun showToast(message: String) {
        try {
            val context = appContext
            if (context != null) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            XposedBridge.log("[$TAG] Toast 显示失败: ${e.message}")
        }
    }

    /**
     * 更新 SharedPreferences
     *
     * 将主题信息写入 SharedPreferences，指向新主题
     */
    private fun updateSharedPreferences(userId: String, themeId: String, themeInfo: ThemeCacheInfo) {
        try {
            val context = appContext
            if (context == null) {
                XposedBridge.log("[$TAG] 无法获取 Context")
                return
            }

            // 构造缓存信息
            val cacheInfo = mapOf(
                "theme" to mapOf(
                    "usageScene" to "theme",
                    "skinId" to themeInfo.skinId,
                    "userSkinId" to themeId,
                    "userId" to userId,
                    "md5" to themeInfo.md5,
                    "appSquareMd5" to themeInfo.md5,
                    "cacheTime" to themeInfo.cacheTime,
                    "versionLimit" to themeInfo.versionLimit,
                    "isDiySkin" to false,
                    "name" to themeInfo.name,
                    "expireDate" to themeInfo.expireDate,
                    "skinType" to themeInfo.skinType,
                    "materialId" to "",
                    "diyExpiredTime" to 0L
                )
            )

            // 序列化为 JSON
            val json = JSON.toJSONString(cacheInfo)

            // 使用 Android 标准 SharedPreferences API
            val prefs = context.getSharedPreferences("prefs_skincenter_file", android.content.Context.MODE_PRIVATE)
            prefs.edit()
                .putString("cached_skin_info_v2#$userId", json)
                .apply()

            XposedBridge.log("[$TAG] 已更新 SharedPreferences")
            XposedBridge.log("[$TAG]    userSkinId: $themeId")
            XposedBridge.log("[$TAG]    key: cached_skin_info_v2#$userId")
        } catch (e: Exception) {
            XposedBridge.log("[$TAG] 更新 SharedPreferences 失败: ${e.message}")
            XposedBridge.log(e)
        }
    }

    /**
     * 清除内存缓存
     *
     * 清除 SCInnerManager 的内存缓存 this.g 中的 theme 条目
     */
    private fun clearMemoryCache() {
        try {
            val classLoader = appClassLoader
            if (classLoader == null) {
                XposedBridge.log("[$TAG] 无法获取 ClassLoader")
                return
            }

            val scInnerManagerClass = classLoader.loadClass(
                "com.alipay.mobile.skincenter.manage.SCInnerManager"
            )

            // 获取单例实例（方法名是 m()，不是 getInstance()）
            val getInstanceMethod = scInnerManagerClass.getDeclaredMethod("m")
            val instance = getInstanceMethod.invoke(null)

            // 获取内存缓存 Map (字段名: g)
            val gField = scInnerManagerClass.getDeclaredField("g")
            gField.isAccessible = true
            val cacheMap = gField.get(instance) as? MutableMap<String, Any>

            if (cacheMap != null) {
                // 清除 theme 缓存
                cacheMap.remove("theme")
                XposedBridge.log("[$TAG] 已清除内存缓存")
            } else {
                XposedBridge.log("[$TAG] 无法获取内存缓存 Map")
            }
        } catch (e: Exception) {
            XposedBridge.log("[$TAG] 清除内存缓存失败: ${e.message}")
            XposedBridge.log(e)
        }
    }

    /**
     * 重新读取缓存
     *
     * 调用 SCInnerManager.K() 方法，从 SharedPreferences 重新加载缓存
     */
    private fun reloadCache() {
        try {
            val classLoader = appClassLoader
            if (classLoader == null) {
                XposedBridge.log("[$TAG] 无法获取 ClassLoader")
                return
            }

            val scInnerManagerClass = classLoader.loadClass(
                "com.alipay.mobile.skincenter.manage.SCInnerManager"
            )

            // 获取单例实例（方法名是 m()，不是 getInstance()）
            val getInstanceMethod = scInnerManagerClass.getDeclaredMethod("m")
            val instance = getInstanceMethod.invoke(null)

            // 调用 K() 方法重新读取缓存
            val kMethod = scInnerManagerClass.getDeclaredMethod("K")
            kMethod.invoke(instance)

            XposedBridge.log("[$TAG] 已重新读取缓存")
        } catch (e: Exception) {
            XposedBridge.log("[$TAG] 重新读取缓存失败: ${e.message}")
            XposedBridge.log(e)
        }
    }

    /**
     * 通知皮肤更换
     *
     * 调用 AntSkinRenderManager.notifySkinChanged() 实现动态刷新
     */
    private fun notifySkinChanged() {
        try {
            val classLoader = appClassLoader
            if (classLoader == null) {
                XposedBridge.log("[$TAG] 无法获取 ClassLoader")
                return
            }

            val antSkinRenderManagerClass = classLoader.loadClass("com.alipay.mobile.skincenter.manage.AntSkinRenderManager")
            val notifySkinChangedMethod = antSkinRenderManagerClass.getDeclaredMethod("notifySkinChanged")
            notifySkinChangedMethod.invoke(null)

            XposedBridge.log("[$TAG] 已通知 UI 刷新")
        } catch (e: Exception) {
            XposedBridge.log("[$TAG] 通知 UI 刷新失败: ${e.message}")
            XposedBridge.log(e)
        }
    }

    /**
     * 立即导出主题
     *
     * 直接执行导出操作，不需要通过文件夹标记
     * 供 UI 层直接调用
     */
    fun exportThemeNow(): Pair<Boolean, String> {
        try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Pair(false, "无法获取用户ID")
            }

            val userThemeDir = File(INTERNAL_STORAGE_PATH, userId)
            if (!userThemeDir.exists()) {
                return Pair(false, "用户主题目录不存在")
            }

            val exportDir = File(EXTERNAL_STORAGE_PATH, EXPORTED_THEMES_FOLDER)
            exportDir.mkdirs()
            val targetDir = File(exportDir, userId)
            targetDir.mkdirs()

            // 获取 ltp 源目录
            val ltpSourceDir = File(userThemeDir, "ltp")
            val hasLtp = ltpSourceDir.exists() && ltpSourceDir.isDirectory

            // 导出 theme 目录下的每个主题
            val themeSourceDir = File(userThemeDir, "theme")
            if (!themeSourceDir.exists() || !themeSourceDir.isDirectory) {
                return Pair(false, "theme 目录不存在")
            }

            val themeDirs = themeSourceDir.listFiles { file -> file.isDirectory }
            if (themeDirs == null || themeDirs.isEmpty()) {
                return Pair(false, "未找到主题目录")
            }

            var exportedCount = 0
            themeDirs.forEach { themeDir ->
                try {
                    val themeId = themeDir.name
                    val themeTargetDir = File(targetDir, themeId)

                    // 复制主题资源
                    copyDirectory(themeDir, themeTargetDir)

                    // 将 ltp 复制到主题目录中
                    if (hasLtp) {
                        val ltpTargetDir = File(themeTargetDir, "ltp")
                        copyDirectory(ltpSourceDir, ltpTargetDir)
                    }

                    XposedBridge.log("[$TAG] 已导出主题: $themeId")
                    exportedCount++
                } catch (e: Exception) {
                    XposedBridge.log("[$TAG] 导出主题失败 (${themeDir.name}): ${e.message}")
                }
            }

            if (exportedCount > 0) {
                XposedBridge.log("[$TAG] 主题导出成功，已导出 $exportedCount 个主题")
                return Pair(true, "主题导出成功")
            } else {
                return Pair(false, "没有成功导出任何主题")
            }
        } catch (e: Exception) {
            XposedBridge.log("[$TAG] 主题导出失败: ${e.message}")
            return Pair(false, "主题导出失败: ${e.message}")
        }
    }

}
