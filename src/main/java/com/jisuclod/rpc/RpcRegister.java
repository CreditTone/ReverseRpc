package com.jisuclod.rpc;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class RpcRegister {
	
	private String className;
	
	private Object instance;
	
	private List<Method> methods;

	public RpcRegister(Object instance) {
		super();
		this.instance = instance;
		className = instance.getClass().getName();
		methods = Arrays.asList(instance.getClass().getDeclaredMethods());
	}

	public String getClassName() {
		return className;
	}

	public List<Method> getMethods() {
		return methods;
	}

	public Object getInstance() {
		return instance;
	}

	public void setInstance(Object instance) {
		this.instance = instance;
	}

}
