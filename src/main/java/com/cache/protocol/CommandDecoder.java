package com.cache.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by changliwang on 8/3/16.
 *
 * Decode command from the byte stream.
 */
public class CommandDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandDecoder.class);

    private boolean needDataBlock = false;
    private int blockSize = 0;
    private Command storageCommand;

    private final ByteBuf lineDelimiter = Unpooled.wrappedBuffer(new byte[]{(byte) 13, (byte) 10});

    private static int indexOf(ByteBuf haystack, ByteBuf needle) {
        for (int i = haystack.readerIndex(); i < haystack.writerIndex(); i ++) {
            int haystackIndex = i;
            int needleIndex;
            for (needleIndex = 0; needleIndex < needle.capacity(); needleIndex ++) {
                if (haystack.getByte(haystackIndex) != needle.getByte(needleIndex)) {
                    break;
                } else {
                    haystackIndex ++;
                    if (haystackIndex == haystack.writerIndex() &&
                            needleIndex != needle.capacity() - 1) {
                        return -1;
                    }
                }
            }

            if (needleIndex == needle.capacity()) {
                // Found the needle from the haystack!
                return i - haystack.readerIndex();
            }
        }
        return -1;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        if(!needDataBlock) { //get the command line
            int frameLength = indexOf(byteBuf, lineDelimiter);
            if(frameLength == -1){
                return;
            }

            ByteBuf frame = byteBuf.readRetainedSlice(frameLength);
            byteBuf.skipBytes(lineDelimiter.capacity());

            String commandLine = frame.toString(Charset.defaultCharset());
            ReferenceCountUtil.release(frame);

            LOGGER.debug("receive a command "+ commandLine);
            Command command = Command.parseCommand(commandLine);

            if (command.isStorage()){
                needDataBlock = true;
                blockSize = command.getBytes(); // store the data block size
                storageCommand  = command; // reserve the storage command
            }else{
                out.add(command);
            }

        }else{ // get the data block
            if(byteBuf.readableBytes() < blockSize + 2){
                return;
            }
            ByteBuf dataBlock = byteBuf.readRetainedSlice(blockSize);
            ByteBuf lastTwoBytes = byteBuf.readRetainedSlice(2);

            if(indexOf(lastTwoBytes, lineDelimiter) != 0){ //data block does not end with \r\n
                storageCommand.setDecodeError("CLIENT_ERROR Parse client value failed\r\n");
            }else {
                byte[] data = new byte[dataBlock.readableBytes()];
                dataBlock.readBytes(data);
                storageCommand.setDataBlock(data);
            }

            // release the bytebufs we create
            ReferenceCountUtil.release(dataBlock);
            ReferenceCountUtil.release(lastTwoBytes);

            out.add(storageCommand);

            // clean the state
            needDataBlock = false;
            blockSize = 0;
            storageCommand = null;
        }
    }
}
