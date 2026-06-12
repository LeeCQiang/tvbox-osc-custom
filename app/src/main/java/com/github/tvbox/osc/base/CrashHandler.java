package com.github.tvbox.osc.base;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import com.github.tvbox.osc.util.LOG;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 全局未捕获异常处理 — 崩溃日志写入文件 + 友好提示
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String CRASH_DIR = "crash_logs";
    private static final String TAG = "CrashHandler";

    private Context context;
    private Thread.UncaughtExceptionHandler defaultHandler;

    public CrashHandler(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // 保存崩溃日志
        String crashInfo = collectCrashInfo(thread, throwable);
        saveCrashLog(crashInfo);

        // 显示友好提示 (在主线程)
        new Thread(() -> {
            Looper.prepare();
            Toast.makeText(context, "TVBox 遇到异常已重启", Toast.LENGTH_LONG).show();
            Looper.loop();
        }).start();

        // 延迟后重启APP
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}

        restartApp();
    }

    private String collectCrashInfo(Thread thread, Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        pw.println("═══════════════════════════════════════════");
        pw.println("TVBox Crash Report");
        pw.println("Time: " + sdf.format(new Date()));
        pw.println("Thread: " + thread.getName() + " (id=" + thread.getId() + ")");
        pw.println("═══════════════════════════════════════════");
        pw.println();
        throwable.printStackTrace(pw);
        pw.println();
        pw.println("═══════════════════════ Caused by ═══════════════════");
        Throwable cause = throwable.getCause();
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.println("═══════════════════════════════════════════");
        pw.close();
        return sw.toString();
    }

    private void saveCrashLog(String crashInfo) {
        try {
            File dir = new File(context.getExternalCacheDir(), CRASH_DIR);
            if (!dir.exists()) dir.mkdirs();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String fileName = "crash_" + sdf.format(new Date()) + ".log";
            File file = new File(dir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(crashInfo.getBytes("UTF-8"));
            fos.close();

            LOG.e(TAG + ": crash log saved → " + file.getAbsolutePath());
        } catch (Exception e) {
            LOG.e(TAG + ": failed to save crash log: " + e.getMessage());
        }
    }

    private void restartApp() {
        try {
            Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage(context.getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            LOG.e(TAG + ": restart failed: " + e.getMessage());
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
