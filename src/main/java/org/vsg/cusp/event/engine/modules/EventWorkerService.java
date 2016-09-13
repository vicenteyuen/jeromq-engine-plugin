package org.vsg.cusp.event.engine.modules;

import javax.inject.Inject;

import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.core.Handler;
import org.vsg.cusp.core.Service;
import org.vsg.cusp.event.Message;
import org.vsg.cusp.event.MessageBus;
import org.vsg.cusp.event.MessageCodec;
import org.vsg.cusp.event.MessageConsumerBoxFactory;
import org.vsg.cusp.event.OperationEvent;
import org.vsg.cusp.event.impl.CodecManager;
import org.vsg.cusp.event.impl.OperationEventMessageCodec;
import org.vsg.cusp.eventbus.MessageConsumer;

import com.google.common.primitives.Ints;

public class EventWorkerService implements Service {

	private MessageConsumerBoxFactory boxFactory;
	
	private CodecManager codecManager;
	
	@Inject
	public EventWorkerService(MessageConsumerBoxFactory boxFactory,
			CodecManager codecManager) {
		this.boxFactory = boxFactory;
		this.codecManager = codecManager;
	}

	@Override
	public void start() throws Exception {
		
		MessageConsumer<byte[]> consumer = boxFactory.getConsumer( MessageBus.EVENT_METHOD_CHANNEL );
		
		// ---- 
		Handler<Message<byte[]>> handler = new Handler<Message<byte[]>>() {

			@Override
			public void handle(Message<byte[]> msg) {
				
				// --- parse content ---
				byte[] msgBody = msg.body();
				
				int locFrom = 0;
				int locTo = locFrom + Ints.BYTES;
				int msgBodyLength = Ints.fromByteArray( java.util.Arrays.copyOfRange(msgBody, locFrom, locTo) );

				locFrom = locTo;
				locTo = locFrom + msgBodyLength;		
				
				String  msgCodecName = new String(java.util.Arrays.copyOfRange(msgBody, locFrom, locTo) );
				
				if ("null".equals(msgCodecName)) {
					
				}
				else if ("operation-event".equals(OperationEventMessageCodec.NAME))  {
					
					MessageCodec<?,OperationEvent>  msgCodec =  codecManager.getCodec(msgCodecName);
					
					int maxLength = msgBody.length - locTo;
					Buffer buffer = Buffer.buffer( maxLength );
					buffer.appendBytes( java.util.Arrays.copyOfRange(msgBody, locTo, msgBody.length)  );
					
					OperationEvent event = msgCodec.decodeFromWire( 0 , buffer);
					
					System.out.println("return event : " + event);

					
					//scheduleAndExecuteEvent(event);
					// --- arrange and execute object ---
					
					//Message<byte[]> respMsg = createResponseMsg(msg);

					
				}				
				
				
			}
			
		};
		consumer.handler(handler);
		
		Handler<Void> voidhandler = new Handler<Void>() {

			@Override
			public void handle(Void event) {
				// TODO Auto-generated method stub
				
			}


			
		};		
		consumer.endHandler(voidhandler);
		


	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}
