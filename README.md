## 基于Mina实现的一个反向Rpc框架

性能没有测试，但是是基于Mina通信的。官方说Mina自大支持50000个Client。



### 例子



服务端：

```java
import org.apache.mina.core.session.IoSession;

import com.jisuclod.rpc.server.MinaRpcServer;

import rpc.test.interfaces.Person;

public class RpcServerTest {

	public static void main(String[] args) throws Exception {
		MinaRpcServer s = new MinaRpcServer();
		s.start();
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

```



客户端：

```java
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
```



Person.java:

```java
public interface Person {
	
	void makelove();
	
	void eat();
	
	void sleep();
	
	String name();
}
```



ClientPersonImpl.java

```java
import rpc.test.interfaces.Person;

public class ClientPersonImpl implements Person{

	public void makelove() {
		System.out.println("去啪啪啪");
	}

	public void eat() {
		System.out.println("去吃火锅");
	}

	public void sleep() {
		System.out.println("去碎觉");
	}

	public String name() {
		return "苍井空";
	}

}
```



客户端输出：

```
我是客户端，我注册了一个苍井空的Person实现
去吃火锅
去啪啪啪
去碎觉
```

服务器端输出：

```
我是服务器端
我拿到了一个实例的代理，她的名字叫苍井空
让苍井空去吃饭
让苍井空.....
让苍井空睡觉
```



