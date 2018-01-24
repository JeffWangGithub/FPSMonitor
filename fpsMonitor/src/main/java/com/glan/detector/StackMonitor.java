package com.glan.detector;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * @title: 负责抓取主线程的堆栈信息
 * @description:
 * @company: Netease
 * @author: GlanWang
 * @version: Created on 18/1/24.
 */

public class StackMonitor {
    private static final String TAG = "FPS";

    private static StackMonitor mInstance = new StackMonitor();

    private HandlerThread mStackMonitorThread = new HandlerThread("stack monitor thread");

    private Handler mIoHandler;

    private static final long TIME_BLOCK = 52L;

    private static List<StackTraceElement[]> stackCacheTemp = new ArrayList<>();

    private static Runnable mLogRunnable = new Runnable() {
        @Override
        public void run() {
            StackTraceElement[] stackTrace = Looper.getMainLooper().getThread().getStackTrace();
            stackCacheTemp.add(stackTrace);
        }
    };


    private StackMonitor() {
        mStackMonitorThread.start();
        mIoHandler = new Handler(mStackMonitorThread.getLooper());
    }

    public static StackMonitor getInstance() {
        return mInstance;
    }

    public void startMonitor() {
        mIoHandler.postDelayed(mLogRunnable, TIME_BLOCK);
    }

    public String removeMonitor(boolean isPrint) {
        mIoHandler.removeCallbacks(mLogRunnable);
        StringBuilder sb = new StringBuilder();
        if (isPrint) {
            for (int i = 0, size = stackCacheTemp.size(); i < size; i++) {
                StackTraceElement[] stackTraceElements = stackCacheTemp.get(i);
                for (StackTraceElement s : stackTraceElements) {
                    sb.append(s.toString() + "\n");
                }
                Log.e(TAG, sb.toString());
            }
        }
        stackCacheTemp.clear();
        return sb.toString();
    }
}
