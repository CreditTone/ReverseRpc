package com.jisuclod.rpc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class MethodInvokeHandler extends IoHandlerAdapter{
	
	private Map<String,RpcRegister> rpcRegisteres = new HashMap<String, RpcRegister>();
	
	private LinkedBlockingQueue<IoSession>  sessions = new LinkedBlockingQueue<IoSession>();
	
	private LinkedBlockingQueue<MehtodResponse> resultQueue = new LinkedBlockingQueue<MehtodResponse>();
	
	public MethodInvokeHandler(Map<String,RpcRegister> rpcRegisteres){
		this.rpcRegisteres = rpcRegisteres;
	}
	
	public LinkedBlockingQueue<IoSession> getSessions(){
		return sessions;
	}
	
	 /**
     * 连接创建事件
     */
    @Override
    public void sessionCreated(IoSession session){
    	sessions.add(session);
    }
    
    
    @Override
	public synchronized void sessionClosed(IoSession session) throws Exception {
    	sessions.remove(session);
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
    	}else{
    		JSONObject resultJson = JSON.parseObject(message.toString());
    		Object result = resultJson.remove("result");
    		MehtodResponse mehtodResponse = JSON.parseObject(resultJson.toString(), MehtodResponse.class); 
    		if (result != null){
    			if (result instanceof JSON){
    				Class resultCls = Class.forName(mehtodResponse.getResultClass());
    				mehtodResponse.setResult(JSON.parseObject(result.toString(), resultCls));
    			}else{
    				mehtodResponse.setResult(result);
    			}
    		}
    		resultQueue.add(mehtodResponse);
    	}
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        //System.out.println("Server IDLE" + session.getIdleCount(status));
    }
    
    public MehtodResponse invokeLocal(MehtodInvoke method){
    	Method targetMethod = findTargetMethod(method);
    	MehtodResponse response = new MehtodResponse();
    	response.setId(method.getId());
    	if (targetMethod != null){
    		Object obj = rpcRegisteres.get(method.getClassName()).getInstance();
    		try{
    			if (method.getParams() != null){
        			Object[] args = method.getParams().toArray();
        			response.setResult(targetMethod.invoke(obj, args));
        		}else{
        			response.setResult(targetMethod.invoke(obj));
        		}
    			response.setResultClass(response.getClass().getName());
    		}catch(Exception ex){
    			ex.printStackTrace();
    			response.setException(ex);
    		}
    	}
    	return response;
    }

	protected Method findTargetMethod(MehtodInvoke method) {
		String className = method.getClassName();
		RpcRegister rpcRe = rpcRegisteres.get(className);
    	int invokeParamsCount = method.getParams()==null?0:method.getParams().size();
    	Method targetMethod = null;
		for (Method refMethod : rpcRe.getMethods()){
    		if (refMethod.getName().equals(method.getMethod()) && refMethod.getParameterCount() == invokeParamsCount){
    			targetMethod = refMethod;
    			for (int x = 0;x < refMethod.getParameterCount();x++) {
    				Class cls = refMethod.getParameterTypes()[x];
    				if (!cls.getName().equals(method.getParams().get(x).getClass().getName())){
    					targetMethod = null;
    					break;
    				}
				}
    			if (targetMethod != null){
    				break;
    			}
    		}
    	}
		return targetMethod;
	}
    
    public synchronized MehtodResponse invokeRemote(MehtodInvoke method,IoSession session){
    	String invokeJson = JSON.toJSONString(method);
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
