package rpc.test.client;

import java.net.InetSocketAddress;

import com.jisuclod.rpc.client.MinaRpcClient;

import rpc.test.interfaces.Person;

public class RpcClientTest {

	public static void main(String[] args) {
		MinaRpcClient client = null;
		try{
			client = new MinaRpcClient(new InetSocketAddress("localhost", 6488)).connect();
			client.registRpc(Person.class, new ClientPersonImpl());//注册实现类
			System.out.println("我是客户端，我注册了一个苍井空的Person实现");
			Thread.sleep(1000);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if (client != null){
				client.quit();
			}
		}
	}

}
