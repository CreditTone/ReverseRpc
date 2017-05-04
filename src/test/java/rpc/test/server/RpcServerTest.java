package rpc.test.server;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.mina.core.session.IoSession;

import com.jisuclod.rpc.server.MinaRpcServer;

import rpc.test.interfaces.Person;

public class RpcServerTest {

	public static void main(String[] args) throws Exception {
		MinaRpcServer s = new MinaRpcServer();
		s.start(6588);
		//模拟等待客户端连接进来,这时启动RpcClient
		IoSession session = null;
		while(session == null){
			session = s.pollIoSession();
			Thread.sleep(1);
		}
		System.out.println("我是服务器端");
		Person p = s.getClientProxy(Person.class, session);//取得代理类
		System.out.println("我拿到了一个实例的代理，她的名字叫"+p.name());
		System.out.println("让"+p.name()+"去吃饭");
		p.eat();
		System.out.println("让"+p.name()+".....");
		p.makelove();
		System.out.println("让"+p.name()+"睡觉");
		p.sleep();
		s.returnIoSession(session);
	}

}
