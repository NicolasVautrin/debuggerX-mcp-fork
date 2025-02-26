package io.debuggerx.protocol.packet;

import io.debuggerx.common.enums.ConnectionType;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author wuou
 */
@Getter
@Setter
public class PacketSource {

    private final ConnectionType sourceType;

    private final Channel channel;

    public PacketSource(ConnectionType sourceType, Channel channel) {
        this.sourceType = sourceType;
        this.channel = channel;
    }

    @Override
    public int hashCode() {
        return channel != null ? channel.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PacketSource that = (PacketSource) o;

        return Objects.equals(channel, that.channel);
    }

    @Override
    public String toString() {
        return sourceType.toString() + "_" + channel.toString();
    }
}
