package im.hoho.alipayInstallB.theme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import im.hoho.alipayInstallB.theme.ui.ThemeScreen
import im.hoho.alipayInstallB.ui.AppTheme

/**
 * 主题中心 Activity
 *
 * 提供主题管理界面
 */
class ThemeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 创建 Repository 和 ViewModel
        val repository = ThemeRepository(this)
        val viewModel = ThemeViewModel(repository)

        setContent {
            AppTheme {
                ThemeScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}
