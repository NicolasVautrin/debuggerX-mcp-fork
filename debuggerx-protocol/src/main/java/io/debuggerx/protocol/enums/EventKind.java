package io.debuggerx.protocol.enums;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 事件类型
 * @author ouwu
 * @see <a href="https://docs.oracle.com/javase/8/docs/platform/jpda/jdwp/jdwp-protocol.html#JDWP_EventKind">EventKind</a>
 */

public enum EventKind {
    /**
     * 单步执行事件 - 当线程完成单步操作时触发
     */
    SINGLE_STEP(1),

    /**
     * 断点事件 - 当线程执行到断点位置时触发
     */
    BREAKPOINT(2),

    /**
     * 栈帧弹出事件 - 当栈帧被弹出调用栈时触发(已废弃)
     */
    FRAME_POP(3),

    /**
     * 异常事件 - 当异常被抛出且未被捕获时触发
     */
    EXCEPTION(4),

    /**
     * 用户自定义事件 - 用于扩展的自定义事件类型
     */
    USER_DEFINED(5),

    /**
     * 线程启动事件 - 当新线程启动时触发
     */
    THREAD_START(6),

    /**
     * 线程终止事件 - 当线程结束时触发
     */
    THREAD_DEATH(7),

    /**
     * 类准备事件 - 当类完成准备阶段(Preparation)时触发
     */
    CLASS_PREPARE(8),

    /**
     * 类卸载事件 - 当类被卸载时触发
     */
    CLASS_UNLOAD(9),

    /**
     * 类加载事件 - 当类被加载时触发(已废弃)
     */
    CLASS_LOAD(10),

    /**
     * 字段访问事件 - 当字段被访问时触发(需要canWatchFieldAccess能力)
     */
    FIELD_ACCESS(20),

    /**
     * 字段修改事件 - 当字段被修改时触发(需要canWatchFieldModification能力)
     */
    FIELD_MODIFICATION(21),

    /**
     * 异常捕获事件 - 当异常被catch块捕获时触发
     */
    EXCEPTION_CATCH(30),

    /**
     * 方法进入事件 - 当进入方法时触发
     */
    METHOD_ENTRY(40),

    /**
     * 方法退出事件 - 当方法正常返回时触发
     */
    METHOD_EXIT(41),

    /**
     * 带返回值的方法退出事件 - 当方法返回并携带返回值时触发(JDWP 1.6+)
     */
    METHOD_EXIT_WITH_RETURN_VALUE(42),

    /**
     * 监视器争用进入事件 - 当线程尝试进入被占用的监视器时触发(JDWP 1.6+)
     */
    MONITOR_CONTENDED_ENTER(43),

    /**
     * 监视器已进入事件 - 当线程成功进入被争用的监视器时触发(JDWP 1.6+)
     */
    MONITOR_CONTENDED_ENTERED(44),

    /**
     * 监视器等待事件 - 当线程在监视器上等待时触发(JDWP 1.6+)
     */
    MONITOR_WAIT(45),

    /**
     * 监视器结束等待事件 - 当线程结束监视器等待时触发(JDWP 1.6+)
     */
    MONITOR_WAITED(46),

    /**
     * 虚拟机启动事件 - 当VM初始化完成时自动触发
     */
    VM_START(90),

    /**
     * 虚拟机终止事件 - 当VM完全终止时自动触发
     */
    VM_DEATH(99),

    /**
     * 虚拟机断开事件 - 当调试器与VM断开连接时触发(不通过JDWP协议发送)
     */
    VM_DISCONNECTED(100);

    public final byte id;

    EventKind(int id) {
        this.id = (byte) id;
    }

    public static EventKind findByValue(byte value) {
        return Arrays.stream(EventKind.values())
                .filter(eventKind -> eventKind.id == value)
                .findAny().orElse(null);
    }

    public static EventKind read(ByteBuffer byteBuffer) {
        return findByValue(byteBuffer.get());
    }

    public final byte getId() {
        return id;
    }

}
