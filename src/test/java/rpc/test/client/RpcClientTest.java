package rpc.test.client;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.jisuclod.rpc.client.MinaRpcClient;

import rpc.test.interfaces.Person;

public class RpcClientTest {

	public static void main(String[] args) throws IOException {
		MinaRpcClient c = new MinaRpcClient(new InetSocketAddress("localhost", 6488)).connect();
		c.registRpc(Person.class, new ClientPersonImpl());//注册实现类
		System.out.println("我是客户端，我注册了一个苍井空的Person实现");
	}

}
