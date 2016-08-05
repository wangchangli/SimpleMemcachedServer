package com.cache;

import com.cache.impl.ConcurrentHashMapCache;
import com.cache.impl.GuavaCache;
import com.cache.protocol.Cmd;
import com.cache.protocol.Command;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by changliwang on 8/2/16.
 * <p>
 * Dispatch commands.
 */
@ChannelHandler.Sharable
public class CommandHandler extends SimpleChannelInboundHandler<Command> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

    private CommandProcessor commandProcessor;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        commandProcessor = new CommandProcessor();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        if (command.getDecodeError() != null) {
            ctx.writeAndFlush(command.getDecodeError());
            return;
        }
        Cmd cmd = command.getCmd();
        try {
            switch (cmd) {
                case get:
                    commandProcessor.handleGet(ctx, command);
                    break;
                case set:
                    commandProcessor.handleSet(ctx, command);
                    break;
                case delete:
                    commandProcessor.handleDelete(ctx, command);
                    break;
                default:
                    ctx.writeAndFlush("SERVER_ERROR not supported command \r\n");
                    break;
            }
        } catch (CacheException e) {
            ctx.writeAndFlush("SERVER_ERROR " + e.getMessage() + "\r\n");
            ctx.close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error(cause.getMessage(), cause);
        ctx.close();
    }
}
