package im.hoho.alipayInstallB.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import im.hoho.alipayInstallB.BuildConfig
import im.hoho.alipayInstallB.ui.*

/**
 * 使用说明页面
 *
 * 介绍模块功能和操作方法
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("使用说明") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = AppTextPrimary,
                    navigationIconContentColor = AppTextPrimary
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 项目介绍
                ProjectIntroCard()

                // 环境要求
                RequirementsCard()

                // 皮肤功能说明
                SkinGuideCard()

                // 主题功能说明
                ThemeGuideCard()

                // 常见问题
                FaqCard()

                // 注意事项
                NoticeCard()

                // 存储路径说明
                StoragePathCard()

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * 项目介绍卡片
 */
@Composable
private fun ProjectIntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapeLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBannerGradient)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "美化模块",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "专业级 Xposed 框架模块，为支付宝提供自定义付款码皮肤和全局主题美化功能。" +
                            "通过 LSPosed 框架加载，所有修改仅在本地生效，不影响账号安全。",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "当前版本: ${BuildConfig.VERSION_NAME}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 环境要求卡片
 */
@Composable
private fun RequirementsCard() {
    GuideSection(
        title = "环境要求",
        icon = Icons.Default.Build
    ) {
        GuideItem(
            step = "1",
            title = "安装 LSPosed 框架",
            description = "需要 Root 权限，通过 Magisk 安装 LSPosed 模块"
        )
        GuideItem(
            step = "2",
            title = "启用本模块",
            description = "在 LSPosed 管理器中勾选本模块，作用域选择「支付宝」"
        )
        GuideItem(
            step = "3",
            title = "重启设备",
            description = "首次启用模块后需要重启设备使模块生效"
        )
        GuideItem(
            step = "4",
            title = "授予存储权限",
            description = "打开本应用，授予存储权限以便读写皮肤和主题文件"
        )
    }
}

/**
 * 皮肤功能说明卡片
 */
@Composable
private fun SkinGuideCard() {
    GuideSection(
        title = "皮肤设置 - 操作指南",
        icon = Icons.Default.Face
    ) {
        GuideItem(
            step = "1",
            title = "下载资源包",
            description = "首次使用请点击「下载资源包」按钮，从 GitHub 下载皮肤模板文件到本地"
        )
        GuideItem(
            step = "2",
            title = "启用自定义皮肤",
            description = "打开「启用自定义皮肤」开关，激活皮肤替换功能"
        )
        GuideItem(
            step = "3",
            title = "选择会员等级",
            description = "在会员等级下拉框中选择想要显示的等级（普通/黄金/铂金/钻石）"
        )
        GuideItem(
            step = "4",
            title = "选择或导入皮肤",
            description = "从皮肤列表中选择一个皮肤，或通过「导入ZIP」「导入目录」按钮导入自定义皮肤包"
        )
        GuideItem(
            step = "5",
            title = "更新皮肤缓存",
            description = "点击「更新皮肤缓存」按钮，然后打开支付宝进入付款码页面即可看到效果"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 皮肤操作按钮说明
        Text(
            text = "操作按钮说明",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTextPrimary,
            modifier = Modifier.padding(top = 4.dp)
        )
        BulletItem("导出现有皮肤：从支付宝导出当前使用的皮肤资源到 SD 卡")
        BulletItem("删除皮肤缓存：清除支付宝的皮肤缓存，强制重新加载")
        BulletItem("更新皮肤缓存：将选中的皮肤推送到支付宝缓存目录")
    }
}

/**
 * 主题功能说明卡片
 */
@Composable
private fun ThemeGuideCard() {
    GuideSection(
        title = "主题中心 - 操作指南",
        icon = Icons.Default.Settings
    ) {
        GuideItem(
            step = "1",
            title = "导出现有主题",
            description = "点击「导出主题」按钮，将支付宝当前使用的主题导出到 SD 卡"
        )
        GuideItem(
            step = "2",
            title = "导入自定义主题",
            description = "通过「导入主题包 (ZIP)」或「导入主题目录」按钮导入新主题"
        )
        GuideItem(
            step = "3",
            title = "选择主题",
            description = "在主题列表中点击想要使用的主题卡片进行选择"
        )
        GuideItem(
            step = "4",
            title = "更新主题缓存",
            description = "点击「更新主题缓存」按钮，将选中的主题推送到支付宝"
        )
        GuideItem(
            step = "5",
            title = "重启支付宝",
            description = "完全关闭并重新打开支付宝，即可看到新主题效果"
        )
    }
}

/**
 * 常见问题卡片
 */
@Composable
private fun FaqCard() {
    GuideSection(
        title = "常见问题",
        icon = Icons.Default.Info
    ) {
        FaqItem(
            question = "皮肤/主题没有生效？",
            answer = "请确认：1) 模块已在 LSPosed 中启用并勾选支付宝；2) 已点击「更新」按钮；3) 已重新打开支付宝付款码页面"
        )
        FaqItem(
            question = "支付宝更新后皮肤失效？",
            answer = "支付宝更新或清除缓存后，需要重新点击「更新皮肤缓存」按钮刷新"
        )
        FaqItem(
            question = "导入皮肤/主题失败？",
            answer = "请确认 ZIP 文件格式正确，皮肤包需要包含 background_2x1.png 等图片文件，主题包需要包含 meta.json 文件"
        )
        FaqItem(
            question = "多个皮肤如何切换？",
            answer = "当存在多个皮肤目录时，系统会随机选择其中一个显示。如需固定使用某个皮肤，请删除其他皮肤目录后点击「更新」"
        )
    }
}

/**
 * 注意事项卡片
 */
@Composable
private fun NoticeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapeLarge,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AppWarning,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "注意事项",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTextPrimary
                )
            }

            BulletItem("本模块仅修改本地显示效果，不会修改账号数据或云端设置")
            BulletItem("所有操作均在本地设备完成，不会上传任何用户信息")
            BulletItem("系统仅在展示支付二维码界面时响应皮肤配置变更")
            BulletItem("本模块仅通过 GitHub 官方渠道发布，其他途径获取可能存在安全风险")
        }
    }
}

