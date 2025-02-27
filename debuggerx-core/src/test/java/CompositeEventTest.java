import io.debuggerx.common.constants.JdwpConstants;
import io.debuggerx.common.utils.CollectionUtils;
import io.debuggerx.core.processor.CommandProcessor;
import io.debuggerx.core.processor.registry.CommandProcessorRegistry;
import io.debuggerx.core.processor.registry.EventProcessorRegistry;
import io.debuggerx.protocol.enums.CommandIdentifier;
import io.debuggerx.protocol.packet.JdwpHeader;
import io.debuggerx.protocol.packet.JdwpPacket;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author 吴欧(欧弟)
 */
public class CompositeEventTest {

    private static final EventProcessorRegistry eventProcessors = new EventProcessorRegistry();
    private final CommandProcessorRegistry commandProcessors = new CommandProcessorRegistry(eventProcessors);

    @Test
    public void testCompositeEventWithMethodEntry() {
        byte[] packet = new byte[]{0, 0, 0, -27, 0, 0, 2, -100, 0, 64, 100, 0, 0, 0, 0, 3, 8, 0, 0, 0, 47, 0, 0, 0, 0, 0, 0, -37, 24, 1, 0, 0, 0, 0, 0, 0, -36, -38, 0, 0, 0, 41, 76, 115, 117, 110, 47, 114, 101, 102, 108, 101, 99, 116, 47, 71, 101, 110, 101, 114, 97, 116, 101, 100, 77, 101, 116, 104, 111, 100, 65, 99, 99, 101, 115, 115, 111, 114, 55, 53, 53, 54, 59, 0, 0, 0, 3, 8, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, -37, 24, 1, 0, 0, 0, 0, 0, 0, -36, -38, 0, 0, 0, 41, 76, 115, 117, 110, 47, 114, 101, 102, 108, 101, 99, 116, 47, 71, 101, 110, 101, 114, 97, 116, 101, 100, 77, 101, 116, 104, 111, 100, 65, 99, 99, 101, 115, 115, 111, 114, 55, 53, 53, 54, 59, 0, 0, 0, 7, 8, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, -37, 24, 1, 0, 0, 0, 0, 0, 0, -36, -38, 0, 0, 0, 41, 76, 115, 117, 110, 47, 114, 101, 102, 108, 101, 99, 116, 47, 71, 101, 110, 101, 114, 97, 116, 101, 100, 77, 101, 116, 104, 111, 100, 65, 99, 99, 101, 115, 115, 111, 114, 55, 53, 53, 54, 59, 0, 0, 0, 7};

        JdwpHeader header = JdwpHeader.fromBytes(packet);

        int dataLength = header.getLength() - JdwpConstants.HEADER_LENGTH;

        // 读取数据部分
        byte[] data = new byte[dataLength];
        for (int i = JdwpConstants.HEADER_LENGTH, j = 0; i < packet.length; i++, j++) {
            data[j] = packet[i];
        }

        JdwpPacket jdwpPacket = new JdwpPacket(header, data);

        CommandIdentifier commandIdentifier = CommandIdentifier.of(header);

        CommandProcessor processor = commandProcessors.getProcessor(commandIdentifier);

        List<Integer> requestIds = processor.process(ByteBuffer.wrap(data), jdwpPacket);

        Assert.assertFalse(CollectionUtils.isEmpty(requestIds));

    }

}
