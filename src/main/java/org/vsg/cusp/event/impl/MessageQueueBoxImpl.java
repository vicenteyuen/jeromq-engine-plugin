/**
 * 
 */
package org.vsg.cusp.event.impl;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vsg.cusp.concurrent.AsyncResult;
import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.core.Handler;
import org.vsg.cusp.event.Message;
import org.vsg.cusp.event.MessageCodec;
import org.vsg.cusp.event.MessageQueueBox;
import org.vsg.cusp.event.OperationEvent;
import org.vsg.cusp.eventbus.MessageConsumer;

import com.google.common.primitives.Ints;

/**
 * @author ruanweibiao
 *
 */
public class MessageQueueBoxImpl implements MessageQueueBox<byte[]> ,  MessageConsumer<byte[]> {

	

	private static Logger logger = LoggerFactory.getLogger(MessageQueueBoxImpl.class);
	
	private ConcurrentLinkedQueue<Message<byte[]>> msgQueue = new ConcurrentLinkedQueue<Message<byte[]>>();

	/* (non-Javadoc)
	 * @see org.vsg.cusp.event.MessageInbox#receiveMsg(org.vsg.cusp.event.Message)
	 */
	@Override
	public synchronized void handle(Message<byte[]> msg) {
	
		if (!paused) {
			return;
		}
		
		/**
		 * check the queue count
		 */
		if (msgQueue.size() > this.maxBufferedMessages) {
			if (logger.isWarnEnabled()) {
				logger.warn("Buffered message is up to limited. New message is discarded.");
			}
			return ;
		}
		
		
		msgQueue.offer( msg );


		// fire new message continer
		Message<byte[]>  tmpMsg = msgQueue.poll();
		
		if ( null ==  tmpMsg ) {
			return;
		}
		
		// --- call handle message --- 
		/*
		try {
			this.handler.handle( msg );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if (null != exceptionHandler) {
				exceptionHandler.handle( e ); 				
			}
		} finally {
			if (null != this.endHandler) {
				endHandler.handle(null);
			}
		}
		*/

	}

	
	
	private Handler<Throwable> exceptionHandler;


	@Override
	public MessageConsumer<byte[]> exceptionHandler(Handler<Throwable> handler) {
		this.exceptionHandler = handler;
		return this;
	}
	
	private Handler<Message<byte[]>> handler;


	@Override
	public MessageConsumer<byte[]> handler(Handler<Message<byte[]>> handler) {
		this.handler = handler;
		
		if (this.handler != null && !registered) {
			registered = true;
		} else if ( this.handler == null && registered ){
			this.unregister();
		}
		return this;
	}
	
	private volatile boolean  paused;

	@Override
	public MessageConsumer<byte[]> pause() {
		this.paused = true;
		return this;
	}



	@Override
	public MessageConsumer<byte[]> resume() {
		paused = false;
		return this;
	}
	
	private Handler<Void> endHandler;

	@Override
	public MessageConsumer<byte[]> endHandler(Handler<Void> endHandler) {
		
		if (null != endHandler) {
			
			this.endHandler = endHandler;			
		} else {
			
		}
		

		return this;
	}
	
	private boolean registered;


	@Override
	public boolean isRegistered() {
		// TODO Auto-generated method stub
		return registered;
	}
	
	private String address;


	@Override
	public String address() {
		return address;
	}
	
	private int maxBufferedMessages = 10;


	@Override
	public MessageConsumer<byte[]> setMaxBufferedMessages(
			int maxBufferedMessages) {
		this.maxBufferedMessages = maxBufferedMessages;
		return this;
	}



	@Override
	public int getMaxBufferedMessages() {
		return maxBufferedMessages;
	}
	
	
	private Handler<AsyncResult<Void>> completionHandler;

	@Override
	public void completionHandler(Handler<AsyncResult<Void>> completionHandler) {
		this.completionHandler = completionHandler;		
	}



	@Override
	public void unregister() {
		doUnregister(null ,false);
	}
	@Override
	public void unregister(Handler<AsyncResult<Void>> completionHandler) {
		Objects.requireNonNull(completionHandler);
		doUnregister(completionHandler , false);
	}

	private void doUnregister(Handler<AsyncResult<Void>> completionHandler, boolean callEndHandler) {
		if (endHandler != null && callEndHandler) {
			Handler<Void> theEndHandler = endHandler;
			Handler<AsyncResult<Void>> handler = completionHandler;
			completionHandler = ar -> {
				theEndHandler.handle(null);
				if (handler != null) {
					handler.handle(ar);
				}
			};
		}
		
		if (registered) {
			registered = false;
		} else {

		}
		registered = false;
	}
	
	
}
