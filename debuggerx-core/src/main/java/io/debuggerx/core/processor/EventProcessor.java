package io.debuggerx.core.processor;

import io.debuggerx.common.exception.DebuggerException;
import io.debuggerx.protocol.enums.EventKind;
import io.debuggerx.protocol.jdwp.IdSizes;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 事件处理器
 *
 * @author wuou
 */
public interface EventProcessor {

    /**
     * 是否支持特殊处理该事件类型
     * @param eventKind 事件类型
     * @return true - 处理 false - 不处理
     */
    boolean supports(EventKind eventKind);

    /**
     * 处理事件
     * @param buffer data
     * @param idSizes idSizes
     * @return requestIds
     * @throws DebuggerException e
     */
    List<Integer> processEvent(ByteBuffer buffer, IdSizes idSizes) throws DebuggerException;
}
