package org.vsg.cusp.event.flow.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vsg.cusp.concurrent.AsyncResult;
import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.core.Handler;
import org.vsg.cusp.core.Service;
import org.vsg.cusp.event.DeliveryOptions;
import org.vsg.cusp.event.Message;
import org.vsg.cusp.event.MessageCodec;
import org.vsg.cusp.event.impl.AbstractMessage;
import org.vsg.cusp.event.impl.ByteArrayMessageImpl;
import org.vsg.cusp.event.impl.CodecManager;
import org.vsg.cusp.event.impl.MessageImpl;
import org.vsg.cusp.event.impl.ZmqcmdHelper;
import org.vsg.cusp.eventbus.EventBus;
import org.vsg.cusp.eventbus.MessageConsumer;
import org.vsg.cusp.eventbus.MessageProducer;
import org.vsg.cusp.eventbus.MultiMap;
import org.vsg.cusp.eventbus.SendContext;
import org.vsg.cusp.eventbus.impl.EventBusOptions;
import org.vsg.cusp.eventbus.impl.HandlerHolder;
import org.vsg.cusp.eventbus.impl.HandlerRegistration;
import org.vsg.cusp.eventbus.impl.Handlers;
import org.vsg.cusp.eventbus.impl.MessageProducerImpl;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;


/**
 * @deprecated
 * @author ruanweibiao
 *
 */
public class ZmqEventBusImplEndPoint implements EventBus , Service{

	private static Logger logger = LoggerFactory.getLogger(ZmqEventBusImplEndPoint.class);

	protected volatile boolean started;

	protected ConcurrentMap<String, Handlers> handlerMap = new ConcurrentHashMap<>();

	protected CodecManager codecManager;

	private EventBusOptions options;

	@Inject
	public ZmqEventBusImplEndPoint(EventBusOptions options) {
		this.options = options;

		init();
	}
	
	@Inject
	public void setCodecManager(CodecManager codeManager) {
		this.codecManager = codeManager;
	}
	
	
	private ZmqcmdHelper cmdHelper;


	private void init() {
		// --- define helper ---
		cmdHelper = options.getCmdHelper();
	}

	@Override
	public EventBus send(String address, Object message) {
		return send(address, message, new DeliveryOptions(), null);
	}

	@Override
	public <T> EventBus send(String address, Object message,
			Handler<AsyncResult<Message<T>>> replyHandler) {
		return send(address, message, new DeliveryOptions(), replyHandler);
	}

	@Override
	public EventBus send(String address, Object message, DeliveryOptions options) {
		return send(address, message, options, null);
	}

	@Override
	public <T> EventBus send(String address, Object message,
			DeliveryOptions options,
			Handler<AsyncResult<Message<T>>> replyHandler) {
		
		// --- bind headers handle ---
		
		
		AbstractMessage<T> msg = createMessage(true, address, options.getHeaders(), message,	options.getCodecName());
		sendOrPubInternal(msg, options, replyHandler);
		return this;
	}

	@Override
	public EventBus publish(String address, Object message) {

		return publish(address, message, new DeliveryOptions());
	}

	@Override
	public EventBus publish(String address, Object message,
			DeliveryOptions options) {
		AbstractMessage<byte[]> msg = createMessage(false, address, options.getHeaders(), message,
				options.getCodecName());
		
		
		sendOrPubInternal(msg, options, null);
		return this;
	}

	public <T> AbstractMessage<T> createMessage(boolean send, String address,
			MultiMap headers, Object body, String codecName) {
		Objects.requireNonNull(address, "no null address accepted");
		
		// --- create byte message ---
		ByteArrayMessageImpl msg = new ByteArrayMessageImpl();
		msg.setHeaders( headers );
		msg.setAddress(address);
		
		byte[] mainBody = null;
		if (null != codecName) {
			MessageCodec<Object,byte[]> msgCodec = codecManager.getCodec(codecName);
			
			Buffer buffer = Buffer.factory.buffer();
			msgCodec.encodeToWire( buffer , body);
			mainBody = buffer.getBytes();
			
			// --- remove byte ---
			int cIndex = 0;
			for (int tmpEmpty = mainBody.length-1 ; tmpEmpty > 0 ; tmpEmpty --) {
				if (mainBody[tmpEmpty] != 0 ) {
					break;
				}
				cIndex++;
			}
			
			mainBody = java.util.Arrays.copyOfRange(mainBody, 0, mainBody.length - cIndex);
		} else {
			MessageCodec codec = codecManager.lookupCodec(body, codecName);
			
			codecName = codec.name();			
			codec.transform( body );

		}
		Objects.requireNonNull(codecName, "code Name is not define.");		
		Objects.requireNonNull(mainBody, "Could not find the body content.");
		
		
		byte[] cnOffset = Ints.toByteArray( codecName.length() );
		
		msg.setBody( Bytes.concat( cnOffset , codecName.getBytes() , mainBody ) );
		
		
		return (AbstractMessage<T>)msg;
	}

