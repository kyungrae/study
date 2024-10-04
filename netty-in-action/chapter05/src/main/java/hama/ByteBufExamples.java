package hama;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ByteProcessor;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;

public class ByteBufExamples {

    private static final Random random = new Random();

    public static void heapBuf() {
        ByteBuf heapBuf = Unpooled.buffer();
        byte[] array = heapBuf.array();
        int offset = heapBuf.arrayOffset() + heapBuf.readerIndex();
        int length = heapBuf.readableBytes();
        handleArray(array, offset, length);
    }

    public static void directBuf() {
        ByteBuf directBuf = Unpooled.buffer();
        if (!directBuf.hasArray()) {
            int length = directBuf.readableBytes();
            byte[] array = new byte[length];
            directBuf.getBytes(directBuf.readerIndex(), array);
            handleArray(array, 0, length);
        }
    }

    public static void byteBufferComposite(ByteBuffer header, ByteBuffer body) {
        ByteBuffer message = ByteBuffer.allocate(header.remaining() + body.remaining());
        message.put(header);
        message.put(body);
        message.flip();
    }

    public static void compositeByteBuf() {
        CompositeByteBuf messageBuf = Unpooled.compositeBuffer();
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();

        messageBuf.addComponents(header, body);

        for (ByteBuf buf : messageBuf) {
            System.out.println(buf.toString());
        }
    }

    public static void byteBufCompositeArray() {
        CompositeByteBuf compBuf = Unpooled.compositeBuffer();
        int length = compBuf.readableBytes();
        byte array[] = new byte[length];
        compBuf.getBytes(compBuf.readerIndex(), array);
        handleArray(array, 0, array.length);
    }

    public static void byteBufRelativeAccess() {
        ByteBuf buffer = Unpooled.buffer();
        for (int i = 0; i < buffer.capacity(); i++) {
            byte b = buffer.getByte(i);
            System.out.println((char) b);
        }
    }

    public static void readAllData() {
        ByteBuf buffer = Unpooled.buffer();
        while (buffer.isReadable()) {
            System.out.println(buffer.readByte());
        }
    }

    public static void writeAllData() {
        ByteBuf buffer = Unpooled.buffer();
        while (buffer.writableBytes() >= 4) {
            buffer.writeInt(random.nextInt());
        }
    }

    public static void byteProcessor() {
        ByteBuf buffer = Unpooled.buffer();
        int index = buffer.forEachByte(ByteProcessor.FIND_CR);
    }

    public static void byteBufSlice() {
        Charset utf8 = CharsetUtil.UTF_8;
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        ByteBuf sliced = buf.slice(0, 14);
        System.out.println(sliced.toString(utf8));
        buf.setByte(0, (byte) 'J');
        assert buf.getByte(0) == sliced.getByte(0);
    }

    public static void byteBufCopy() {
        Charset utf8 = CharsetUtil.UTF_8;
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        ByteBuf copy = buf.copy(0, 14);
        System.out.println(copy.toString(utf8));
        buf.setByte(0, (byte) 'J');
        assert buf.getByte(0) != copy.getByte(0);
    }

    public static void byteBufSetGet() {
        Charset utf8 = CharsetUtil.UTF_8;
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        System.out.println((char) buf.getByte(0));
        int readerIndex = buf.readerIndex();
        int writerIndex = buf.writerIndex();
        buf.setByte(0, (byte) 'B');
        System.out.println((char) buf.getByte(0));
        assert readerIndex == buf.readerIndex();
        assert writerIndex == buf.writerIndex();
    }

    public static void byteBufWriteRead() {
        Charset utf8 = CharsetUtil.UTF_8;
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        System.out.println((char) buf.readByte());
        int readerIndex = buf.readerIndex();
        int writerIndex = buf.writerIndex();
        buf.writeByte((byte) '?');
        assert readerIndex == buf.readerIndex();
        assert writerIndex != buf.writerIndex();
    }

    public static void obtainingByteBufAllocatorReference(Channel channel, ChannelHandlerContext ctx) {
        ByteBufAllocator allocator1 = channel.alloc();
        ByteBufAllocator allocator2 = ctx.alloc();
    }

    public static void referenceCounting(Channel channel) {
        ByteBufAllocator allocator = channel.alloc();
        ByteBuf buf = allocator.directBuffer();
        assert buf.refCnt() == 1;
    }

    public static void releaseReferenceCountedObject() {
        ByteBuf buf = Unpooled.buffer();
        buf.release();
    }

    private static void handleArray(byte[] array, int offset, int len) {
    }
}
