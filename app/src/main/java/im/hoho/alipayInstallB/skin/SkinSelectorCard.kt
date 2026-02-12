package im.hoho.alipayInstallB.skin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
 * 皮肤选择卡片
 *
 * 显示可用皮肤列表，支持预览和选择
 *
 * @param availableSkins 可用皮肤列表
 * @param selectedSkinName 当前选中的皮肤名称
 * @param onSkinSelected 皮肤选择回调
 * @param onRefresh 刷新皮肤列表回调
 * @param onImport 导入皮肤ZIP回调
 * @param onImportDirectory 导入皮肤目录回调
 * @param onViewDetail 查看皮肤详情回调
 */
@Composable
fun SkinSelectorCard(
    availableSkins: List<SkinInfo>,
    selectedSkinName: String?,
    onSkinSelected: (String) -> Unit,
    onRefresh: () -> Unit,
    onImport: () -> Unit,
    onImportDirectory: () -> Unit,
    onViewDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapeLarge,
        colors = CardDefaults.cardColors(
            containerColor = AppCardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "选择皮肤",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTextPrimary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 导入ZIP按钮
                    FilledTonalButton(
                        onClick = onImport,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AppPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("导入ZIP", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }

                    // 导入目录按钮
                    FilledTonalButton(
                        onClick = onImportDirectory,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AppAccent,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("导入目录", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }

                    TextButton(
                        onClick = onRefresh,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = AppPrimary
                        )
                    ) {
                        Text("刷新", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 皮肤列表容器
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapeMedium)
                    .background(AppContainerBackground)
                    .padding(12.dp)
            ) {
                if (availableSkins.isEmpty()) {
                    // 空状态
                    EmptySkinState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(availableSkins) { skin ->
                            SkinItem(
                                skin = skin,
                                isSelected = skin.name == selectedSkinName,
                                onClick = { onSkinSelected(skin.name) },
                                onViewDetail = { onViewDetail(skin.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 单个皮肤项
 */
@Composable
private fun SkinItem(
    skin: SkinInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    onViewDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        AppPrimary
    } else {
        AppDivider
    }

    val backgroundColor = if (isSelected) {
        Color(0xFFE8F8F5)
    } else {
        AppCardBackground
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapeMedium,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 3.dp else 0.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 预览图
                SkinPreviewImage(
                    previewPath = skin.previewImagePath,
                    modifier = Modifier
                        .size(80.dp, 50.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                // 皮肤信息
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = skin.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    if (skin.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = skin.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    // 主题色指示器
                    if (skin.themeColor.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        ThemeColorIndicator(themeColor = skin.themeColor)
                    }
                }

                // 选中指示器
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "已选中",
                        tint = AppPrimary,
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }

            // 查看详情按钮
            TextButton(
                onClick = onViewDetail,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AppPrimary
                )
            ) {
                Text(
                    text = "查看详情",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

        }
    }
}

/**
 * 皮肤预览图
 */
@Composable
private fun SkinPreviewImage(
    previewPath: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(AppPlaceholderGradient),
        contentAlignment = Alignment.Center
    ) {
        if (previewPath != null) {
            AsyncImage(
                model = previewPath,
                contentDescription = "皮肤预览",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "无预览图",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * 主题色指示器
 */
@Composable
private fun ThemeColorIndicator(
    themeColor: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(parseColor(themeColor))
        )
        Text(
            text = themeColor,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * 空状态显示
 */
@Composable
private fun EmptySkinState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "暂无可用皮肤",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "请先下载资源包或更新皮肤缓存",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
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
