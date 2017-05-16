package com.jisuclod.rpc;

import org.apache.mina.core.session.IoSession;

public interface SessionListeer {
	
	void onSessionCreated(IoSession session);
	
	void onSessionDelete(long sessionId);
}
