package com.jisuclod.rpc.client;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.jisuclod.rpc.MethodInvokeHandler;
import com.jisuclod.rpc.MethodProxy;
import com.jisuclod.rpc.RegistNotify;
import com.jisuclod.rpc.RpcRegister;
import com.jisuclod.rpc.SessionListeer;

public class MinaRpcClient {

	private Map<String, RpcRegister> rpcRegisteres = new HashMap<String, RpcRegister>();

	private NioSocketConnector connector;
	
	private ConnectFuture future;
	
	private MethodInvokeHandler handler = new MethodInvokeHandler(rpcRegisteres);
	
	private InetSocketAddress addr;
	
	public MinaRpcClient(InetSocketAddress addr){
		this.addr = addr;
	}

	public void registRpc(Class cls,Object obj) {
		rpcRegisteres.put(cls.getName(), new RpcRegister(obj));
	}

	public MinaRpcClient connect() throws Exception{
		if (connector == null) {
			try{
				connector = new NioSocketConnector();
				// 设置连接超时检查时间
				connector.setConnectTimeoutCheckInterval(3);
				ObjectSerializationCodecFactory objectSerializationCodecFactory = new ObjectSerializationCodecFactory();  
		        objectSerializationCodecFactory.setDecoderMaxObjectSize(Integer.MAX_VALUE);  
		        objectSerializationCodecFactory.setEncoderMaxObjectSize(Integer.MAX_VALUE);
		        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(objectSerializationCodecFactory));
				connector.setHandler(handler);
				// 建立连接
				future = connector.connect(addr);
				// 等待连接创建完成
				future.awaitUninterruptibly();
				future.getSession();
			}catch(Exception e){
				connector.dispose();
				throw e;
			}
		}
		return this;
	}

	public <T> T getServerProxy(Class<T> protocol) {
		MethodProxy invocationHandler = new MethodProxy(protocol,handler);
		Object newProxyInstance = Proxy.newProxyInstance(protocol.getClassLoader(), new Class[] { protocol },
				invocationHandler);
		return (T) newProxyInstance;
	}
	
	public String getServerAddress(){
		return future.getSession().getRemoteAddress().toString();
	}
	
	public void setListener(SessionListeer listener) {
		handler.setListener(listener);
	}
	
	public void quit(){
		try{
			future.getSession().write("quit");
			future.getSession().getCloseFuture().awaitUninterruptibly();
			connector.dispose();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
