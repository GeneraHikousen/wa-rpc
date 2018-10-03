package org.wa.remoting.model;

/**
 * @Auther: XF
 * @Date: 2018/10/3 18:33
 * @Description:
 */
public class ByteHolder {
    private transient byte[] bytes;
    public byte[] bytes(){
        return bytes;
    }
    public void bytes(byte[] bytes){
        this.bytes = bytes;
    }
    public int size(){
        return bytes == null? 0 : bytes.length;
    }
}
