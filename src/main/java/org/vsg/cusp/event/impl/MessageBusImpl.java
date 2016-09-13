/**
 * 
 */
package org.vsg.cusp.event.impl;

import javax.inject.Inject;

import org.vsg.cusp.concurrent.AsyncResult;
import org.vsg.cusp.core.Handler;
import org.vsg.cusp.event.DeliveryOptions;
import org.vsg.cusp.event.Message;
import org.vsg.cusp.event.MessageBus;
import org.vsg.cusp.event.MessageEncoder;
import org.vsg.cusp.eventbus.MessageConsumer;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

/**
 * @author Vicente Yuen
 *
 */
public class MessageBusImpl implements MessageBus {
	
	private  MessageEncoder encoder;	
	
	@Inject
	public MessageBusImpl(MessageEncoder encoder) {
		this.encoder = encoder;
	}
	

	/* (non-Javadoc)
	 * @see org.vsg.cusp.event.MessageBus#send(java.lang.String, java.lang.Object, org.vsg.cusp.event.DeliveryOptions, org.vsg.cusp.event.Handler)
	 */
	@Override
	public <T> MessageBus send(String address, Object message,
			DeliveryOptions options,
			Handler<AsyncResult<Message<T>>> replyHandler) {
		
		Context clientContext = ZMQ.context(zmq.ZMQ.ZMQ_IO_THREADS);			
		
		Socket requester = clientContext.socket(ZMQ.REQ);
		requester.connect("tcp://localhost:8701");			
		if (null != encoder) {
			byte[] content = encoder.encode((Message<byte[]>)message);
			requester.send(content, 0);
			
			// --- reply content ---
			byte[] reply  = requester.recv(0);


		}
		
		requester.close();
		clientContext.term();		

		return this;
	}

	@Override
	public <T> MessageConsumer<T> consumer(String address) {
		

		return null;
	}
	
	
	

}
