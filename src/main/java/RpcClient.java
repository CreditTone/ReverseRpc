import java.io.IOException;
import java.net.InetSocketAddress;

import com.jisuclod.rpc.client.MinaRpcClient;

public class RpcClient {

	public static void main(String[] args) throws IOException {
		MinaRpcClient c = new MinaRpcClient();
		c.connect(new InetSocketAddress("localhost", 6488));
		Person p = c.getServerProxy(Person.class);
		p.eat();
		p.makelove();
		p.sleep();
		System.out.println(p.name());
	}

}
