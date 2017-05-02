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
	
	private LinkedBlockingQueue<IoSession>  sessions = new LinkedBlockingQueue<IoSession>();
	
	private LinkedBlockingQueue<MehtodResponse> resultQueue = new LinkedBlockingQueue<MehtodResponse>();
	
	private Map<String,List<String>> stringPart = new ConcurrentHashMap<String,List<String>>();

	public void setStringPrat(String id, String part) {
		List<String> partList = stringPart.get(id);
		if (partList == null){
			partList = new ArrayList<String>();
			stringPart.put(id, partList);
		}
		partList.add(part);
	}

	public String getPratString(String id) {
		List<String> partList = stringPart.remove(id);
		if (partList != null){
			StringBuilder builder = new StringBuilder();
			for (String part : partList) {
				builder.append(part);
			}
			return builder.toString();
		}
		return null;
	}
	
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
        session.closeNow();
    }
    
    /**
     * 消息接收事件
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
    	if (message.equals("quit")){
    		session.closeNow();
    		return;
    	}
    	if (message.toString().startsWith("part ")){
    		String id = message.toString().substring(5, 41);
    		String partBody = message.toString().substring(42, message.toString().length());
    		setStringPrat(id, partBody);
    	}else if (message.toString().contains("method")){
    		MehtodInvoke mehtodInvoke =  JSON.parseObject(message.toString(), MehtodInvoke.class);
        	MehtodResponse response  = invokeLocal(mehtodInvoke);
        	if (String.class.getName().equals(response.getResultClass())){
        		String strRet = (String)response.getResult();
        		if (strRet.length() >= 512){
        			response.setResult("");
        			List<String> partlist = Util.getPartList(strRet);
        			for (String part : partlist) {
        				String partWrite = "part "+ response.getId() + " " + part;
        				session.write(partWrite);
					}
        		}
        	}
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
    				String partBody = getPratString(mehtodResponse.getId());
    				if (partBody != null) {
    					mehtodResponse.setResult(partBody);
    				}
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
    			if (response.getResult() != null){
    				response.setResultClass(response.getResult().getClass().getName());
    			}
    		}catch(Exception ex){
    			ex.printStackTrace();
    			response.setException(ex.getCause().toString());
    		}
    	}else{
    		System.err.print("Not found method by name " + method.getMethod());
    	}
    	return response;
    }

	protected Method findTargetMethod(MehtodInvoke method) {
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