	private <T> void sendOrPubInternal(AbstractMessage<T> message,
			DeliveryOptions options,
			Handler<AsyncResult<Message<T>>> replyHandler) {
		checkStarted();
		HandlerRegistration<T> replyHandlerRegistration = createReplyHandlerRegistration(message, options, replyHandler);

		SendContextImpl<T> sendContext = new SendContextImpl<>(message,	options, replyHandlerRegistration);
		sendContext.next();

	}

	private final AtomicLong replySequence = new AtomicLong(0);

	protected String generateReplyAddress() {
		return Long.toString(replySequence.incrementAndGet());
	}

	protected <T> Handler<Message<T>> convertHandler(
			Handler<AsyncResult<Message<T>>> handler) {
		return reply -> {
			Future<Message<T>> result;
			/*
			 * if (reply.body() instanceof ReplyException) { // This is kind of
			 * clunky - but hey-ho ReplyException exception = (ReplyException)
			 * reply.body(); metrics.replyFailure(reply.address(),
			 * exception.failureType()); result =
			 * Future.failedFuture(exception); } else { result =
			 * Future.succeededFuture(reply); }
			 */
			// handler.handle(result);
		};
	}

	private <T> HandlerRegistration<T> createReplyHandlerRegistration(
			AbstractMessage<T> message, DeliveryOptions options,
			Handler<AsyncResult<Message<T>>> replyHandler) {
		if (replyHandler != null) {
			long timeout = options.getSendTimeout();
			String replyAddress = generateReplyAddress();
			//message.setReplyAddress(replyAddress);
			Handler<Message<T>> simpleReplyHandler = convertHandler(replyHandler);

			Context context = ZMQ.context(1);
		
			Socket requester = context.socket(ZMQ.REQ);
			requester.connect("tcp://localhost:5559");
			


			HandlerRegistration<T> registration = new HandlerRegistration<>(
					requester);
			registration.handler(simpleReplyHandler);
			return registration;
		} else {
			return null;
		}

	}

	@Override
	public <T> MessageConsumer<T> consumer(String address) {
		// --- create consumer ---
		Context context = ZMQ.context(1);
		Socket requester = context.socket(ZMQ.REQ);
		requester.connect("tcp://localhost:5559");

		Objects.requireNonNull(address, "address");
		HandlerRegistration mci = new HandlerRegistration(requester);
		return mci;
	}

	@Override
	public <T> MessageConsumer<T> consumer(String address,
			Handler<Message<T>> handler) {
		Objects.requireNonNull(handler, "handler");
		MessageConsumer<T> consumer = consumer(address);
		return consumer;
	}


	@Override
	public <T> MessageProducer<T> sender(String address) {
		Objects.requireNonNull(address, "address");
		MessageProducerImpl msgProdImpl = new MessageProducerImpl<>(this, address, true,new DeliveryOptions());
		return msgProdImpl;
	}

	@Override
	public <T> MessageProducer<T> sender(String address, DeliveryOptions options) {
		Objects.requireNonNull(address, "address");
		Objects.requireNonNull(options, "options");
		
		// --- create byte producer handle ---
		MessageProducerImpl msgProdImpl = new MessageProducerImpl<>(this,address, true,	options);
		return msgProdImpl;
	}

	@Override
	public <T> MessageProducer<T> publisher(String address) {
		Objects.requireNonNull(address, "address");
		MessageProducerImpl msgProdImpl = new MessageProducerImpl<>(this, address, true,
				new DeliveryOptions());
		return msgProdImpl;
	}

	@Override
	public <T> MessageProducer<T> publisher(String address,
			DeliveryOptions options) {
		Objects.requireNonNull(address, "address");
		Objects.requireNonNull(options, "options");
		return new MessageProducerImpl<>(this,address, false, options);
	}

	@Override
	public EventBus registerCodec(MessageCodec codec) {
		codecManager.registerCodec(codec);
		return this;
	}

	@Override
	public EventBus unregisterCodec(String name) {
		codecManager.unregisterCodec(name);
		return this;
	}

	@Override
	public <T> EventBus registerDefaultCodec(Class<T> clazz,
			MessageCodec<T, ?> codec) {
		codecManager.registerDefaultCodec(clazz, codec);
		return this;
	}

