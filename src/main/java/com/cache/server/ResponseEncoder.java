package com.cache.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;

/**
 * Created by changliwang on 8/4/16.
 *
 * Encode the response to bytes before we return it.
 */
public class ResponseEncoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf out) throws Exception {
        if(msg instanceof CharSequence){
            out.writeCharSequence((CharSequence) msg, Charset.defaultCharset());
        }else{
            out.writeBytes((byte[])msg);
        }
    }
}
