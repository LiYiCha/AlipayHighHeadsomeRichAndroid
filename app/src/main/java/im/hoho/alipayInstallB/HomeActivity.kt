package im.hoho.alipayInstallB

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import im.hoho.alipayInstallB.guide.GuideActivity
import im.hoho.alipayInstallB.skin.SkinActivity
import im.hoho.alipayInstallB.theme.ThemeActivity
import im.hoho.alipayInstallB.ui.*

/**
 * 现代化主页
 *
 * 整合皮肤和主题功能的入口页面
 */
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                HomeScreen(
                    onSkinClick = {
                        startActivity(Intent(this, SkinActivity::class.java))
                    },
                    onThemeClick = {
                        startActivity(Intent(this, ThemeActivity::class.java))
                    },
                    onGuideClick = {
                        startActivity(Intent(this, GuideActivity::class.java))
                    },
                    onGithubClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = "https://github.com/LiYiCha/AlipayHighHeadsomeRichAndroid".toUri()
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSkinClick: () -> Unit,
    onThemeClick: () -> Unit,
    onGuideClick: () -> Unit,
    onGithubClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackgroundGradient)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 欢迎卡片
                WelcomeCard()

                // 功能卡片
                FeatureCard(
                    title = "皮肤设置",
                    description = "自定义付款码皮肤，让你的付款码与众不同",
                    icon = Icons.Default.Face,
                    gradient = AppSkinCardGradient,
                    onClick = onSkinClick
                )

                FeatureCard(
                    title = "主题中心",
                    description = "管理你的主题，打造属于你的个性化界面",
                    icon = Icons.Default.Settings,
                    gradient = AppThemeCardGradient,
                    onClick = onThemeClick
                )

                FeatureCard(
                    title = "使用说明",
                    description = "了解模块功能与操作方法，快速上手",
                    icon = Icons.Default.Info,
                    gradient = AppGuideCardGradient,
                    onClick = onGuideClick
                )

                Spacer(modifier = Modifier.weight(1f))

                // 底部信息
                BottomInfo(
                    onGithubClick = onGithubClick
                )
            }
        }
    }
}

@Composable
fun WelcomeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = AppShapeLarge,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBannerGradient)
                .padding(28.dp)
        ) {
            Column {
                Text(
                    text = "欢迎使用",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "美化模块",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AppShapeLarge,
        colors = CardDefaults.cardColors(
            containerColor = AppCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标区域
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(AppShapeMedium)
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 文字区域
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = AppTextHint,
                    lineHeight = 18.sp
                )
            }

            // 箭头
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "进入",
                tint = AppTextHint
            )
        }
    }
}

@Composable
fun BottomInfo(
    onGithubClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // GitHub 链接
        Row(
            modifier = Modifier
                .clip(AppShapeMedium)
                .clickable(onClick = onGithubClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "GitHub",
                modifier = Modifier.size(16.dp),
                tint = AppPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "GitHub 开源项目",
                fontSize = 13.sp,
                color = AppPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
