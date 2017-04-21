package com.jisuclod.rpc;

import java.lang.reflect.Method;
import java.util.List;

public class RpcRegister {
	
	private String className;
	
	private Object instance;
	
	private List<Method> methods;

	public RpcRegister(Object instance) {
		super();
		this.instance = instance;
	}

	public String getClassName() {
		return className;
	}

	public List<Method> getMethods() {
		return methods;
	}

}
