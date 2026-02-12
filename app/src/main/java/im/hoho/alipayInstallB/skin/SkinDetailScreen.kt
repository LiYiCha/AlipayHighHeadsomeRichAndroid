package im.hoho.alipayInstallB.skin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import im.hoho.alipayInstallB.ui.*

/**
 * 皮肤详情屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinDetailScreen(
    viewModel: SkinDetailViewModel,
    onBack: () -> Unit,
    onReplaceImage: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("皮肤详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = AppTextPrimary,
                    navigationIconContentColor = AppTextPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppBackgroundGradient)
        ) {
            if (state.error != null) {
                // 错误状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                // 正常显示
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 基本信息卡片
                    item {
                        SkinInfoCard(
                            skinName = state.skinName,
                            description = state.description,
                            themeColor = state.themeColor
                        )
                    }

                    // 图片列表
                    items(state.images) { image ->
                        ImageItemCard(
                            image = image,
                            onReplace = { onReplaceImage(image.fileName) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 皮肤信息卡片
 */
@Composable
private fun SkinInfoCard(
    skinName: String,
    description: String,
    themeColor: String
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
                .padding(20.dp)
        ) {
            Text(
                text = skinName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppTextPrimary
            )

            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = AppTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 主题色
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "主题色:",
                    fontSize = 14.sp,
                    color = AppTextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(AppShapeSmall)
                        .background(parseColor(themeColor))
                )
                Text(
                    text = themeColor,
                    fontSize = 14.sp,
                    color = AppTextHint
                )
            }
        }
    }
}

/**
 * 图片项卡片
 */
@Composable
private fun ImageItemCard(
    image: SkinImageInfo,
    onReplace: () -> Unit
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
                .padding(16.dp)
        ) {
            // 标题和替换按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = image.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppTextPrimary
                    )
                    Text(
                        text = image.fileName,
                        fontSize = 12.sp,
                        color = AppTextHint
                    )
                }

                // 始终显示替换按钮，无论图片是否存在
                FilledTonalButton(
                    onClick = onReplace,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AppPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (image.exists) "替换" else "添加", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 图片预览
            if (image.exists) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(AppShapeMedium)
                        .background(AppContainerBackground),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = image.path,
                        contentDescription = image.displayName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(AppShapeMedium)
                        .background(AppContainerBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = AppTextHint
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "图片不存在",
                            fontSize = 13.sp,
                            color = AppTextHint
                        )
                    }
                }
            }
        }
    }
}

/**
 * 解析颜色字符串
 */
private fun parseColor(colorString: String): Color {
    return try {
        val cleanColor = colorString.removePrefix("#")
        val colorInt = cleanColor.toLong(16)
        Color(colorInt or 0xFF000000)
    } catch (e: Exception) {
        Color.Gray
    }
}
