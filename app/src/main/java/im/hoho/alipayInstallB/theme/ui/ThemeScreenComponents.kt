package im.hoho.alipayInstallB.theme.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import im.hoho.alipayInstallB.theme.ThemeInfo
import im.hoho.alipayInstallB.theme.ThemeOperation
import im.hoho.alipayInstallB.ui.*

/**
 * 操作卡片
 */
@Composable
fun OperationsCard(
    operationStates: Map<ThemeOperation, Boolean>,
    onExecute: (ThemeOperation) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapeLarge,
        colors = CardDefaults.cardColors(containerColor = AppCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "主题操作",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTextPrimary
            )

            // 导出操作
            OperationButton(
                operation = ThemeOperation.EXPORT,
                onExecute = { onExecute(ThemeOperation.EXPORT) }
            )

            // 删除操作
            OperationButton(
                operation = ThemeOperation.DELETE,
                onExecute = { onExecute(ThemeOperation.DELETE) }
            )

            // 更新操作
            OperationButton(
                operation = ThemeOperation.UPDATE,
                onExecute = { onExecute(ThemeOperation.UPDATE) }
            )

            Text(
                text = "注意：操作将在下次打开付款码时自动执行",
                style = MaterialTheme.typography.bodySmall,
                color = AppTextHint
            )
        }
    }
}

/**
 * 操作按钮
 */
@Composable
private fun OperationButton(
    operation: ThemeOperation,
    onExecute: () -> Unit
) {
    Button(
        onClick = onExecute,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = AppShapeMedium,
        colors = ButtonDefaults.buttonColors(
            containerColor = when (operation) {
                ThemeOperation.EXPORT -> AppInfo
                ThemeOperation.DELETE -> AppError
                ThemeOperation.UPDATE -> AppSuccess
            },
            contentColor = Color.White
        )
    ) {
        Text(operation.displayName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

