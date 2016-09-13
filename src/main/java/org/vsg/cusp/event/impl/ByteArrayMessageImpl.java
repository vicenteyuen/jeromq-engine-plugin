package org.vsg.cusp.event.impl;

import org.vsg.cusp.concurrent.AsyncResult;
import org.vsg.cusp.core.Handler;
import org.vsg.cusp.event.DeliveryOptions;
import org.vsg.cusp.event.Message;


public class ByteArrayMessageImpl extends AbstractMessage<byte[]> {

	@Override
	public String replyAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reply(Object message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <R> void reply(Object message,
			Handler<AsyncResult<Message<R>>> replyHandler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reply(Object message, DeliveryOptions options) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <R> void reply(Object message, DeliveryOptions options,
			Handler<AsyncResult<Message<R>>> replyHandler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fail(int failureCode, String message) {
		// TODO Auto-generated method stub
		
	}



}
