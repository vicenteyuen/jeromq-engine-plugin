/**
 * 
 */
package org.vsg.cusp.event.impl;

import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.vsg.cusp.concurrent.AsyncResult;
import org.vsg.cusp.concurrent.Callback;
import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.core.Handler;
import org.vsg.cusp.event.DeliveryOptions;
import org.vsg.cusp.event.EventMethodDescription;
import org.vsg.cusp.event.EventMethodRegister;
import org.vsg.cusp.event.EventTrigger;
import org.vsg.cusp.event.Message;
import org.vsg.cusp.event.MessageBus;
import org.vsg.cusp.event.MessageCodec;
import org.vsg.cusp.event.RuntimeParams;
import org.vsg.cusp.eventbus.MultiMap;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

/**
 * @author ruanweibiao
 *
 */
public class EventTriggerImpl implements EventTrigger {
	
	private EventMethodRegister eventMethodRegister;
	
	private MessageBus messageBus;
	
	protected CodecManager codecManager;	
	
	@Inject
	public EventTriggerImpl(
			EventMethodRegister eventMethodRegister , 
			@Named("EventMethodMessageBus") MessageBus messageBus,
			CodecManager codecManager
			) {
		this.eventMethodRegister = eventMethodRegister;
		this.messageBus = messageBus;
		this.codecManager = codecManager;
	}

	/* (non-Javadoc)
	 * @see org.vsg.cusp.event.EventTrigger#fire(java.lang.String, org.vsg.cusp.event.RuntimeParams, org.vsg.cusp.concurrent.Callback)
	 */
	@Override
	public <T> void fire(String eventName, RuntimeParams params,
			Callback<T> callback) throws Exception {
		
		// --- call method for event name ---
		Set<EventMethodDescription>   eventMethodDescSet =  eventMethodRegister.findAllRegisterEventsByName(eventName);

		// --- save state and create new thread to monitor call back handle
		if (eventMethodDescSet.isEmpty()) {
			throw new Exception("Could not find the registerd event method. ");
		}
		
		/**
		 * sent the method content to mq server
		 * 
		 * create event transation id for every message and construct the message wrapper event handle 
		 * 
		 */
		DeliveryOptions deliveryOpts = new DeliveryOptions();
		deliveryOpts.addHeader("basekey", "123");
		deliveryOpts.setCodecName( OperationEventMessageCodec.NAME );
		
		Handler<AsyncResult<Message<T>>> callbackHandler = new Handler<AsyncResult<Message<T>>>() {

			@Override
			public void handle(AsyncResult<Message<T>> event) {
				// TODO Auto-generated method stub
				System.out.println("call back event");
			}
			
		};
		
		for (EventMethodDescription eventMethodDesc : eventMethodDescSet) {
			
			OperationEventImpl eventImpl = new OperationEventImpl();
			eventImpl.setMethodDescription( eventMethodDesc );
			eventImpl.setRuntimeArgument( params.getRuntimeParams() );
			
			// --- convert to operation object 
			AbstractMessage<byte[]> msg = createMessage(false, MessageBus.EVENT_METHOD_CHANNEL, deliveryOpts.getHeaders(), eventImpl,	deliveryOpts.getCodecName());
			
			/**
			 * send message to channel
			 */
			messageBus.send(MessageBus.EVENT_METHOD_CHANNEL, msg, deliveryOpts, callbackHandler);
			
		}

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

}
