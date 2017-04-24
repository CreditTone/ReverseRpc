package com.jisuclod.rpc;

public class MehtodResponse {
	
	private String id;
	
	private Object result;
	
	private String resultClass;
	
	private Exception exception;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public String getResultClass() {
		return resultClass;
	}

	public void setResultClass(String resultClass) {
		this.resultClass = resultClass;
	}
	
}
