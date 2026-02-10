package im.hoho.alipayInstallB.theme.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import im.hoho.alipayInstallB.theme.ThemeInfo
import im.hoho.alipayInstallB.theme.ThemeViewModel

/**
 * 主题详情页面
 *
 * 显示主题的详细信息，包括预览图、名称、ID和资源列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDetailScreen(
    themeInfo: ThemeInfo,
    viewModel: ThemeViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // 处理系统返回按钮
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("主题详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF5F7FA),
                            Color(0xFFE8EAF6)
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                // 预览图
                item {
                    PreviewImageCard(themeInfo)
                }

                // 主题信息
                item {
                    ThemeInfoCard(themeInfo)
                }

                // 资源列表
                item {
                    ResourceListCard(themeInfo)
                }

                // 操作按钮
                item {
                    ActionButtons(
                        themeInfo = themeInfo,
                        onApply = {
                            viewModel.selectTheme(themeInfo.themeId)
                            Toast.makeText(context, "已选择主题: ${themeInfo.name}", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            // TODO: 实现删除功能，仅移除记录不删除真正的文件
                            Toast.makeText(context, "删除功能待实现", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

/**
 * 预览图卡片
 */
@Composable
private fun PreviewImageCard(themeInfo: ThemeInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        if (themeInfo.previewImagePath != null) {
            Image(
                painter = rememberAsyncImagePainter(themeInfo.previewImagePath),
                contentDescription = "主题预览",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE1D9D2),
                                Color(0xFFD2FFFB)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无预览图",
                    fontSize = 16.sp,
                    color = Color(0xFF424242)
                )
            }
        }
    }
}

/**
 * 主题信息卡片
 */
@Composable
private fun ThemeInfoCard(themeInfo: ThemeInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 主题名称
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "主题名称: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF616161)
                )
                Text(
                    text = themeInfo.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF131313)
                )
            }

            // 主题ID
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "主题ID: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF616161)
                )
                Text(
                    text = themeInfo.description.removePrefix("主题ID: ").removePrefix("文件夹: "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF424242)
                )
            }
        }
    }
}

/**
 * 资源列表卡片
 */
@Composable
private fun ResourceListCard(themeInfo: ThemeInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📋 主题包含的资源",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF131313)
            )

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFFE0E0E0)
            )

            // 资源项列表
            val resources = listOf(
                "• TabBar 主题色",
                "• 首页/理财/生活/消息/我的 图标",
                "• 导航栏背景图",
                "• Lottie 动画效果",
                "• 共 50+ 个资源项"
            )

            resources.forEach { resource ->
                Text(
                    text = resource,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF424242),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * 操作按钮
 */
@Composable
private fun ActionButtons(
    themeInfo: ThemeInfo,
    onApply: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 应用主题按钮
        Button(
            onClick = onApply,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00BCD4)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("应用此主题")
        }

        // 删除主题按钮
        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFE53935)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("删除主题")
        }
    }
}
