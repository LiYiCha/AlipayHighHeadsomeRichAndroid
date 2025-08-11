package im.hoho.alipayInstallB;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 主题管理Activity
 * 用于选择和应用不同的主题
 */
public class ThemeManagerActivity extends Activity {
    
    private static final String EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory() +
            "/Android/media/com.eg.android.AlipayGphone/000_HOHO_ALIPAY_SKIN";
    
    private static final String THEMES_PATH = EXTERNAL_STORAGE_PATH + "/themes";
    // 修改为正确的支付宝主题路径
    private static final String ALIPAY_THEME_BASE_PATH = "/data/data/com.eg.android.AlipayGphone/files/skin_center_dir";
    
    private ListView listViewThemes;
    private Button btnApplyTheme;
    private ArrayAdapter<String> adapter;
    private List<String> themeList;
    private String selectedTheme = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_manager);
        
        initViews();
        loadThemes();
        setupListeners();
    }
    
    private void initViews() {
        listViewThemes = findViewById(R.id.listViewThemes);
        btnApplyTheme = findViewById(R.id.btnApplyTheme);
        
        themeList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, themeList);
        listViewThemes.setAdapter(adapter);
        listViewThemes.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }
    
    private void loadThemes() {
        themeList.clear();
        
        File themesDir = new File(THEMES_PATH);
        if (!themesDir.exists()) {
            themesDir.mkdirs();
            // 将默认主题包解压到主题目录
            extractDefaultTheme();
        }
        
        File[] themeFolders = themesDir.listFiles();
        if (themeFolders != null) {
            for (File themeFolder : themeFolders) {
                if (themeFolder.isDirectory()) {
                    themeList.add(themeFolder.getName());
                }
            }
        }
        
        adapter.notifyDataSetChanged();
    }
    
    private void extractDefaultTheme() {
        try {
            // 将assets中的default.zip复制到themes目录
            File defaultThemeZip = new File(getFilesDir(), "default.zip");
            if (!defaultThemeZip.exists()) {
                // 从assets复制
                android.content.res.AssetManager assetManager = getAssets();
                java.io.InputStream in = assetManager.open("default.zip");
                java.io.FileOutputStream out = new java.io.FileOutputStream(defaultThemeZip);
                
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.close();
            }
            
            // 解压到themes目录
            ZipFile zipFile = new ZipFile(defaultThemeZip);
            File themeDir = new File(THEMES_PATH, "Default");
            if (!themeDir.exists()) {
                themeDir.mkdirs();
            }
            zipFile.extractAll(themeDir.getAbsolutePath());
            
            // 删除临时文件
            defaultThemeZip.delete();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "解压默认主题失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupListeners() {
        listViewThemes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedTheme = themeList.get(position);
                for (int i = 0; i < listViewThemes.getChildCount(); i++) {
                    listViewThemes.setItemChecked(i, i == position);
                }
            }
        });
        
        btnApplyTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedTheme.isEmpty()) {
                    Toast.makeText(ThemeManagerActivity.this, "请先选择一个主题", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                applyTheme(selectedTheme);
            }
        });
    }
    
    private void applyTheme(String themeName) {
        new AlertDialog.Builder(this)
                .setTitle("应用主题")
                .setMessage("确定要应用主题 \"" + themeName + "\" 吗？这将替换当前的皮肤。")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doApplyTheme(themeName);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    private void doApplyTheme(String themeName) {
        try {
            // 获取用户ID（从用户信息文件中读取）
            String userId = getUserId();
            if (userId == null) {
                Toast.makeText(this, "无法获取用户ID，请先打开支付宝并登录", Toast.LENGTH_LONG).show();
                return;
            }
            
            // 构建目标主题路径
            String targetThemePath = ALIPAY_THEME_BASE_PATH + "/" + userId + "/theme";
            
            // 确保目标目录存在
            File targetDir = new File(targetThemePath);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            
            // 删除当前主题内容
            deleteDirectory(targetDir);
            
            // 复制选中的主题到支付宝主题目录
            File sourceThemeDir = new File(THEMES_PATH, themeName);
            copyDirectory(sourceThemeDir, targetDir);
            
            // 提示用户需要重启支付宝
            Toast.makeText(this, "主题应用成功！请重启支付宝以使更改生效", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "应用主题失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private String getUserId() {
        // 尝试从用户信息文件中读取用户ID
        try {
            File userInfoFile = new File(EXTERNAL_STORAGE_PATH + "/user_info.json");
            if (userInfoFile.exists()) {
                java.util.Scanner scanner = new java.util.Scanner(userInfoFile);
                String userInfoJson = scanner.nextLine();
                scanner.close();
                
                // 简单解析JSON获取userId
                if (userInfoJson.contains("\"userId\":\"")) {
                    int start = userInfoJson.indexOf("\"userId\":\"") + 10;
                    int end = userInfoJson.indexOf("\"", start);
                    return userInfoJson.substring(start, end);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
    
    private void copyDirectory(File source, File destination) {
        if (!destination.exists()) {
            destination.mkdirs();
        }
        
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                File newFile = new File(destination, file.getName());
                if (file.isDirectory()) {
                    copyDirectory(file, newFile);
                } else {
                    try {
                        java.io.FileInputStream in = new java.io.FileInputStream(file);
                        java.io.FileOutputStream out = new java.io.FileOutputStream(newFile);
                        
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        in.close();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}