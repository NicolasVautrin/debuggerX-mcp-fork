package io.debuggerx.core.processor;

import io.debuggerx.protocol.packet.JdwpPacket;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 命令包处理
 * @author ouwu
 */
public interface CommandProcessor {
    /**
     * 命令包处理
     * @param byteBuffer data
     * @param packet 数据包
     * @return requestIds
     */
    List<Integer> process(ByteBuffer byteBuffer, JdwpPacket packet);
}