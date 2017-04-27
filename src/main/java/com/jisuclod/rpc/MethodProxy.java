package com.jisuclod.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.session.IoSession;

public class MethodProxy implements InvocationHandler {
	
	private MethodInvokeHandler methodInvokeHandler;
	
	private Class targetCls;
	
	private IoSession session;
	
	public MethodProxy(Class targetCls,MethodInvokeHandler methodInvokeHandler) {
		this.methodInvokeHandler = methodInvokeHandler;
		this.targetCls = targetCls;
		session = methodInvokeHandler.getSessions().peek();
	}

	public MethodProxy(Class targetCls,MethodInvokeHandler methodInvokeHandler,IoSession session) {
		this.methodInvokeHandler = methodInvokeHandler;
		this.targetCls = targetCls;
		this.session = session;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MehtodInvoke invoke = new MehtodInvoke();
		invoke.setClassName(targetCls.getName());
		invoke.setMethod(method.getName());
		if (args != null && args.length > 0){
			List<Object> params = new ArrayList<Object>();
			for (int i = 0; i < args.length; i++) {
				params.add(args[i]);
			}
			invoke.setParams(params);
		}
		MehtodResponse response = methodInvokeHandler.invokeRemote(invoke,session);
		if (response.getException() != null){
			throw new Exception(response.getException());
		}
		return response.getResult();
	}
}