	@Override
	public EventBus unregisterDefaultCodec(Class clazz) {
		codecManager.unregisterDefaultCodec(clazz);
		return this;
	}

	protected void checkStarted() {
		if (!started) {
			throw new IllegalStateException("Event Bus is not started");
		}
	}

	@Override
	public void start(Handler<AsyncResult<Void>> completionHandler) {
		if (started) {
			throw new IllegalStateException("Already started");
		}

		started = true;
		// completionHandler.handle(Future.succeededFuture());
	}

	@Override
	public void close(Handler<AsyncResult<Void>> completionHandler) {
		checkStarted();
		unregisterAll();

		if (completionHandler != null) {
			// vertx.runOnContext(v ->
			// completionHandler.handle(Future.succeededFuture()));
		}

	}

	private void unregisterAll() {
		// Unregister all handlers explicitly - don't rely on context hooks
		for (Handlers handlers : handlerMap.values()) {
			for (HandlerHolder holder : handlers.list) {
				holder.getHandler().unregister(true);
			}
		}
	}

	private List<Handler<SendContext>> interceptors = new CopyOnWriteArrayList<>();

	/**
	 * Define base inner context
	 * 
	 * @author Vicente Yuen
	 *
	 * @param <T>
	 */
	protected class SendContextImpl<T> implements SendContext<T> {

		public AbstractMessage<T> message;
		public DeliveryOptions options;
		public HandlerRegistration<T> handlerRegistration;
		public Iterator<Handler<SendContext>> iter;

		public SendContextImpl(AbstractMessage<T> message, DeliveryOptions options,
				HandlerRegistration<T> handlerRegistration) {
			this.message = message;
			this.options = options;
			this.handlerRegistration = handlerRegistration;
			this.iter = interceptors.iterator();
		}

		@Override
		public Message<T> message() {
			return message;
		}

		@Override
		public void next() {

			if (iter.hasNext()) {
				Handler<SendContext> handler = iter.next();
				try {
					handler.handle(this);
				} catch (Throwable t) {
					logger.error("Failure in interceptor", t);
				}
			} else {
				sendOrPub(this);
			}
		}

		@Override
		public boolean send() {
			//return message.send();
			return false;
		}
	}

	protected <T> void sendOrPub(SendContextImpl<T> sendContext) {
		AbstractMessage<T> message = sendContext.message;
		
		// --- message send ---
		cmdHelper.messageSent(message, options);
		// metrics.messageSent(message.address(), !message.send(), true, false);
	}


	public <T> void sendReply(AbstractMessage<T> replyMessage,
			AbstractMessage<T> replierMessage, DeliveryOptions options,
			Handler<AsyncResult<Message<T>>> replyHandler) {
		if (replyMessage.address() == null) {
			throw new IllegalStateException("address not specified");
		} else {
			HandlerRegistration<T> replyHandlerRegistration = createReplyHandlerRegistration(replyMessage, options, replyHandler);
			
			//new ReplySendContextImpl<>(replyMessage, options,replyHandlerRegistration, replierMessage).next();
		}
	}

	protected <T> boolean deliverMessageLocally(MessageImpl msg) {
		//msg.setBus(this);
		Handlers handlers = handlerMap.get(msg.address());

		if (handlers != null) {
			if (msg.send()) {
				// Choose one
				HandlerHolder holder = handlers.choose();
				/*
				 * if (holder != null) { metrics.messageReceived(msg.address(),
				 * !msg.send(), isMessageLocal(msg), 1); deliverToHandler(msg,
				 * holder); } } else { // Publish
				 * metrics.messageReceived(msg.address(), !msg.send(),
				 * isMessageLocal(msg), handlers.list.size()); for
				 * (HandlerHolder holder: handlers.list) { deliverToHandler(msg,
				 * holder); } }
				 */
				return true;
			} else {
				// metrics.messageReceived(msg.address(), !msg.send(),
				// isMessageLocal(msg), 0);
				return false;
			}
		}
		return false;
	}
	
	private Lock lock = new ReentrantLock();

	@Override
	public void start() throws Exception {
		
		
		if (lock.tryLock()) {
			
			try {
				lock.lock();
				
				Handler<AsyncResult<Void>> completionHandler = new Handler<AsyncResult<Void>>() {

					@Override
					public void handle(AsyncResult<Void> event) {
						// TODO Auto-generated method stub
						System.out.println(event);
					}
					
				};
				this.start(completionHandler);	
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
			
		} else {
			// --- lock fail ---
			System.out.println("get lock fail.");
			System.exit( -1);
		}
		

		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}


}
