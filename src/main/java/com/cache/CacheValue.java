package com.cache;

import java.util.Date;

/**
 * Created by changliwang on 8/2/16.
 *
 * The value we save in the cache.
 */
public class CacheValue {

    private int flags;
    private byte[] dataBlock;
    private long casUnique;
    private Date exptime;

    public CacheValue(byte[] dataBlock) {
        this.dataBlock = dataBlock;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getFlags() {
        return flags;
    }

    public byte[] getDataBlock() {
        return dataBlock;
    }

    public long getCasUnique() {
        return casUnique;
    }

    public void setCasUnique(long casUnique){
        this.casUnique = casUnique;
    }

    public Date getExptime() {
        return exptime;
    }

    public void setExptime(Date exptime) {
        this.exptime = exptime;
    }

    public boolean isExpired(){
        if( exptime != null && exptime.before(new Date())){
            return true;
        }
        return false;
    }
}
