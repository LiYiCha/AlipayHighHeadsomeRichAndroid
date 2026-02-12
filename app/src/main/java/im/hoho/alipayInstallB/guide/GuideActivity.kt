package im.hoho.alipayInstallB.guide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import im.hoho.alipayInstallB.ui.AppTheme

/**
 * 使用说明 Activity
 *
 * 提供模块功能介绍和操作指南
 */
class GuideActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                GuideScreen(onBack = { finish() })
            }
        }
    }
}
