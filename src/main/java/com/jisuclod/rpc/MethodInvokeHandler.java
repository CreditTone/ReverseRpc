package com.jisuclod.rpc;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class MethodInvokeHandler extends IoHandlerAdapter {
	
	private Map<String,RpcRegister> rpcRegisteres = new HashMap<String, RpcRegister>();
	
	private Map<String,FileUploadRequest> files = new ConcurrentHashMap<String, FileUploadRequest>();
	
	private Map<Long,IoSession>  sessions = new HashMap<Long,IoSession>();
	
	private LinkedBlockingQueue<MehtodResponse> resultQueue = new LinkedBlockingQueue<MehtodResponse>();
	
	private SessionListeer  listener;
	
	public MethodInvokeHandler(Map<String,RpcRegister> rpcRegisteres){
		this.rpcRegisteres = rpcRegisteres;
	}
	
	public SessionListeer getListener() {
		return listener;
	}

	public void setListener(SessionListeer listener) {
		this.listener = listener;
	}

	public Map<Long, IoSession> getSessions() {
		return sessions;
	}

	/**
     * 连接创建事件
     */
    @Override
    public void sessionCreated(final IoSession session){
    	System.out.println("sessionCreated "+session.getId());
    	sessions.put(session.getId(),session);
    	if (listener != null){
			new Thread(){
				public void run() {
					listener.onSessionCreated(session);
				};
			}.start();
    	}
    }
    
    
    @Override
	public synchronized void sessionClosed(IoSession session) throws Exception {
    	System.out.println("sessionClosed "+session.getId());
    	sessions.remove(session.getId());
    	if (listener != null){
    		listener.onSessionDelete(session.getId());
    	}
	}

	@Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
        session.closeNow();
    }
    
    /**
     * 消息接收事件
     */
    @Override
    public void messageReceived(final IoSession session, Object message) throws Exception {
    	if (message instanceof RegistNotify){
    		final RegistNotify notify = (RegistNotify) message;
    		return;
    	}
    	if (message instanceof FileUploadRequest){
    		FileUploadRequest fur = (FileUploadRequest) message;
    		files.put(fur.getId(), fur);
    		return;
    	}
    	if (message.equals("quit")){
    		session.closeNow();
    		return;
    	}
    	if (message.toString().contains("method")){
    		MehtodInvoke mehtodInvoke =  JSON.parseObject(message.toString(), MehtodInvoke.class);
    		MehtodResponse response = null;
    		try{
    			response  = invokeLocal(mehtodInvoke);
            	if ("[B".equals(response.getResultClass())){
            		FileUploadRequest fur = new FileUploadRequest();
            		fur.setBytes((byte[]) response.getResult());
            		fur.setId(response.getId());
            		session.write(fur);
            		response.setResult("");
            	}
    		}catch(Exception e){
    			e.printStackTrace();
    			response = new MehtodResponse();
    			response.setId(mehtodInvoke.getId());
    			response.setException(e.getCause().toString());
    		}
    		String retStr = JSON.toJSONString(response);
        	session.write(retStr);
    	}else{
    		JSONObject resultJson = null;
    		try{
    			resultJson = JSON.parseObject(message.toString());
    			Object result = resultJson.remove("result");
        		MehtodResponse mehtodResponse = JSON.parseObject(resultJson.toString(), MehtodResponse.class);
        		if (result != null){
        			if (result instanceof JSON){
        				Class resultCls = Class.forName(mehtodResponse.getResultClass());
        				mehtodResponse.setResult(JSON.parseObject(result.toString(), resultCls));
        			}else if (mehtodResponse.getResultClass().equals("[B")){
        				byte[] body = files.get(mehtodResponse.getId()).getBytes();
        				mehtodResponse.setResult(body);
        			}else{
        				mehtodResponse.setResult(result);
        			}
        		}
        		resultQueue.add(mehtodResponse);
    		}catch(Exception e){
    			e.printStackTrace();
    			System.out.println(message);
    		}
    	}
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        //System.out.println("Server IDLE" + session.getIdleCount(status));
    }
    
    public MehtodResponse invokeLocal(MehtodInvoke method) throws Exception{
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
    			response.setResultClass(targetMethod.getReturnType().getName());
    		}catch(Exception ex){
    			ex.printStackTrace();
    			response.setException(ex.getCause().toString());
    		}
    	}else{
    		System.err.print("Not found method by name " + method.getMethod());
    	}
    	return response;
    }

	protected Method findTargetMethod(MehtodInvoke method) throws Exception {
		String className = method.getClassName();
		RpcRegister rpcRe = rpcRegisteres.get(className);
    	int invokeParamsCount = method.getParams()==null?0:method.getParams().size();
    	Method targetMethod = null;
		for (Method refMethod : rpcRe.getMethods()){
    		if (refMethod.getName().equals(method.getMethod()) && refMethod.getParameterTypes().length == invokeParamsCount){
    			targetMethod = refMethod;
    			for (int x = 0;x < refMethod.getParameterTypes().length;x++) {
    				Class cls = getJavaBasicTypeClass(refMethod.getParameterTypes()[x]);
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
			long startTime = System.currentTimeMillis();
			while(true){
				response = resultQueue.poll();
				if (response != null){
					if (response.getId().equals(method.getId())){
						return response;
					}else{
						resultQueue.add(response);
						Thread.sleep(1);
					}
				}else{
					if ((startTime - System.currentTimeMillis()) > 1000 * 60){
						throw new Exception("invoke method "+method.getMethod()+" timeout");
					}
				}
				Thread.sleep(5);
			}
		}catch(Exception ex){
			//ex.printStackTrace();
			response = new MehtodResponse();
			response.setId(method.getId());
			response.setException(ex.getCause().toString());
		}
    	return response;
    }
    
    
    private static Class getJavaBasicTypeClass(Class cls){
    	switch(cls.getName()){
    	case "int":
    		return Integer.class;
    	case "short":
    		return Short.class;
    	case "long":
    		return Long.class;
    	case "byte":
    		return Byte.class;
    	case "double":
    		return Double.class;
    	case "boolean":
    		return Boolean.class;
    	default:
    		return cls;
    	}
    }
}
