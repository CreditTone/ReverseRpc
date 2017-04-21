package com.jisuclod.rpc;

import java.util.List;
import java.util.UUID;

public final class MehtodInvoke {
	
	private final String id = UUID.randomUUID().toString();
	
	private String className;
	
	private String method;
	
	private List<Object> params;

	public String getId() {
		return id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public List<Object> getParams() {
		return params;
	}

	public void setParams(List<Object> params) {
		this.params = params;
	}
	
}
