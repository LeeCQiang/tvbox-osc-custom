package com.github.tvbox.osc.base;

import android.app.Activity;
import androidx.multidex.MultiDexApplication;

import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.callback.EmptyCallback;
import com.github.tvbox.osc.callback.LoadingCallback;
import com.github.tvbox.osc.data.AppDataManager;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.util.AppManager;
import com.github.tvbox.osc.util.EpgUtil;
import com.github.tvbox.osc.util.FileUtils;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.LOG;
import com.github.tvbox.osc.util.OkGoHelper;
import com.github.tvbox.osc.util.PlayerHelper;
import com.kingja.loadsir.core.LoadSir;
import com.orhanobut.hawk.Hawk;
import com.p2p.P2PClass;
import com.whl.quickjs.android.QuickJSLoader;
import com.github.catvod.crawler.JsLoader;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.unit.Subunits;

/**
 * @author pj567
 * @date :2020/12/17
 * @description:
 */
public class App extends MultiDexApplication {
    private static App instance;

    private static P2PClass p;
    public static String burl;
    private static String dashData;

    // 延迟初始化任务
    private android.os.Handler delayHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        delayHandler = new android.os.Handler(getMainLooper());
        // 全局崩溃捕获
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        initParams();
        // 必须立即初始化的
        OkGoHelper.init();
        LoadSir.beginBuilder()
                .addCallback(new EmptyCallback())
                .addCallback(new LoadingCallback())
                .commit();
        AutoSizeConfig.getInstance().setCustomFragment(true).getUnitsManager()
                .setSupportDP(false)
                .setSupportSP(false)
                .setSupportSubunits(Subunits.MM);
        // 延迟非关键初始化（不影响首页显示）
        delayHandler.postDelayed(() -> {
            ControlManager.init(this);
            AppDataManager.init();
            PlayerHelper.init();
            EpgUtil.init();
            QuickJSLoader.init();
            FileUtils.cleanPlayerCache();
            // 自动清理7天前的缓存（后台线程）
            new Thread(() -> {
                try {
                    java.io.File cacheDir = new java.io.File(getFilesDir(), "cache");
                    if (cacheDir.exists()) {
                        long now = System.currentTimeMillis();
                        long weekMs = 7L * 24 * 60 * 60 * 1000;
                        java.io.File[] files = cacheDir.listFiles();
                        if (files != null) {
                            for (java.io.File f : files) {
                                if (now - f.lastModified() > weekMs) f.delete();
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }).start();
        }, 300);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (delayHandler != null) {
            delayHandler.removeCallbacksAndMessages(null);
        }
        JsLoader.destroy();
    }

    private void initParams() {
        // Hawk
        Hawk.init(this).build();
        Hawk.put(HawkConfig.DEBUG_OPEN, false);
        if (!Hawk.contains(HawkConfig.PLAY_TYPE)) {
            Hawk.put(HawkConfig.PLAY_TYPE, 1);
        }
    }

    public static App getInstance() {
        return instance;
    }

    // onTerminate moved to the onCreate section to avoid duplication


    private VodInfo vodInfo;
    public void setVodInfo(VodInfo vodinfo){
        this.vodInfo = vodinfo;
    }
    public VodInfo getVodInfo(){
        return this.vodInfo;
    }

    public static P2PClass getp2p() {
        try {
            if (p == null) {
                p = new P2PClass(FileUtils.getExternalCachePath());
            }
            return p;
        } catch (Exception e) {
            LOG.e(e.toString());
            return null;
        }
    }

    public Activity getCurrentActivity() {
        return AppManager.getInstance().currentActivity();
    }

    public void setDashData(String data) {
        dashData = data;
    }
    public String getDashData() {
        return dashData;
    }
}