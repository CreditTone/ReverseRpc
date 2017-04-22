import java.io.IOException;

import com.jisuclod.rpc.server.MinaRpcServer;

public class RpcServer {

	public static void main(String[] args) throws IOException {
		MinaRpcServer s = new MinaRpcServer();
		s.registRpc(Person.class, new PersonImpl());
		s.start();
	}

}
