package com.glan.detector;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Choreographer;

import java.util.concurrent.TimeUnit;

/**
 * @title: 根据Choreographer.getInstance().postFrameCallback 计算 fps
 * @description:
 * @company: Netease
 * @author: GlanWang
 * @version: Created on 18/1/19.
 */

public class FPSMonitor {
    private static final String TAG = "FPS";

    private static final int DEFAULT_DRAWE_GAP_TIME = 112; //两次 doFrame的耗时阀值(7*16),超过这个时间认定为一次卡顿

    private static final int DEFAULT_CALCULATE_FPS_GAP = 500;//计算 fps 的时间间隔 单位毫秒

    private static Params params = new Params();

    private FPSMonitor() {
    }

    /**
     * 定义一次卡顿的阀值，默认为112毫秒(7*16ms)
     */
    public static Params frameValve(int frameValve) {
        params.frameValve(frameValve);
        return params;
    }

    /**
     * 计算平均 fps 的时间，默认为500ms 计算一次 fps
     * @param fpsCalcuValve
     * @return
     */
    public static Params FPSCalcuValve(int fpsCalcuValve) {
        params.FPSCalcuValve(fpsCalcuValve);
        return params;
    }

    /**
     * 使用默认策略，卡顿阀值112毫秒； 平均 fps 计算时间为500ms
     * @return
     */
    public static Params defaultPolicy() {
        return params;
    }

    public static Params params() {
        return params;
    }

    private static void start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
                long lastFrameTimeNanos = 0;
                long currentFrameTimeNanos = 0;
                long lastOverDraweTimeNanos = 0;
                long renderCount = 0;

                @Override
                public void doFrame(long frameTimeNanos) {
                    //计算瞬时的 fps
                    boolean isFrameLoss = calculateDoFrameTime(frameTimeNanos);
                    //计算均值 fps
                    calculateFPS(frameTimeNanos);
                    //开启堆栈的监控，定时抓去堆栈信息
                    String stackInfo = StackMonitor.getInstance().removeMonitor(isFrameLoss);
                    FPSChanged fpsListener = params.getFpsListener();
                    if (fpsListener != null && !TextUtils.isEmpty(stackInfo)) {
                        fpsListener.onStackChanged(stackInfo);
                    }
                    StackMonitor.getInstance().startMonitor();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        Choreographer.getInstance().postFrameCallback(this);
                    }
                }

                /**
                 * 计算两次 doFrame 的时间
                 * @param frameTimeNanos
                 */
                private boolean calculateDoFrameTime(long frameTimeNanos) {
                    boolean result = false;
                    if (lastFrameTimeNanos == 0) {
                        lastFrameTimeNanos = frameTimeNanos;
                    }
                    currentFrameTimeNanos = frameTimeNanos;
                    long gap = TimeUnit.MILLISECONDS.convert(currentFrameTimeNanos - lastFrameTimeNanos, TimeUnit.NANOSECONDS);
                    if (gap > params.getFrameValve()) {
                        result = true;
                        Log.d(TAG, "gap = " + gap + " --- 瞬时 FPS = " + 1000/(double)gap);
                    }
                    lastFrameTimeNanos = frameTimeNanos;
                    return result;
                }

                /**
                 * 计算一段时间内的均值fps
                 * @param frameTimeNanos
                 */
                private void calculateFPS(long frameTimeNanos) {
                    if (lastOverDraweTimeNanos == 0) {
                        lastOverDraweTimeNanos = frameTimeNanos;
                    }
                    renderCount++;
                    long lastOverDraweTimeDiff = TimeUnit.MILLISECONDS.convert(frameTimeNanos - lastOverDraweTimeNanos, TimeUnit.NANOSECONDS);
                    if (lastOverDraweTimeDiff > params.getFPSCalcuValve()) {
                        final double fps = renderCount * 1000 / (double) lastOverDraweTimeDiff;
                        Log.d(TAG, "均值 FPS = " + (int)fps);
                        renderCount = 0;
                        lastOverDraweTimeNanos = frameTimeNanos;
                        FPSChanged listener = params.getFpsListener();
                        if (listener != null) {
                            listener.onFPSChanged((int) fps);
                        }
                    }
                }
            });
        }
    }


    public static class Params {
        private int frameValve = DEFAULT_DRAWE_GAP_TIME;

        private int fpsCalculateValve = DEFAULT_CALCULATE_FPS_GAP;

        private FPSChanged fpsListener;

        /**
         * 定义一次卡顿的阀值，默认为112毫秒(7*16ms)
         */
        public Params frameValve(int frameValve) {
            this.frameValve = frameValve;
            return this;
        }

        /**
         * 计算平均 fps 的时间，默认为500ms 计算一次 fps
         * @param fpsCalcuValve
         * @return
         */
        public Params FPSCalcuValve(int fpsCalcuValve) {
            this.fpsCalculateValve = fpsCalcuValve;
            return this;
        }

        public Params FPSListener(FPSChanged listener) {
            this.fpsListener = listener;
            return this;
        }

        public FPSChanged getFpsListener() {
            return fpsListener;
        }

        public int getFrameValve () {
            return frameValve;
        }

        public int getFPSCalcuValve() {
            return fpsCalculateValve;
        }

        public void start() {
            FPSMonitor.start();
        }
    }

    public interface FPSChanged {
        void onFPSChanged(int fps);

        void onStackChanged(String stackInfo);
    }

}
