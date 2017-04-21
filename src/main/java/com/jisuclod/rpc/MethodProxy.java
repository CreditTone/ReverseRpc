package com.jisuclod.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSON;

public class MethodProxy implements InvocationHandler {
	
	private MethodInvokeHandler methodInvokeHandler;

	public MethodProxy(MethodInvokeHandler methodInvokeHandler) {
		this.methodInvokeHandler = methodInvokeHandler;
	}



	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MehtodInvoke invoke = new MehtodInvoke();
		invoke.setClassName(proxy.getClass().getName());
		invoke.setMethod(method.getName());
		if (args != null && args.length > 0){
			List<Object> params = new ArrayList<Object>();
			for (int i = 0; i < args.length; i++) {
				params.add(args[i]);
			}
			invoke.setParams(params);
		}
		MehtodResponse response = methodInvokeHandler.invokeRemote(invoke);
		if (response.getException() != null){
			throw response.getException();
		}
		return response.getResult();
	}
}
