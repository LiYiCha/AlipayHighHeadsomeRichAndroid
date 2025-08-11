package im.hoho.alipayInstallB;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.robv.android.xposed.XposedHelpers;
import lombok.Getter;

/**
 * 支付宝个性化模块主界面Activity
 * 提供皮肤管理、会员等级设置、资源下载等功能
 */
public class MainActivity extends Activity {

    // 存储路径常量
    private static final String EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory() + 
            "/Android/media/com.eg.android.AlipayGphone/000_HOHO_ALIPAY_SKIN";
    
    // 各种操作文件路径
    private static final String EXPORT_FILE = EXTERNAL_STORAGE_PATH + "/export";
    private static final String DELETE_FILE = EXTERNAL_STORAGE_PATH + "/delete";
    private static final String UPDATE_FILE = EXTERNAL_STORAGE_PATH + "/update";
    private static final String ACTIVATE_FILE = EXTERNAL_STORAGE_PATH + "/actived";
    private static final String USER_INFO_FILE = EXTERNAL_STORAGE_PATH + "/user_info.json";
    
    // 权限请求码
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    // 资源下载相关常量
    private static final String DOWNLOAD_URL = "https://github.com/nov30th/AlipayHighHeadsomeRichAndroid/raw/master/SD%E5%8D%A1%E8%B5%84%E6%BA%90%E6%96%87%E4%BB%B6%E5%8C%85/SD%E8%B5%84%E6%BA%90%E6%96%87%E4%BB%B6.zip";
    private static final String EXTRACT_PATH = Environment.getExternalStorageDirectory() + "/Android/media/com.eg.android.AlipayGphone/";
    
    // 会员等级选项
    private final String[] memberGrades = {"原有", "普通 (primary)", "黄金 (golden)", "铂金 (platinum)", "钻石 (diamond)"};
    
    // UI控件引用
    private Button btnExport, btnDelete, btnUpdate, btnActivate;
    private Button btnThemeManager; // 添加主题管理按钮引用
    private ImageView ivExportStatus, ivDeleteStatus, ivUpdateStatus, ivActivateStatus;
    private Button btnDownload;
    private ProgressBar progressBar;
    private Spinner spinnerMemberGrade;
    private TextView tvUserId, tvUserName,tvPluginStatus;  
    
    // 异步任务相关
    private ExecutorService executorService;
    private Handler mainHandler;
    
    // 首次运行配置
    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_FIRST_RUN = "isFirstRun";

    /**
     * 权限请求结果回调处理
     * @param requestCode 请求码
     * @param permissions 权限数组
     * @param grantResults 授权结果数组
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予
                Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                // 权限被拒绝
                Toast.makeText(this, "存储权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Activity创建时的初始化操作
     * @param savedInstanceState 保存的状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查是否首次运行并显示隐私说明
        checkFirstRun();

        setContentView(R.layout.activity_main);

        // 初始化UI组件
        initUIComponents();
        
        // 初始化用户信息显示
        tvUserId = findViewById(R.id.tvUserId);
        tvUserName = findViewById(R.id.tvUserName);
        
        // 初始化异步任务相关组件
        initAsyncComponents();
        
        // 设置按钮点击事件
        setupButtons();
        
        // 更新状态显示
        updateStatuses();
        
        // 设置下载按钮事件
        setupDownloadButton();
        
        // 设置其他按钮事件
        setupOtherButtons();
        
        // 更新用户信息显示
        updateUserInfoDisplay();
    }

    /**
     * 检查是否首次运行并显示隐私说明
     */
    private void checkFirstRun() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean isFirstRun = settings.getBoolean(KEY_FIRST_RUN, true);

