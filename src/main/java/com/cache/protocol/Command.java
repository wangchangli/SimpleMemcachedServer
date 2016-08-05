package com.cache.protocol;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by changliwang on 8/2/16.
 *
 * Represent a command.
 */
public class Command {

    private Cmd cmd;
    private String key;
    private int flags;
    private int exptime = 0;
    private int bytes;
    private String casUnique;
    private boolean noreplay;
    private byte[] dataBlock;
    private String decodeError;
    private boolean isStorage;

    public void setKey(String key) {
        this.key = key;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void setExptime(int exptime) {
        this.exptime = exptime;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public void setCasUnique(String casUnique) {
        this.casUnique = casUnique;
    }

    public void setNoreplay(boolean noreplay) {
        this.noreplay = noreplay;
    }

    public int getExptime() {
        return exptime;
    }

    public boolean isNoreplay() {
        return noreplay;
    }

    public int getFlags() {
        return flags;
    }

    public void setCmd(Cmd cmd) {
        this.cmd = cmd;
    }

    public String getKey() {
        return key;
    }

    public boolean isStorage() {
        return isStorage;
    }

    public int getBytes() {
        return bytes;
    }

    public Cmd getCmd() {
        return cmd;
    }

    public void setDataBlock(byte[] dataBlock) {
        this.dataBlock = dataBlock;
    }

    public byte[] getDataBlock() {
        return dataBlock;
    }

    public void setDecodeError(String decodeError) {
        this.decodeError = decodeError;
    }

    public String getDecodeError() {
        return decodeError;
    }

    public void setIsStorage(boolean isStorage) {
        this.isStorage = isStorage;
    }

    final public static int maxSizeAllowedForKey = 250;

    public static Command parseCommand(String commandLine) {
        // only support set/get/delete
        Command command = new Command();
        try {
            if (StringUtils.startsWith(commandLine, "get ") || StringUtils.startsWith(commandLine, "delete ")) {
                //get <key>*\r\n\
                //delete <key> [noreply]\r\n

                String commandFields[] = commandLine.split(" ");
                if (commandFields.length < 2 || commandFields.length > 3) {
                    throw new IllegalArgumentException("Bad number of args passed");
                }
                String key = commandFields[1];
                checkKey(key);

                boolean noreplay = false;
                if (commandFields.length == 3 && StringUtils.equals("noreplay", commandFields[2])) {
                    noreplay = true;
                }
                command.setCmd(Cmd.Of(commandFields[0]));
                command.setKey(key);
                command.setNoreplay(noreplay);

            } else if (StringUtils.startsWith(commandLine, "set ")) {
                command = parseStorageCommand(commandLine);
            } else {
                command.setDecodeError("ERROR\r\n"); // unsupport command
            }
        } catch (IllegalArgumentException e) {
            command.setDecodeError("CLIENT_ERROR " + e.getMessage() + "\r\n"); //some sort of client error in the input line
        }

        return command;
    }

    private static Command parseStorageCommand(String commandLine) {
        //<command name> <key> <flags> <exptime> <bytes> [noreply]\r\n
        //cas <key> <flags> <exptime> <bytes> <cas unique> [noreply]\r\n

        String commandField[] = commandLine.split(" ");

        String cmd = commandField[0];
        if (StringUtils.equals("cas", cmd)) {
            if (commandField.length < 6 || commandField.length > 7) {
                throw new IllegalArgumentException("Bad number of args passed");
            }
        } else if (commandField.length < 5 || commandField.length > 6) {
            throw new IllegalArgumentException("Bad number of args passed");
        }

        String key = commandField[1];
        checkKey(key);

        int flags;
        int exptime;
        int bytes;
        try {
            flags = Integer.parseInt(commandField[2]);
            exptime = Integer.parseInt(commandField[3]);
            bytes = Integer.parseInt(commandField[4]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parse client value failed");
        }
        String casUnique = null;

        boolean noreplay = false;
        if ("cas".equals(cmd)) {
            casUnique = commandField[5];
            if (commandField.length == 7)
                if ("noreply".equals(commandField[6])) {
                    noreplay = true;
                }
        } else if (commandField.length == 6) {
            if ("noreply".equals(commandField[5])) {
                noreplay = true;
            }
        }

        Command command = new Command();
        command.setIsStorage(true);
        command.setCmd(Cmd.Of(cmd));
        command.setKey(key);
        command.setFlags(flags);
        command.setExptime(exptime);
        command.setBytes(bytes);
        command.setCasUnique(casUnique);
        command.setNoreplay(noreplay);

        return command;
    }

    private static void checkKey(String key) {
        if (key.length() > maxSizeAllowedForKey) {
            throw new IllegalArgumentException("key is two big " + key);
        }
    }
}
