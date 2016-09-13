package org.vsg.cusp.eventbus.impl;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vsg.cusp.concurrent.AsyncResult;
import org.vsg.cusp.core.Handler;
import org.vsg.cusp.event.Message;
import org.vsg.cusp.eventbus.MessageConsumer;
import org.zeromq.ZMQ.Socket;

public class HandlerRegistration<T> implements MessageConsumer<T>,
		Handler<Message<T>> {

	private static Logger log = LoggerFactory
			.getLogger(HandlerRegistration.class);

	private String address;

	private String repliedAddress;

	private Socket clientSocket;

	public static final int DEFAULT_MAX_BUFFERED_MESSAGES = 1000;

	private boolean localOnly;
	private Handler<AsyncResult<Message<T>>> asyncResultHandler;
	private long timeoutID = -1;
	private boolean registered;
	private Handler<Message<T>> handler;
	private AsyncResult<Void> result;
	private Handler<AsyncResult<Void>> completionHandler;
	private Handler<Void> endHandler;
	private Handler<Message<T>> discardHandler;
	private int maxBufferedMessages = DEFAULT_MAX_BUFFERED_MESSAGES;
	private boolean paused;

	private Queue<Message<T>> pending = new ArrayDeque<>(8);

	public HandlerRegistration(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	@Override
	public MessageConsumer<T> exceptionHandler(Handler<Throwable> handler) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public MessageConsumer<T> handler(Handler<Message<T>> handler) {
		this.handler = handler;
		if (this.handler != null && !registered) {
			registered = true;
			// eventBus.addRegistration(address, this, repliedAddress != null,
			// localOnly);
		} else if (this.handler == null && registered) {
			// This will set registered to false
			this.unregister();
		}
		return this;
	}

	@Override
	public MessageConsumer<T> pause() {
		if (!paused) {
			paused = true;
		}
		return this;
	}

	private synchronized void checkNextTick() {
		// Check if there are more pending messages in the queue that can be
		// processed next time around
		if (!pending.isEmpty()) {
			/*
			 * handlerContext.runOnContext(v -> { Message<T> message;
			 * Handler<Message<T>> theHandler; synchronized
			 * (HandlerRegistration.this) { if (paused || (message =
			 * pending.poll()) == null) { return; } theHandler = handler; }
			 * deliver(theHandler, message); });
			 */
		}
	}

	@Override
	public MessageConsumer<T> resume() {
		if (paused) {
			paused = false;
			checkNextTick();
		}
		return this;
	}

	@Override
	public MessageConsumer<T> endHandler(Handler<Void> endHandler) {
		if (endHandler != null) {
			// We should use the HandlerHolder context to properly do this
			// (needs small refactoring)
			// Context endCtx = vertx.getOrCreateContext();
			// this.endHandler = v1 -> endCtx.runOnContext(v2 ->
			// endHandler.handle(null));
		} else {
			this.endHandler = null;
		}
		return this;
	}

	@Override
	public boolean isRegistered() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String address() {
		return address;
	}

	@Override
	public MessageConsumer<T> setMaxBufferedMessages(int maxBufferedMessages) {
		while (pending.size() > maxBufferedMessages) {
			pending.poll();
		}
		this.maxBufferedMessages = maxBufferedMessages;
		return this;
	}

	@Override
	public int getMaxBufferedMessages() {
		// TODO Auto-generated method stub
		return maxBufferedMessages;
	}

	@Override
	public void completionHandler(Handler<AsyncResult<Void>> completionHandler) {
		Objects.requireNonNull(completionHandler);
		if (result != null) {
			AsyncResult<Void> value = result;
			// vertx.runOnContext(v -> completionHandler.handle(value));
		} else {
			this.completionHandler = completionHandler;
		}

	}

	@Override
	public void unregister() {
		// TODO Auto-generated method stub
		unregister(false);
	}

	@Override
	public void unregister(Handler completionHandler) {
		Objects.requireNonNull(completionHandler);
		doUnregister(completionHandler, false);
	}

	public void unregister(boolean callEndHandler) {
		doUnregister(null, callEndHandler);
	}

	private void doUnregister(Handler<AsyncResult<Void>> completionHandler,
			boolean callEndHandler) {
		if (timeoutID != -1) {
		}
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
			// eventBus.removeRegistration(address, this, completionHandler);
		} else {
			callCompletionHandlerAsync(completionHandler);
		}
		registered = false;
	}

	private void callCompletionHandlerAsync(
			Handler<AsyncResult<Void>> completionHandler) {
		if (completionHandler != null) {
			// vertx.runOnContext(v ->
			// completionHandler.handle(Future.succeededFuture()));
		}
	}

	@Override
	public void handle(Message<T> message) {
		Handler<Message<T>> theHandler;
		synchronized (this) {
			if (paused) {
				if (pending.size() < maxBufferedMessages) {
					pending.add(message);
				} else {
					if (discardHandler != null) {
						//discardHandler.handle(message);
					} else {
						log.warn("Discarding message as more than "
								+ maxBufferedMessages
								+ " buffered in paused consumer");
					}
				}
				return;
			} else {
				if (pending.size() > 0) {
					pending.add(message);
					message = pending.poll();
				}
				theHandler = handler;
			}
		}
		deliver(theHandler, message);

	}

	private void deliver(Handler<Message<T>> theHandler, Message<T> message) {
		// Handle the message outside the sync block
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=473714
		checkNextTick();
		boolean local = true;
		/*
		 * if (message instanceof ClusteredMessage) { // A bit hacky
		 * ClusteredMessage cmsg = (ClusteredMessage)message; if
		 * (cmsg.isFromWire()) { local = false; } } String creditsAddress =
		 * message
		 * .headers().get(MessageProducerImpl.CREDIT_ADDRESS_HEADER_NAME); if
		 * (creditsAddress != null) { eventBus.send(creditsAddress, 1); } try {
		 * metrics.beginHandleMessage(metric, local);
		 * theHandler.handle(message); metrics.endHandleMessage(metric, null); }
		 * catch (Exception e) { log.error("Failed to handleMessage", e);
		 * metrics.endHandleMessage(metric, e); throw e; }
		 */
	}

}
