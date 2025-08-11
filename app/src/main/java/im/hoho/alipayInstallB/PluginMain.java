package im.hoho.alipayInstallB;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 支付宝个性化模块主类
 * 实现会员等级修改和皮肤替换功能
 */
public class PluginMain implements IXposedHookLoadPackage {
    // 主题存储基础路径
    private static final String THEME_BASE_PATH = Environment.getExternalStorageDirectory() + 
            "/Android/media/com.eg.android.AlipayGphone";
    
    // 主题文件夹名称
    private static final String THEME_FOLDER_NAME = "000_HOHO_ALIPAY_SKIN";
    
    // 完整的主题路径
    private static final String EXTERNAL_STORAGE_PATH = THEME_BASE_PATH + "/" + THEME_FOLDER_NAME;
    
    // SharedPreferences文件名
    private static final String PREFS_NAME = "im.hoho.alipayInstallB.prefs";
    
    // 支付宝包名
    private static final String PACKAGE_NAME = "com.eg.android.AlipayGphone";
    
    // 模块是否已加载的标志
    private static volatile boolean isModuleLoaded = false;
    
    // 类加载器
    private static ClassLoader classLoader = null;
    
    // MicroApplicationContext对象
    private static Object microApplicationContextObject = null;
    
    // 用户信息文件路径
    private static final String USER_INFO_FILE = EXTERNAL_STORAGE_PATH + "/user_info.json";

    /**
     * 构造函数，模块加载时会输出日志
     */
    public PluginMain() {
        XposedBridge.log("正在加载HOHO支付宝模块...");
    }
    
    /**
     * 获取模块是否已加载
     * @return 模块是否已加载
     */
    public static boolean isModuleLoaded() {
        return isModuleLoaded;
    }
    
    /**
     * 保存模块状态到SharedPreferences
     * @param isLoaded 模块是否已加载
     */
    private void saveModuleStatus(boolean isLoaded) {
        try {
            // 通过反射获取Context并保存状态
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = XposedHelpers.callStaticMethod(activityThreadClass, "currentActivityThread");
            Context systemContext = (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
            
            SharedPreferences prefs = systemContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isModuleLoaded", isLoaded);
            editor.putLong("lastUpdate", System.currentTimeMillis());
            editor.apply();
        } catch (Exception e) {
            XposedBridge.log("保存模块状态时出错: " + e.getMessage());
        }
    }

    /**
     * 处理目标应用加载事件
     * @param lpparam 加载包参数
     * @throws Throwable 异常
     */
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 只处理支付宝应用
        if (lpparam.packageName.equals(PACKAGE_NAME)) {
            XposedBridge.log("已加载应用: " + lpparam.packageName);
            XposedBridge.log("由HOHO``提供支持 20230927 杭州亚运会版 sd source changed 20231129");
            
            // 设置模块已加载标志
            isModuleLoaded = true;
            
            // 保存模块状态到SharedPreferences
            saveModuleStatus(true);
            
            // 保存类加载器
            classLoader = lpparam.classLoader;

            // 数据库是否已更新的标志
            final boolean[] isDbUpdated = {false};
            
            // Hook会员等级相关方法
            hookMemberGradeMethods(lpparam);
            
            // Hook登录结果方法以修改会员等级
            hookUserLoginResultMethod(lpparam);
            
            // Hook Activity onCreate 方法以更新数据库
            hookActivityOnCreateMethod(lpparam, isDbUpdated);
            
            // Hook皮肤相关方法
            hookSkinMethods(lpparam);
            
            // Hook支付宝Application类以获取用户信息
            hookAlipayApplication(lpparam);
        }
    }

