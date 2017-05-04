package com.jisuclod.rpc;

import java.io.Serializable;  

/** 
 * 文件上传对象 
 */  
public class FileUploadRequest implements Serializable  
{  
    /** 
     * 序列号 
     */  
    private static final long serialVersionUID = 1547212123169330600L;  
    
    /** 
     * 文件名称
     */  
    private String id;  
      
    /** 
     * 文件字节 
     */  
    private byte [] bytes;
    
    
  
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public byte[] getBytes()   
    {  
        return bytes;  
    }  
  
    public void setBytes(byte[] bytes)   
    {  
        this.bytes = bytes;  
    }  
  
}  