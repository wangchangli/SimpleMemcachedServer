package com.cache.server;

import com.cache.CommandHandler;
import com.cache.protocol.CommandDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by changliwang on 8/2/16.
 */
public class CacheServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast(new CommandDecoder()); //TODO support data black size limitation
        pipeline.addLast(new ResponseEncoder());
        pipeline.addLast(new CommandHandler());
    }
}
