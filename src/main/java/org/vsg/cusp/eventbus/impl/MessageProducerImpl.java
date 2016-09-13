package org.vsg.cusp.eventbus.impl;

import java.util.ArrayDeque;
import java.util.Queue;

import org.vsg.cusp.concurrent.AsyncResult;
import org.vsg.cusp.core.Handler;
import org.vsg.cusp.event.DeliveryOptions;
import org.vsg.cusp.event.Message;
import org.vsg.cusp.eventbus.EventBus;
import org.vsg.cusp.eventbus.MessageConsumer;
import org.vsg.cusp.eventbus.MessageProducer;

public class MessageProducerImpl<T> implements MessageProducer<T> {

	private EventBus bus;
	private boolean send;
	private String address;

	private DeliveryOptions options;

	private Queue<T> pending = new ArrayDeque<>();

	private int maxSize = DEFAULT_WRITE_QUEUE_MAX_SIZE;
	private int credits = DEFAULT_WRITE_QUEUE_MAX_SIZE;

	private MessageConsumer<Integer> creditConsumer;

	private Handler<Void> drainHandler;
	
	public MessageProducerImpl() {
		
	}
	
	public MessageProducerImpl(EventBus eventBus , String address , boolean send , DeliveryOptions options) {
		this.address = address;
		this.send = send;
		this.options = options;
		this.bus = eventBus;
	}
	

	@Override
	public MessageProducer<T> send(T message) {
		doSend(message, null);
		return this;
	}

	@Override
	public <R> MessageProducer<T> send(T message,
			Handler<AsyncResult<Message<R>>> replyHandler) {
		doSend(message, replyHandler);
		return this;
	}

	@Override
	public MessageProducer<T> exceptionHandler(Handler<Throwable> handler) {
		return this;
	}

	@Override
	public MessageProducer<T> write(T data) {
		if (send) {
			doSend(data, null);
		} else {
			bus.publish(address, data, options);
		}
		return this;
	}

	@Override
	public MessageProducer<T> setWriteQueueMaxSize(int s) {
		int delta = s - maxSize;
		maxSize = s;
		credits += delta;
		return this;
	}

	private synchronized <R> void doSend(T data,
			Handler<AsyncResult<Message<R>>> replyHandler) {
		if (credits > 0) {
			credits--;
			if (replyHandler == null) {
				bus.send(address, data, options);
			} else {
				bus.send(address, data, options, replyHandler);
			}
		} else {
			pending.add(data);
		}
	}

	@Override
	public MessageProducer<T> drainHandler(Handler<Void> handler) {
		this.drainHandler = handler;
		return this;
	}

	@Override
	public MessageProducer<T> deliveryOptions(DeliveryOptions options) {
		this.options = options;
		return this;
	}

	@Override
	public String address() {
		// TODO Auto-generated method stub
		return address;
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub
		close();
	}

	@Override
	public void close() {
		if (creditConsumer != null) {
			creditConsumer.unregister();
		}

	}

}
