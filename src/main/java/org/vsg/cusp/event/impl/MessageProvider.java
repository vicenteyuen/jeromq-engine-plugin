/**
 * 
 */
package org.vsg.cusp.event.impl;

import javax.inject.Provider;

import org.vsg.cusp.event.Message;

/**
 * @author ruanweibiao
 *
 */
public class MessageProvider implements Provider<Message<byte[]>> {

	@Override
	public Message<byte[]> get() {
		// TODO Auto-generated method stub
		
		ByteArrayMessageImpl initMessage = new ByteArrayMessageImpl();
		
		return initMessage;
	}

	
	
}
