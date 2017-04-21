import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class MinaTimeClient {
    
    public static void main(String[] args) throws InterruptedException{
        // 创建客户端连接器.
        NioSocketConnector connector = new NioSocketConnector();
        connector.getFilterChain().addLast("logger", new LoggingFilter());
        connector.getFilterChain().addLast("codec", 
                new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
        
        // 设置连接超时检查时间
        connector.setConnectTimeoutCheckInterval(30);
        connector.setHandler(new TimeClientHandler());
        
        // 建立连接
        ConnectFuture cf = connector.connect(new InetSocketAddress("localhost", 6488));
        // 等待连接创建完成
        cf.awaitUninterruptibly();
        System.out.println(cf.getSession().hashCode());
        System.out.println(cf.getSession().hashCode());
        cf.getSession().write("Hi Server!");
        cf.getSession().write("quit");
        cf.getSession().write("世界");
        // 等待连接断开
        cf.getSession().getCloseFuture().awaitUninterruptibly();
        System.out.println("dispose");
        cf.getSession().write("Hi Serveraaaaaaaa!");
        // 释放连接
        connector.dispose();
    }
}
