package com.jisuclod.rpc.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.jisuclod.rpc.MethodInvokeHandler;
import com.jisuclod.rpc.RpcRegister;

public class MinaRpcServer {
	
	private Map<String,RpcRegister> rpcRegisteres = new HashMap<String, RpcRegister>();
	
	private IoAcceptor acceptor = null;
	
	public void registRpc(Object obj){
		rpcRegisteres.put(obj.getClass().getName(), new RpcRegister(obj));
	}
	
	public void start() throws IOException{
		if (acceptor == null){
			 // 创建服务端监控线程
	        acceptor = new NioSocketAcceptor();
	        acceptor.getSessionConfig().setReadBufferSize(2048);
	        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
	        // 指定业务逻辑处理器
	        acceptor.setHandler(new MethodInvokeHandler(rpcRegisteres));
	        // 设置端口号
	        acceptor.bind(new InetSocketAddress(6488));
	        // 启动监听线程
	        acceptor.bind();
		}
		
	}
	
	public <T> T getClientProxy(Class<T> protocol){
		return null;
	}
}
