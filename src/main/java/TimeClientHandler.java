import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class TimeClientHandler extends IoHandlerAdapter {

    public void messageReceived(IoSession session, Object message) throws Exception {
        String content = message.toString();
        System.out.println("client receive a message is : " + content);
    }

    public void messageSent(IoSession session, Object message) throws Exception {
    	System.out.println(message.getClass());
        System.out.println("client messageSent -> ：" + message);
    }
    
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("client IDLE" + session.getIdleCount(status));
    }
    
}