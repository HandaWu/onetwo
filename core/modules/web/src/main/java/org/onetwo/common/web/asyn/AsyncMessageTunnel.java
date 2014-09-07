package org.onetwo.common.web.asyn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.onetwo.common.log.MyLoggerFactory;
import org.slf4j.Logger;

abstract public class AsyncMessageTunnel<T> implements Serializable {
	
	protected final Logger logger = MyLoggerFactory.getLogger(this.getClass());
	

	abstract protected Collection<T> getMessages();
	
	/***
	 * un-thread-safe
	 * @param message
	 */
	public void addMessage(T message){
		getMessages().add(message);
	}
	
	/***
	 * un-thread-safe
	 * @return
	 */
	public List<T> getAndClearMessages(){
		List<T> messages = new ArrayList<T>(getMessages());
		this.clearMessages();
		return messages;
	}
	
	protected void clearMessages() {
		getMessages().clear();
	}
}