/**
 * 存储路径说明卡片
 */
@Composable
private fun StoragePathCard() {
    GuideSection(
        title = "文件存储路径",
        icon = Icons.Default.Home
    ) {
        PathItem(
            label = "皮肤存储路径",
            path = "/storage/emulated/0/Android/media/\ncom.eg.android.AlipayGphone/\n000_HOHO_ALIPAY_SKIN"
        )
        PathItem(
            label = "主题存储路径",
            path = "/storage/emulated/0/Android/media/\ncom.eg.android.AlipayGphone/\n000_HOHO_THEME_CENTER/themes"
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "皮肤目录结构说明",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTextPrimary,
            modifier = Modifier.padding(top = 4.dp)
        )
        BulletItem("actived - 启用自定义皮肤系统（目录或文件）")
        BulletItem("update - 触发系统更新皮肤缓存（操作后自动删除）")
        BulletItem("delete - 触发系统清除皮肤缓存（操作后自动删除）")
        BulletItem("export - 导出系统内置皮肤资源（操作后自动删除）")
        BulletItem("[自定义目录名] - 个性化皮肤资源存储位置")
    }
}

// ==================== 通用组件 ====================

/**
 * 指南章节卡片
 */
@Composable
private fun GuideSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapeLarge,
        colors = CardDefaults.cardColors(containerColor = AppCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTextPrimary
                )
            }

            // 内容
            content()
        }
    }
}

/**
 * 步骤项
 */
@Composable
private fun GuideItem(
    step: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 步骤编号
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(AppShapeSmall)
                .background(AppPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // 步骤内容
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = AppTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = AppTextSecondary,
                lineHeight = 19.sp
            )
        }
    }
}

/**
 * 常见问题项
 */
@Composable
private fun FaqItem(
    question: String,
    answer: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapeMedium)
            .background(AppContainerBackground)
            .padding(14.dp)
    ) {
        Text(
            text = "Q: $question",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "A: $answer",
            fontSize = 13.sp,
            color = AppTextSecondary,
            lineHeight = 19.sp
        )
    }
}

/**
 * 路径项
 */
@Composable
private fun PathItem(
    label: String,
    path: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapeMedium)
            .background(AppContainerBackground)
            .padding(14.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = AppTextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = path,
            fontSize = 12.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            color = AppTextSecondary,
            lineHeight = 18.sp
        )
    }
}

/**
 * 列表项（圆点前缀）
 */
@Composable
private fun BulletItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "•",
            fontSize = 13.sp,
            color = AppPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = AppTextSecondary,
            lineHeight = 19.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
