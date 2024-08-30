package hama;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbsIntegerEncoderTest {

    @Test
    void testEncode() {
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 10; i++)
            buf.writeInt(i * -1);

        EmbeddedChannel channel = new EmbeddedChannel(new AbsIntegerEncoder());
        assertTrue(channel.writeOutbound(buf));
        assertTrue(channel.finish());

        for (int i = 0; i < 10; i++)
            assertEquals(i, channel.<Integer>readOutbound());
        assertNull(channel.readOutbound());
    }
}
