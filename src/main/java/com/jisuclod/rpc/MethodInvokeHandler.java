package com.jisuclod.rpc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSON;

public class MethodInvokeHandler extends IoHandlerAdapter{
	
	private Map<String,RpcRegister> rpcRegisteres = new HashMap<String, RpcRegister>();
	
	private IoSession session;
	
	private LinkedBlockingQueue<MehtodResponse> resultQueue = new LinkedBlockingQueue<MehtodResponse>();
	
	public MethodInvokeHandler(Map<String,RpcRegister> rpcRegisteres){
		this.rpcRegisteres = rpcRegisteres;
	}
	
	 /**
     * 连接创建事件
     */
    @Override
    public void sessionCreated(IoSession session){
        this.session = session;
    }
    
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
    
    /**
     * 消息接收事件
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
    	if (message.toString().contains("method")){
    		MehtodInvoke mehtodInvoke =  JSON.parseObject(message.toString(), MehtodInvoke.class);
        	MehtodResponse response  = invokeLocal(mehtodInvoke);
        	String retStr = JSON.toJSONString(response);
        	session.write(retStr);
    	}else if (message.toString().contains("result")){
    		MehtodResponse mehtodResponse =  JSON.parseObject(message.toString(), MehtodResponse.class);
    		resultQueue.add(mehtodResponse);
    	}
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("Server IDLE" + session.getIdleCount(status));
    }
    
    public MehtodResponse invokeLocal(MehtodInvoke method){
    	return null;
    }
    
    public MehtodResponse invokeRemote(MehtodInvoke method){
    	String invokeJson = JSON.toJSONString(method);
		System.out.println("invokeJson:"+invokeJson);
		session.write(invokeJson);
		MehtodResponse response = null;
		try{
			while(true){
				response = resultQueue.take();
				if (response.getId().equals(method.getId())){
					return response;
				}else{
					resultQueue.add(response);
					Thread.sleep(1);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
    	return null;
    }
    
}