        if (isFirstRun) {
            new AlertDialog.Builder(this)
                    .setTitle("隐私说明")
                    .setMessage("本应用不会收集、不会上传任何用户信息或使用数据。\n\n" +
                            "应用仅在本地运行，不会与任何服务器通信（除非您主动点击\"下载资源包\"按钮从 Github 下载资源）。\n\n" +
                            "所有操作均在您的设备本地完成，请放心使用。")
                    .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 标记已经显示过隐私说明
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean(KEY_FIRST_RUN, false);
                            editor.apply();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    /**
     * 初始化UI组件
     */
    private void initUIComponents() {
        spinnerMemberGrade = findViewById(R.id.spinnerMemberGrade);
        setupMemberGradeSpinner();

        TextView tvVersion = findViewById(R.id.tvVersion);
        tvVersion.setText("Version: " + BuildConfig.VERSION_NAME);
        
        // 初始化用户信息显示控件
        tvUserId = findViewById(R.id.tvUserId);
        tvUserName = findViewById(R.id.tvUserName);
        // 初始化插件状态显示控件
        tvPluginStatus = findViewById(R.id.tvPluginStatus);

        btnExport = findViewById(R.id.btnExport);
        btnDelete = findViewById(R.id.btnDelete);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnActivate = findViewById(R.id.btnActivate);

        ivExportStatus = findViewById(R.id.ivExportStatus);
        ivDeleteStatus = findViewById(R.id.ivDeleteStatus);
        ivUpdateStatus = findViewById(R.id.ivUpdateStatus);
        ivActivateStatus = findViewById(R.id.ivActivateStatus);
    }

    /**
     * 初始化异步任务相关组件
     */
    private void initAsyncComponents() {
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        btnDownload = findViewById(R.id.btnDownload);
        progressBar = findViewById(R.id.progressBar);
        updateDownloadButtonText();
    }

    /**
     * 设置下载按钮点击事件
     */
    private void setupDownloadButton() {
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadAndExtract();
            }
        });
    }

    /**
     * 设置其他按钮点击事件
     */
    private void setupOtherButtons() {
        Button btnOpenResourceFolder = findViewById(R.id.btnOpenResourceFolder);
        TextView tvGithubLink = findViewById(R.id.tvGithubLink);
        btnThemeManager = findViewById(R.id.btnThemeManager); // 初始化主题管理按钮

        btnOpenResourceFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openResourceFolder();
            }
        });

        tvGithubLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGithubLink();
            }
        });
        
        // 设置主题管理按钮点击事件
        btnThemeManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openThemeManager();
            }
        });
    }
    
    /**
     * 打开主题管理界面
     */
    private void openThemeManager() {
        Intent intent = new Intent(this, ThemeManagerActivity.class);
        startActivity(intent);
    }
    
    /**
     * 打开资源文件夹
     */
    private void openResourceFolder() {
        File resourceFolder = new File(EXTRACT_PATH + "000_HOHO_ALIPAY_SKIN");
        if (resourceFolder.exists() && resourceFolder.isDirectory()) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.parse(resourceFolder.getAbsolutePath());
            intent.setDataAndType(uri, "*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

            try {
                startActivity(Intent.createChooser(intent, "选择文件浏览器"));
            } catch (ActivityNotFoundException e) {
                // 如果没有找到文件管理器应用，尝试使用 ACTION_VIEW
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "resource/folder");

                if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "没有找到文件浏览器应用", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(MainActivity.this, "资源包未安装", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 打开GitHub项目链接
     */
    private void openGithubLink() {
        String url = "https://github.com/nov30th/AlipayHighHeadsomeRichAndroid";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    /**
     * 设置按钮点击事件
     */
    private void setupButtons() {
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFile(EXPORT_FILE);
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFile(DELETE_FILE);
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFile(UPDATE_FILE);
            }
        });
        btnActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFile(ACTIVATE_FILE);
            }
        });
    }

    /**
     * 切换文件状态（创建或删除）
     * @param filePath 文件路径
     */
    private void toggleFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        } else {
            try {
                // 确保父目录存在
                new File(EXTERNAL_STORAGE_PATH).mkdirs();
                // 创建文件（实际上是创建目录）
                new File(filePath).mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "创建文件时出错", Toast.LENGTH_SHORT).show();
            }
        }
        updateStatuses();
        Toast.makeText(this, "重新打开付款码以生效", Toast.LENGTH_SHORT).show();
    }

    /**
     * Activity恢复时更新状态
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateStatuses();
        updateDownloadButtonText();
        updateUserInfoDisplay();
    }
    
    /**
     * 更新插件状态显示
     */
    private void updatePluginStatus() {
        // 确保executorService已初始化
        if (executorService == null) {
            // 如果未初始化，直接在主线程设置默认状态
            tvPluginStatus.setText("插件：未加载");
            tvPluginStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }
        
        // 使用后台线程检查插件是否加载
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                boolean isLoaded = false;
                long lastUpdate = 0;
                
                try {
                    // 通过SharedPreferences检查模块状态
                    SharedPreferences prefs = getSharedPreferences("im.hoho.alipayInstallB.prefs", Context.MODE_PRIVATE);
                    isLoaded = prefs.getBoolean("isModuleLoaded", false);
                    lastUpdate = prefs.getLong("lastUpdate", 0);
                    
                    // 检查状态是否过期（超过5分钟）
                    if (System.currentTimeMillis() - lastUpdate > 5 * 60 * 1000) {
                        isLoaded = false;
                    }
                } catch (Exception e) {
                    // 如果出现异常，说明模块未加载或无法访问
                    Log.e("MainActivity", "检查模块状态时出错: " + e.getMessage());
                }
                
                // 在主线程更新UI
                final boolean finalIsLoaded = isLoaded;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (finalIsLoaded) {
                            tvPluginStatus.setText("插件：已加载");
                            tvPluginStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        } else {
                            tvPluginStatus.setText("插件：未加载");
                            tvPluginStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        }
                    }
                });
            }
        });
    }
    
    /**
     * 更新用户信息显示
     */
    private void updateUserInfoDisplay() {
        // 使用后台线程异步读取用户信息文件，避免阻塞UI线程
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File userInfoFile = new File(USER_INFO_FILE);
                    String userId = "未获取到";
                    String userName = "未获取到";
                    
                    if (userInfoFile.exists()) {
                        // 读取文件内容
                        FileInputStream fis = new FileInputStream(userInfoFile);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        byte[] buffer = new byte[(int) userInfoFile.length()];
                        bis.read(buffer);
                        bis.close();
                        fis.close();
                        
                        // 解析JSON
                        String userInfoJson = new String(buffer);
                        JSONObject jsonObject = JSONObject.parseObject(userInfoJson);
                        
                        String userIdFromFile = jsonObject.getString("userId");
                        String userNameFromFile = jsonObject.getString("userName");
                        
                        if (userIdFromFile != null && !userIdFromFile.isEmpty()) {
                            userId = userIdFromFile;
                        }
                        
                        if (userNameFromFile != null && !userNameFromFile.isEmpty()) {
                            userName = userNameFromFile;
                        }
                    }
                    
                    // 在主线程更新UI
                    final String finalUserId = userId;
                    final String finalUserName = userName;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvUserId.setText("用户ID: " + finalUserId);
                            tvUserName.setText("用户名: " + finalUserName);
                        }
                    });
                } catch (Exception e) {
                    Log.e("MainActivity", "读取用户信息文件时出错: " + e.getMessage());
                    // 在主线程更新UI
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvUserId.setText("用户ID: 未获取到");
                            tvUserName.setText("用户名: 未获取到");
                        }
                    });
                }
            }
        });
    }

    /**
     * 更新所有状态显示
     */
    private void updateStatuses() {
        updateStatus(ivExportStatus, EXPORT_FILE);
        updateStatus(ivDeleteStatus, DELETE_FILE);
        updateStatus(ivUpdateStatus, UPDATE_FILE);
        updateStatus(ivActivateStatus, ACTIVATE_FILE);
        updatePluginStatus(); // 更新插件状态显示

        btnActivate.setText(new File(ACTIVATE_FILE).exists() ? "点击禁用皮肤" : "点击启用皮肤");
    }

    /**
     * 更新单个状态图标
     * @param imageView 图标ImageView
     * @param filePath 对应的文件路径
     */
    private void updateStatus(ImageView imageView, String filePath) {
        imageView.setImageResource(new File(filePath).exists() ? R.drawable.green_circle : R.drawable.red_circle);
    }

    /**
     * 更新下载按钮文本
     */
    private void updateDownloadButtonText() {
        File skinFolder = new File(EXTRACT_PATH + "000_HOHO_ALIPAY_SKIN");
        btnDownload.setText(skinFolder.exists() ? "重新下载资源包 (Github) 需要SD卡权限" : "下载资源包 (Github) 需要SD卡权限");
    }

    /**
     * 下载并解压资源包
     */
    private void downloadAndExtract() {
        // 检查并请求存储权限
        if (!checkAndRequestPermissions()) {
            return;
        }

        // 禁用按钮并显示进度条
        btnDownload.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        // 在后台线程执行下载任务
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                performDownloadAndExtract();
            }
        });
    }

    /**
     * 检查并请求存储权限
     * @return 是否已获得权限
     */
    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                return false;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    /**
     * 执行下载和解压操作
     */
    private void performDownloadAndExtract() {
        try {
            URL url = new URL(DOWNLOAD_URL);
            URLConnection connection = url.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength();

            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(EXTRACT_PATH + "temp.zip");

            byte[] data = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                final int progress = (int) (total * 100 / fileLength);
                // 在主线程更新进度
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progress);
                    }
                });
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            // 解压文件
            unzip(EXTRACT_PATH + "temp.zip", EXTRACT_PATH);

            // 在主线程更新UI
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    btnDownload.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "下载和解压完成", Toast.LENGTH_LONG).show();
                    updateDownloadButtonText();
                }
            });
        } catch (final Exception e) {
            // 在主线程显示错误信息
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    btnDownload.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * 解压ZIP文件
     * @param zipFilePath ZIP文件路径
     * @param destDirectory 解压目标目录
     */
    public void unzip(String zipFilePath, String destDirectory) {
        try {
            ZipFile zipFile = new ZipFile(zipFilePath);
            zipFile.extractAll(destDirectory);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    /**
     * Activity销毁时释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * 设置会员等级选择器
     */
    private void setupMemberGradeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, memberGrades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMemberGrade.setAdapter(adapter);

        // 设置当前选中的等级
        String currentGrade = getCurrentMemberGrade();
        int position = adapter.getPosition(currentGrade);
        spinnerMemberGrade.setSelection(position);

        spinnerMemberGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGrade = parent.getItemAtPosition(position).toString();
                updateMemberGrade(selectedGrade);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 无操作
            }
        });
    }

    /**
     * 获取当前会员等级
     * @return 当前会员等级
     */
    private String getCurrentMemberGrade() {
        for (String grade : memberGrades) {
            if (grade.equals("原有")) continue;
            String folderName = "level_" + grade.split(" ")[1].replace("(", "").replace(")", "");
            File folder = new File(EXTERNAL_STORAGE_PATH, folderName);
            if (folder.exists()) {
                return grade;
            }
        }
        return "原有";
    }

    /**
     * 更新会员等级
     * @param selectedGrade 选中的等级
     */
    private void updateMemberGrade(String selectedGrade) {
        // 删除所有 level_ 文件夹
        deleteAllLevelFolders();

        // 如果选择不是"原有"，创建新的 level_ 文件夹
        if (!selectedGrade.equals("原有")) {
            createLevelFolder(selectedGrade);
        }

        Toast.makeText(this, "会员等级已更新为：" + selectedGrade, Toast.LENGTH_SHORT).show();
    }

    /**
     * 删除所有等级文件夹
     */
    private void deleteAllLevelFolders() {
        for (String grade : memberGrades) {
            if (grade.equals("原有")) continue;
            String folderName = "level_" + grade.split(" ")[1].replace("(", "").replace(")", "");
            File folder = new File(EXTERNAL_STORAGE_PATH, folderName);
            if (folder.exists()) {
                deleteRecursive(folder);
            }
        }
    }

    /**
     * 创建指定等级的文件夹
     * @param selectedGrade 选中的等级
     */
    private void createLevelFolder(String selectedGrade) {
        String folderName = "level_" + selectedGrade.split(" ")[1].replace("(", "").replace(")", "");
        File newFolder = new File(EXTERNAL_STORAGE_PATH, folderName);
        newFolder.mkdirs();
    }

    /**
     * 递归删除文件或文件夹
     * @param fileOrDirectory 文件或文件夹
     */
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }
}