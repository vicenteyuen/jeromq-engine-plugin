/**
 * 
 */
package org.vsg.cusp.event.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.vsg.cusp.event.MessageConsumerBoxFactory;
import org.vsg.cusp.eventbus.MessageConsumer;

/**
 * @author ruanweibiao
 *
 */
public class MessageQueueBoxBean implements MessageConsumerBoxFactory {
	
	/**
	 * handle map
	 * 
	 */
	private Map<String, MessageConsumer<byte[]>> localMQBoxMap = new LinkedHashMap<String, MessageConsumer<byte[]>>();
	
	

	/* (non-Javadoc)
	 * @see org.vsg.cusp.event.MessageQueueBoxFactory#getBox(java.lang.String)
	 */
	@Override
	public MessageConsumer<byte[]> getConsumer(String address) {
		// TODO Auto-generated method stub
		MessageConsumer<byte[]> msgQueueBox = null;
		if (localMQBoxMap.containsKey(address)) {
			msgQueueBox = localMQBoxMap.get(address);
		} else {
			msgQueueBox = createNewBoxInst();
		}
		
		// --- put the box object to cache ---
		localMQBoxMap.put( address , msgQueueBox);
		
		return msgQueueBox;
	}

	
	
	private MessageConsumer<byte[]> createNewBoxInst() {
		return new MessageQueueBoxImpl();
	}
	

}
