import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

/**
 * 服务器端业务逻辑
 */
public class TimeServerHandler extends IoHandlerAdapter {

    /**
     * 连接创建事件
     */
    @Override
    public void sessionCreated(IoSession session){
        // 显示客户端的ip和端口
        System.out.println("server sessionCreated "+session.getRemoteAddress().toString());
    }
    
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
    
    /**
     * 消息接收事件
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String strMsg = message.toString();
        System.out.println("server receive a message is : " + strMsg);
        if (strMsg.trim().equalsIgnoreCase("quit")) {
            session.close(true);
            return;
        }
        // 返回消息字符串
        session.write("Hi Client!");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("Server IDLE" + session.getIdleCount(status));
    }
    
    public void messageSent(IoSession session, Object message) throws Exception {
        System.out.println("Server messageSent -> ：" + message);
    }
    
}