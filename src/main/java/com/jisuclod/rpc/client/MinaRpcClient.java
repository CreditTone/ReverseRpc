package com.jisuclod.rpc.client;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.jisuclod.rpc.MethodInvokeHandler;
import com.jisuclod.rpc.MethodProxy;
import com.jisuclod.rpc.RpcRegister;

public class MinaRpcClient {

	private Map<String, RpcRegister> rpcRegisteres = new HashMap<String, RpcRegister>();

	private NioSocketConnector connector;
	
	private ConnectFuture future;
	
	private MethodInvokeHandler handler = new MethodInvokeHandler(rpcRegisteres);

	public void registRpc(Object obj) {
		rpcRegisteres.put(obj.getClass().getName(), new RpcRegister(obj));
	}

	public void connect(InetSocketAddress addr) throws IOException {
		if (connector == null) {
			connector = new NioSocketConnector();
			// 设置连接超时检查时间
			connector.setConnectTimeoutCheckInterval(30);
			connector.setHandler(handler);
			// 建立连接
			future = connector.connect(addr);
			// 等待连接创建完成
			future.awaitUninterruptibly();
		}
	}

	public <T> T getServerProxy(Class<T> protocol) {
		MethodProxy invocationHandler = new MethodProxy(handler);
		Object newProxyInstance = Proxy.newProxyInstance(protocol.getClassLoader(), new Class[] { protocol },
				invocationHandler);
		return (T) newProxyInstance;
	}
}