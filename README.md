### 利用Choreographer.FrameCallback监控卡顿(计算FPS)

Android 系统每个15ms 发出 VSYNC 信号，通知 UI 进行刷新，每一次的同步周期为16.7ms。
SDK 提供了一个相关的回掉，通过计算两次回调的时间差值，我们可以判断是否存在丢帧的现象。

Choreographer设置他的 FrameCallback 函数，每一帧会渲染时都会回掉doFrame(long frameTimeNanos)
函数。如果两次 doFrame 之前的时间间隔大于16.6ms 说明发生了卡顿。

[详细连接参考](https://mp.weixin.qq.com/s/MthGj4AwFPL2JrZ0x1i4fw)