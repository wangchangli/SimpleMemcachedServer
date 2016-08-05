package com.cache;

import com.cache.protocol.Command;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by changliwang on 8/2/16.
 *
 * Process the commands.
 */
public class CommandProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandProcessor.class);
    private final Cache cache = CacheFactory.getCache();

    public void handleGet(ChannelHandlerContext ctx, Command command) throws CacheException {
        //VALUE <key> <flags> <bytes> [<cas unique>]\r\n
        //<data block>\r\n
        //END\r\n

        CacheValue cacheValue = cache.get(command.getKey());
        if (cacheValue != null) {
            StringBuilder sb = new StringBuilder();

            sb.append("VALUE ");
            sb.append(command.getKey());
            sb.append(" ");
            sb.append(cacheValue.getFlags());
            sb.append(" ");
            sb.append(cacheValue.getDataBlock().length);
            sb.append(" ");
            sb.append(cacheValue.getCasUnique());
            sb.append("\r\n");
            ctx.write(sb.toString());

            // data block
            ctx.write(cacheValue.getDataBlock());
            ctx.write("\r\n");
        }
        ctx.write("END\r\n");
        ctx.flush();
    }


    public void handleSet(ChannelHandlerContext ctx, Command command) throws CacheException {
        CacheValue cacheValue = new CacheValue(command.getDataBlock());
        cacheValue.setFlags(command.getFlags());
        if (command.getExptime() > 0) {
            cacheValue.setExptime(new Date(System.currentTimeMillis() + command.getExptime() * 1000));
        }
        cache.set(command.getKey(), cacheValue, command.getExptime());

        if (!command.isNoreplay()) {
            ctx.writeAndFlush("STORED\r\n");
        }

    }

    /**
     *
     * @param ctx
     * @param command :
     *
     */
    public void handleDelete(ChannelHandlerContext ctx, Command command) throws CacheException{
        //DELETED\r\n
        //NOT_FOUND\r\n

        boolean deleteResult = cache.delete(command.getKey());

        if(!command.isNoreplay()){
            if(deleteResult){
                ctx.writeAndFlush("DELETED\r\n");
            }else{
                ctx.writeAndFlush("NOT_FOUND\r\n");
            }
        }
    }
}