    /**
     * Hook支付宝Application类以获取用户信息
     * @param lpparam 加载包参数
     */
    private void hookAlipayApplication(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook Application类的attach方法以获取Context
            XposedHelpers.findAndHookMethod(android.app.Application.class, "attach", Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Context context = (Context) param.args[0];
                            // 尝试获取用户信息并保存到文件
                            saveUserInfoToFile();
                            super.afterHookedMethod(param);
                        }
                    });
        } catch (Exception e) {
            XposedBridge.log("Hook Application失败: " + e.getMessage());
        }
    }

    /**
     * 保存用户信息到文件
     */
    private void saveUserInfoToFile() {
        try {
            String userId = getUserId();
            String userName = getUserName();
            
            if (userId != null) {
                // 创建JSON格式的用户信息
                String userInfoJson = "{\"userId\":\"" + userId + "\",\"userName\":\"" + (userName != null ? userName : "") + "\"}";
                
                // 确保目录存在
                File dir = new File(EXTERNAL_STORAGE_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                // 写入文件
                File file = new File(USER_INFO_FILE);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(userInfoJson.getBytes());
                fos.close();
                
                XposedBridge.log("用户信息已保存: " + userInfoJson);
            }
        } catch (Exception e) {
            XposedBridge.log("保存用户信息到文件时出错: " + e.getMessage());
        }
    }

    /**
     * 获取MicroApplicationContext对象
     * @return MicroApplicationContext对象
     */
    public static Object getMicroApplicationContext() {
        if (microApplicationContextObject == null) {
            try {
                Class<?> alipayApplicationClass = XposedHelpers.findClass(
                        "com.alipay.mobile.framework.AlipayApplication", classLoader
                );
                Object alipayApplicationInstance = XposedHelpers.callStaticMethod(
                        alipayApplicationClass, "getInstance"
                );
                if (alipayApplicationInstance == null) {
                    return null;
                }
                microApplicationContextObject = XposedHelpers.callMethod(
                        alipayApplicationInstance, "getMicroApplicationContext"
                );
            } catch (Throwable t) {
                XposedBridge.log("getMicroApplicationContext err: " + t.getMessage());
            }
        }
        return microApplicationContextObject;
    }

    /**
     * 获取服务对象
     * @param service 服务接口名
     * @return 服务对象
     */
    public static Object getServiceObject(String service) {
        try {
            return XposedHelpers.callMethod(getMicroApplicationContext(), "findServiceByInterface", service);
        } catch (Throwable th) {
            XposedBridge.log("getServiceObject err: " + th.getMessage());
        }
        return null;
    }

    /**
     * 获取用户对象
     * @return 用户对象
     */
    public static Object getUserObject() {
        try {
            Class<?> socialSdkContactServiceClass = XposedHelpers.findClass(
                    "com.alipay.mobile.personalbase.service.SocialSdkContactService", classLoader);
            return XposedHelpers.callMethod(
                    getServiceObject(socialSdkContactServiceClass.getName()), "getMyAccountInfoModelByLocal");
        } catch (Throwable th) {
            XposedBridge.log("getUserObject err: " + th.getMessage());
        }
        return null;
    }

    /**
     * 获取用户ID
     * @return 用户ID
     */
    public static String getUserId() {
        try {
            Object userObject = getUserObject();
            if (userObject != null) {
                Object userIdObject = XposedHelpers.getObjectField(userObject, "userId");
                if (userIdObject != null) {
                    return userIdObject.toString();
                }
            }
        } catch (Throwable th) {
            XposedBridge.log("getUserId err: " + th.getMessage());
        }
        return null;
    }

    /**
     * 获取用户名
     * @return 用户名
     */
    public static String getUserName() {
        try {
            Object userObject = getUserObject();
            if (userObject != null) {
                Object userNameObject = XposedHelpers.getObjectField(userObject, "userName");
                if (userNameObject != null) {
                    return userNameObject.toString();
                }
            }
        } catch (Throwable th) {
            XposedBridge.log("getUserName err: " + th.getMessage());
        }
        return null;
    }

    /**
     * Hook会员等级相关方法
     * @param lpparam 加载包参数
     */
    private void hookMemberGradeMethods(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // 查找会员等级枚举类
            Class<?> memberGradeEnumClass = XposedHelpers.findClass("com.alipay.mobile.onsitepay9.utils.MergeMemberGradeEnum", lpparam.classLoader);
            if (memberGradeEnumClass != null) {
                // Hook转换会员等级的方法
                XposedHelpers.findAndHookMethod("com.alipay.mobile.onsitepay9.utils.MergeMemberGradeEnum",
                        lpparam.classLoader,
                        "convertMemberGrade",
                        String.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                String newGrade = getCurrentMemberGrade();
                                XposedBridge.log("会员等级将更改为: " + newGrade);
                                
                                // 如果不是"原有"等级，则修改等级
                                if (!newGrade.equals("原有")) {
                                    setMemberGradeResult(param, memberGradeEnumClass, newGrade);
                                    XposedBridge.log("会员等级已更改为: " + newGrade);
                                }
                            }
                        });
                XposedBridge.log("convertMemberGrade方法已Hook.");
            } else {
                XposedBridge.log("未找到MergeMemberGradeEnum类.");
            }
        } catch (XposedHelpers.ClassNotFoundError e) {
            XposedBridge.log("未找到MergeMemberGradeEnum类: " + e.getMessage());
        } catch (NoSuchMethodError e) {
            XposedBridge.log("未找到convertMemberGrade方法: " + e.getMessage());
        } catch (Exception e) {
            XposedBridge.log("Hook convertMemberGrade时出错: " + e.getMessage());
        }
    }

    /**
     * 设置会员等级结果
     * @param param 方法Hook参数
     * @param memberGradeEnumClass 会员等级枚举类
     * @param newGrade 新等级
     */
    private void setMemberGradeResult(XC_MethodHook.MethodHookParam param, Class<?> memberGradeEnumClass, String newGrade) {
        switch(newGrade) {
            case "primary":
                param.setResult(XposedHelpers.getStaticObjectField(memberGradeEnumClass, "PRIMARY"));
                break;
            case "golden":
                param.setResult(XposedHelpers.getStaticObjectField(memberGradeEnumClass, "GOLDEN"));
                break;
            case "platinum":
                param.setResult(XposedHelpers.getStaticObjectField(memberGradeEnumClass, "PLATINUM"));
                break;
            case "diamond":
                param.setResult(XposedHelpers.getStaticObjectField(memberGradeEnumClass, "DIAMOND"));
                break;
            default:
                param.setResult(XposedHelpers.getStaticObjectField(memberGradeEnumClass, "NULL"));
                break;
        }
    }

    /**
     * Hook用户登录结果方法
     * @param lpparam 加载包参数
     */
    private void hookUserLoginResultMethod(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> userLoginResultClass = XposedHelpers.findClass("com.alipay.mobilegw.biz.shared.processer.login.UserLoginResult", lpparam.classLoader);
            if (userLoginResultClass != null) {
                // Hook获取扩展属性的方法
                XposedHelpers.findAndHookMethod("com.alipay.mobilegw.biz.shared.processer.login.UserLoginResult", 
                        lpparam.classLoader, 
                        "getExtResAttrs", 
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                XposedBridge.log("现在，让我们安装B...");
                                @SuppressWarnings("unchecked")
                                Map<String, String> map = (Map<String, String>) param.getResult();
                                
                                if (map.containsKey("memberGrade")) {
                                    XposedBridge.log("原始会员等级: " + map.get("memberGrade"));

                                    String newGrade = getCurrentMemberGrade();
                                    if (!newGrade.equals("原有")) {
                                        XposedBridge.log("将 " + newGrade + " 放入字典...");
                                        map.put("memberGrade", newGrade);
                                        XposedBridge.log("会员等级已更改为: " + map.get("memberGrade"));
                                    } else {
                                        XposedBridge.log("会员等级未修改.");
                                    }
                                } else {
                                    XposedBridge.log("无法在返回值中获取会员等级... WTF?");
                                }
                            }
                        });
            } else {
                XposedBridge.log("未找到UserLoginResult类.");
            }
        } catch (Exception e) {
            XposedBridge.log("未找到UserLoginResult类: " + e.getMessage());
        }
    }

    /**
     * Hook Activity onCreate 方法以更新数据库
     * @param lpparam 加载包参数
     * @param isDbUpdated 数据库是否已更新的标志
     */
    private void hookActivityOnCreateMethod(final XC_LoadPackage.LoadPackageParam lpparam, final boolean[] isDbUpdated) {
        XposedHelpers.findAndHookMethod("android.app.Activity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String newGrade = getCurrentMemberGrade();

                // 如果数据库已更新或等级为"原有"，则不执行操作
                if (isDbUpdated[0] || newGrade.equals("原有")) {
                    return;
                }
                
                Context context = (Context) param.thisObject;
                XposedBridge.log("--------------数据库更新器--------------");
                File dbFile = context.getDatabasePath("alipayclient.db");
                
                if (dbFile.exists()) {
                    XposedBridge.log("获取数据库: " + context.getDatabasePath("alipayclient.db").getParentFile());
                    updateDatabase(dbFile, newGrade);
                } else {
                    XposedBridge.log("无法获取数据库: " + context.getDatabasePath("alipayclient.db").getParentFile() + ", 忽略!");
                }
                
                XposedBridge.log("--------------数据库更新器--------------");
                isDbUpdated[0] = true;
            }
        });
    }

    /**
     * 更新数据库中的会员等级
     * @param dbFile 数据库文件
     * @param newGrade 新等级
     */
    private void updateDatabase(File dbFile, String newGrade) {
        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE)) {
            // 更新用户信息表中的会员等级
            db.execSQL("UPDATE 'main'.'userinfo' SET 'memberGrade' = '" + newGrade + "'");
            XposedBridge.log("数据库更新成功!");
        } catch (Exception e) {
            XposedBridge.log("数据库更新错误: " + e);
        }
    }

    /**
     * Hook皮肤相关方法
     * @param lpparam 加载包参数
     */
    private void hookSkinMethods(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // 查找皮肤模型类
            final Class<?> ospSkinModel = lpparam.classLoader.loadClass("com.alipay.mobile.onsitepaystatic.skin.OspSkinModel");

            // Hook获取面部支付皮肤模型的方法
            XposedHelpers.findAndHookMethod("com.alipay.mobile.onsitepaystatic.ConfigUtilBiz", lpparam.classLoader, "getFacePaySkinModel", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    handleSkinUpdate(param, ospSkinModel, lpparam);
                }
            });
        } catch (Exception e) {
            XposedBridge.log("Hook皮肤方法时出错: " + e.getMessage());
        }
    }

    /**
     * 处理皮肤更新
     * @param param 方法Hook参数
     * @param ospSkinModel 皮肤模型类
     * @param lpparam 加载包参数
     */
    private void handleSkinUpdate(XC_MethodHook.MethodHookParam param, Class<?> ospSkinModel, XC_LoadPackage.LoadPackageParam lpparam) {
        // 定义各种路径
        String fixedPathInAliData = "/data/data/" + PACKAGE_NAME + "/files/onsitepay_skin_dir/HOHO";
        String alipaySkinsRoot = "/data/data/" + PACKAGE_NAME + "/files/onsitepay_skin_dir";
        File hohoSkinFileInAliData = new File(fixedPathInAliData);

        String basePathUpdates = Environment.getExternalStorageDirectory() + "/Android/media/" + PACKAGE_NAME;
        ensureDirectoryExists(basePathUpdates);
        String fixedPathUpdates = basePathUpdates + "/000_HOHO_ALIPAY_SKIN";

        // 各种控制文件
        File skinActived = new File(fixedPathUpdates + "/actived");
        File skinUpdateRequired = new File(fixedPathUpdates + "/update");
        File skinDeleteRequired = new File(fixedPathUpdates + "/delete");
        File exportSkinSign = new File(fixedPathUpdates + "/export");

        // 处理导出皮肤
        if (exportSkinSign.exists()) {
            handleExportSkin(alipaySkinsRoot, fixedPathUpdates, exportSkinSign);
        }

        // 处理删除皮肤
        if (skinDeleteRequired.exists()) {
            handleDeleteSkin(hohoSkinFileInAliData, skinDeleteRequired);
        }

        // 处理更新皮肤
        if (skinUpdateRequired.exists()) {
            handleUpdateSkin(fixedPathUpdates, fixedPathInAliData, hohoSkinFileInAliData, skinUpdateRequired);
        }

        // 如果皮肤已激活，则更新皮肤
        if (hohoSkinFileInAliData.exists() && skinActived.exists()) {
            updateSkin(param, ospSkinModel, fixedPathInAliData);
        } else {
            XposedBridge.log("皮肤未激活.");
        }
    }

    /**
     * 确保目录存在
     * @param path 路径
     */
    private void ensureDirectoryExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            XposedBridge.log("创建皮肤SD卡路径: " + path);
            dir.mkdirs();
        }
    }

    /**
     * 处理导出皮肤
     * @param alipaySkinsRoot 支付宝皮肤根目录
     * @param fixedPathUpdates 固定更新路径
     * @param exportSkinSign 导出标志文件
     */
    private void handleExportSkin(String alipaySkinsRoot, String fixedPathUpdates, File exportSkinSign) {
        try {
            XposedBridge.log("正在导出皮肤...");
            File alipaySkinsRootFile = new File(alipaySkinsRoot);
            
            if (!alipaySkinsRootFile.exists()) {
                // 没有找到皮肤，忽略导出
                XposedBridge.log("未找到皮肤，忽略导出");
            } else {
                File fixedPathUpdatesFile = new File(fixedPathUpdates);
                if (!fixedPathUpdatesFile.exists()) {
                    // 创建目录
                    fixedPathUpdatesFile.mkdirs();
                }
                
                // 复制除HOHO目录外的所有皮肤
                File[] alipaySkinsRootFileList = alipaySkinsRootFile.listFiles();
                if (alipaySkinsRootFileList != null) {
                    for (File alipaySkinsRootFileListItem : alipaySkinsRootFileList) {
                        if (alipaySkinsRootFileListItem.isDirectory()) {
                            if (alipaySkinsRootFileListItem.getName().equals("HOHO")) {
                                continue;
                            }
                            XposedBridge.log("正在导出皮肤: " + alipaySkinsRootFileListItem.getName());
                            copy(alipaySkinsRootFileListItem.getPath(), fixedPathUpdates + "/" + alipaySkinsRootFileListItem.getName());
                        }
                    }
                }
                
                // 删除导出标志
                exportSkinSign.delete();
            }
        } catch (Exception e) {
            XposedBridge.log("导出皮肤时出错: " + e.getMessage());
        }
    }

    /**
     * 处理删除皮肤
     * @param hohoSkinFileInAliData HOHO皮肤文件
     * @param skinDeleteRequired 删除标志文件
     */
    private void handleDeleteSkin(File hohoSkinFileInAliData, File skinDeleteRequired) {
        XposedBridge.log("正在删除皮肤...");
        skinDeleteRequired.delete();
        deleteFile(hohoSkinFileInAliData);
        XposedBridge.log("皮肤已删除");
    }

    /**
     * 处理更新皮肤
     * @param fixedPathUpdates 固定更新路径
     * @param fixedPathInAliData 固定支付宝数据路径
     * @param hohoSkinFileInAliData HOHO皮肤文件
     * @param skinUpdateRequired 更新标志文件
     */
    private void handleUpdateSkin(String fixedPathUpdates, String fixedPathInAliData, File hohoSkinFileInAliData, File skinUpdateRequired) {
        XposedBridge.log("正在复制皮肤...");
        skinUpdateRequired.delete();
        if (!hohoSkinFileInAliData.exists()) {
            hohoSkinFileInAliData.mkdirs();
        }
        copy(fixedPathUpdates, fixedPathInAliData);
        XposedBridge.log("文件已复制..");
    }

    /**
     * 更新皮肤
     * @param param 方法Hook参数
     * @param ospSkinModel 皮肤模型类
     * @param fixedPathInAliData 固定支付宝数据路径
     */
    private void updateSkin(XC_MethodHook.MethodHookParam param, Class<?> ospSkinModel, String fixedPathInAliData) {
        XposedBridge.log("正在更新皮肤..");
        List<String> randomConf = searchSkins(fixedPathInAliData);
        String subFolder = "";
        
        if (!randomConf.isEmpty()) {
            // 随机选择配置
            int pos = (int) (Math.random() * 100) % randomConf.size();
            subFolder = randomConf.get(pos);
        }
        
        String hohoSkinModelJson = "{\"md5\":\"HOHO_MD5\",\"minWalletVersion\":\"10.2.23.0000\",\"outDirName\":\"HOHO/" + subFolder + "\",\"skinId\":\"HOHO_CUSTOMIZED\",\"skinStyleId\":\"2022 New Year Happy!\",\"userId\":\"HOHO\"}";
        Object skinModel = JSON.parseObject(hohoSkinModelJson, ospSkinModel);
        param.setResult(skinModel);
        XposedBridge.log("皮肤已更新..");
    }

    /**
     * 递归删除文件或目录
     * @param file 文件或目录
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteFile(f);
                }
            }
            file.delete();
        } else if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 复制文件或目录
     * @param fromFile 源文件路径
     * @param toFile 目标文件路径
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void copy(String fromFile, String toFile) {
        File root = new File(fromFile);
        if (!root.exists()) {
            return;
        }
        
        File[] currentFiles = root.listFiles();
        if (currentFiles == null) {
            return;
        }
        
        File targetDir = new File(toFile);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        
        for (File currentFile : currentFiles) {
            if (currentFile.isDirectory()) {
                // 如果当前项为子目录，递归处理
                copy(currentFile.getPath(), toFile + "/" + currentFile.getName());
            } else {
                // 如果当前项为文件则进行文件拷贝
                copySdcardFile(currentFile.getPath(), toFile + "/" + currentFile.getName());
            }
        }
    }

    /**
     * 复制SD卡文件
     * @param fromFile 源文件路径
     * @param toFile 目标文件路径
     */
    public void copySdcardFile(String fromFile, String toFile) {
        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte[] bt = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
        } catch (Exception ex) {
            XposedBridge.log("复制SD卡文件时出错: " + ex.getMessage());
        }
    }

    /**
     * 搜索皮肤配置
     * @param path 搜索路径
     * @return 皮肤配置列表
     */
    public List<String> searchSkins(String path) {
        List<String> resultList = new ArrayList<>();
        File[] files = new File(path).listFiles();
        
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    // 跳过特殊目录
                    if (f.getName().equals("update") || f.getName().equals("actived") || 
                        f.getName().equals("delete") || f.getName().startsWith("level_")) {
                        continue;
                    }
                    resultList.add(f.getName());
                }
            }
        }
        return resultList;
    }

    /**
     * 获取当前会员等级
     * @return 当前会员等级
     */
    private String getCurrentMemberGrade() {
        String[] grades = {"primary", "golden", "platinum", "diamond", "unknown"};
        for (String grade : grades) {
            File folder = new File(EXTERNAL_STORAGE_PATH, "level_" + grade);
            if (folder.exists()) {
                return grade;
            }
        }
        return "原有";
    }
}