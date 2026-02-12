package im.hoho.alipayInstallB.skin

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import im.hoho.alipayInstallB.ui.AppTheme

/**
 * 皮肤详情 Activity
 *
 * 显示单个皮肤的详细信息，包括所有图片和配置
 * 支持查看和替换皮肤中的图片
 */
class SkinDetailActivity : ComponentActivity() {

    private lateinit var viewModel: SkinDetailViewModel
    private var currentReplacingImage: String? = null

    // 图片选择器启动器
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val imageName = currentReplacingImage
            if (imageName != null) {
                // 替换图片
                viewModel.replaceImage(imageName, it) { success, message ->
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            if (success) "替换成功" else "替换失败：$message",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (success) {
                            // 刷新皮肤详情
                            viewModel.loadSkinDetail()
                        }
                    }
                }
            }
            currentReplacingImage = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取皮肤名称
        val skinName = intent.getStringExtra("skinName") ?: run {
            Toast.makeText(this, "未指定皮肤名称", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 初始化 ViewModel
        viewModel = SkinDetailViewModel(this, skinName)

        // 设置 Compose UI
        setContent {
            AppTheme {
                SkinDetailScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onReplaceImage = { imageName ->
                        startImagePicker(imageName)
                    }
                )
            }
        }
    }

    /**
     * 启动图片选择器
     *
     * @param imageName 要替换的图片名称
     */
    private fun startImagePicker(imageName: String) {
        currentReplacingImage = imageName
        imagePickerLauncher.launch("image/*")
    }
}
