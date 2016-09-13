/**
 * 
 */
package org.vsg.cusp.event.impl;

import java.util.List;

import org.vsg.cusp.event.Message;
import org.vsg.cusp.event.MessageEncoder;
import org.vsg.cusp.eventbus.impl.EventBusOptions;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

/**
 * @deprecated
 * @author Vicente Yuen
 *
 */
public class ZmqcmdHelper {
	
	
	private  MessageEncoder encoder;

	private EventBusOptions options;

	/**
	 * 
	 */
	public ZmqcmdHelper(EventBusOptions options) {
		this.options = options;
		this.options.setCmdHelper(this);
	}

	public MessageEncoder getEncoder() {
		return encoder;
	}

	public void setEncoder(MessageEncoder encoder) {
		this.encoder = encoder;
	}
	
	public void messageSent(Message message ,EventBusOptions options) {
		List<String>  senderHosts =  options.getSenderHosts();
		// Socket to talk to server

		for (String senderHost : senderHosts) {
			Context clientContext = ZMQ.context(zmq.ZMQ.ZMQ_IO_THREADS);			
			
			Socket requester = clientContext.socket(ZMQ.REQ);
			requester.connect(senderHost);			
			if (null != encoder && senderHosts.size() > 0) {
				byte[] content = encoder.encode(message);
				requester.send(content, 0);
				
				// --- reply content ---
				byte[] reply  = requester.recv(0);
				System.out.println(new String(reply));
				//Message replyMsg = encoder.decode( reply );
			}
			
			requester.close();
			clientContext.term();
		}
		


	}

}
