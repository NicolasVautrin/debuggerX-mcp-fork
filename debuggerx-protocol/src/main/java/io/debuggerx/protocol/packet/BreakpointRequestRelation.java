package io.debuggerx.protocol.packet;

import io.debuggerx.protocol.enums.EventKind;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * @author wuou
 */
@Getter
@Setter
public class BreakpointRequestRelation {

    private final EventKind eventKind;

    private final Integer requestId;

    public BreakpointRequestRelation(EventKind eventKind, Integer requestId) {
        this.eventKind = eventKind;
        this.requestId = requestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BreakpointRequestRelation that = (BreakpointRequestRelation) o;
        return eventKind == that.eventKind && Objects.equals(requestId, that.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventKind, requestId);
    }

    /**
     * 根据事件类型和请求ID生成字节数组
     * @return 5字节数组 [eventKind(1字节) | requestId的4字节大端编码]
     * @throws NullPointerException 如果参数为null
     */
    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(1 + Integer.BYTES)
                .order(ByteOrder.BIG_ENDIAN)
                .put(eventKind.getId())
                .putInt(requestId);

        return buffer.array();
    }
}
