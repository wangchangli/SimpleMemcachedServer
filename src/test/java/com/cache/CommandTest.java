package com.cache;

import com.cache.protocol.Cmd;
import com.cache.protocol.Command;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by changliwang on 8/3/16.
 */
public class CommandTest  {

    @Test
    public void parseSetCommandTest(){
        Command command = Command.parseCommand("set key1 0 10 6");
        Assert.assertEquals(Cmd.set, command.getCmd());
        Assert.assertEquals("key1", command.getKey());
        Assert.assertEquals(0, command.getFlags());
        Assert.assertEquals(10, command.getExptime());
    }

    @Test
    public void parseGetCommandTest(){
        Command command = Command.parseCommand("get key1");
        Assert.assertEquals(Cmd.get, command.getCmd());
        Assert.assertEquals("key1", command.getKey());
    }

    @Test
    public void parseDeleteCommandTest(){
        Command command = Command.parseCommand("delete key1");
        Assert.assertEquals(Cmd.delete, command.getCmd());
        Assert.assertEquals("key1", command.getKey());
    }

    @Test
    public void parseNonSupportCommandTest(){
        Command command = Command.parseCommand("aa");
        Assert.assertEquals("ERROR\r\n", command.getDecodeError());
    }

    @Test
    public void parseBadArgsNumCommandTest(){
        Command command = Command.parseCommand("set 1 2");
        Assert.assertEquals("CLIENT_ERROR Bad number of args passed\r\n", command.getDecodeError());
    }
    @Test
    public void parseInvalidArgsCommandTest(){
        Command command = Command.parseCommand("set a b c d e");
        Assert.assertEquals("CLIENT_ERROR Parse client value failed\r\n", command.getDecodeError());
    }
}
