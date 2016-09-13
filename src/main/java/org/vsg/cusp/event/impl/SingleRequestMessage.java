package org.vsg.cusp.event.impl;

import java.io.UnsupportedEncodingException;

import org.vsg.cusp.core.utils.CorrelationIdGenerator;
import org.vsg.cusp.event.Message;
import org.vsg.cusp.event.MessageCodec;
import org.vsg.cusp.event.MessageCodecSupport;
import org.vsg.cusp.event.RequestMessageDecoder;
import org.vsg.cusp.event.RequestMessageEncoder;
import org.vsg.cusp.event.codes.BooleanMessageCodec;
import org.vsg.cusp.event.codes.BufferMessageCodec;
import org.vsg.cusp.event.codes.ByteArrayMessageCodec;
import org.vsg.cusp.event.codes.ByteMessageCodec;
import org.vsg.cusp.event.codes.CharMessageCodec;
import org.vsg.cusp.event.codes.DoubleMessageCodec;
import org.vsg.cusp.event.codes.FloatMessageCodec;
import org.vsg.cusp.event.codes.IntMessageCodec;
import org.vsg.cusp.event.codes.LongMessageCodec;
import org.vsg.cusp.event.codes.NullMessageCodec;
import org.vsg.cusp.event.codes.ShortMessageCodec;
import org.vsg.cusp.event.codes.StringMessageCodec;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

/***
 * @deprecated
 * @author ruanweibiao
 *
 */
public class SingleRequestMessage extends AbstractRequestMessageEnvelope {

	
	protected static CodecManager codecManager = new CodecManager();
	
	
	public SingleRequestMessage() {
		
		this.setApiId((byte)1);
		this.setApiVersion((short)1);

	}
	
	
	public CodecManager getCodecManager() {
		return codecManager;
	}
	
	private SingleRequestMessageDecoder reqMessageDecoder = new SingleRequestMessageDecoder();
	
	public RequestMessageDecoder getRequestManagerDecoder() {
		return reqMessageDecoder;
	}
	
	private SingleRequestMessageEncoder reqMessageEncoder = new SingleRequestMessageEncoder();
	
	public RequestMessageEncoder getRequestMessageEncoder() {
		return reqMessageEncoder;
	}
	
	
	private MessageCodec returnMsgCodecbySystemCodeId(byte systemCodeId) {
		MessageCodec  msgCodec = null;
		if ( NullMessageCodec.SYSTEMCODEC_ID == systemCodeId ) {
			msgCodec = codecManager.getCodec("null");
		}
		else if (ByteMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			msgCodec = codecManager.getCodec("byte");
		}
		else if (BooleanMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			msgCodec = codecManager.getCodec("byte");
		}		
		else if (ShortMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			msgCodec = codecManager.getCodec("short");
		}		
		else if (IntMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			msgCodec = codecManager.getCodec("int");
		}		
		else if (LongMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			msgCodec = codecManager.getCodec("long");
		}		
		else if (FloatMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			msgCodec = codecManager.getCodec("float");
		}		
		else if (CharMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			msgCodec = codecManager.getCodec("char");
		}
		else if (DoubleMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			msgCodec = codecManager.getCodec("double");
		}
		else if (StringMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			msgCodec = codecManager.getCodec("string");
		}
		else if (BufferMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			msgCodec = codecManager.getCodec("buffer");
		}			
		else if (ByteArrayMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			msgCodec = codecManager.getCodec("bytearray");
		}			
		else if (OperationEventMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			msgCodec = codecManager.getCodec("operation-event");
		}
		
		return msgCodec;
	}	

	/**
	 * 
	 * define single request message decoder 
	 *
	 */
	private class SingleRequestMessageDecoder implements RequestMessageDecoder {

		/* (non-Javadoc)
		 * @see org.vsg.cusp.event.RequestMessageDecoder#decode(byte[])
		 */
		@Override
		public Message<byte[]> decode(byte[] msgByteContByte) {
			
			//MessageImpl<byte[] , byte[]> msgImpl = new MessageImpl<byte[] , byte[]>();
			MessageImpl<byte[] , byte[]> msgImpl = null;
			
			// --- bind message handle ----
			
			int locFrom = 0;
			int locTo = locFrom + 32;
			byte[] allAddress = java.util.Arrays.copyOfRange(msgByteContByte, locFrom, locTo);
			
			String address= null;
			String replyAddress = null;
			try {
				String allAddressStr = new String(allAddress,"UTF-8");
				int loc = allAddressStr.indexOf("->");
				if (loc > -1) {
					address = allAddressStr.substring(0, loc);
					replyAddress = allAddressStr.substring(loc+2);
				}
				else {
					address = allAddressStr;
				}
				
				msgImpl.setAddress(address);
				msgImpl.setReplyAddress( replyAddress );
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			locFrom = locTo;
			byte[] body = java.util.Arrays.copyOfRange(msgByteContByte, locFrom, msgByteContByte.length);
			msgImpl.setSentBody( body );

			try {
				System.out.println( new String(body,"utf-8") );
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			


			return msgImpl;
		}


	}
	
	
	private class SingleRequestMessageEncoder implements RequestMessageEncoder {

		@Override
		public <T> byte[] encode(Message<T> msg) {
			
			
			// ---- set  query message content ---
			byte[] contBytes = new byte[0];
			try {
				contBytes = createQueryMessage(msg);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			return contBytes;
		}
		
		
		private <T> byte[] createQueryMessage(Message<T> msg) throws UnsupportedEncodingException {
			String replyAddress = msg.replyAddress();
			
			StringBuilder fullAddress = new StringBuilder(msg.address());
			if (null != replyAddress && !replyAddress.equals("")) {
				fullAddress.append("->").append( replyAddress );
			}
			
			int currentMark = 32 -  fullAddress.length();
			if (currentMark > 0) {
				for (int i = 0 ; i < currentMark ;i++) {
					fullAddress.append(" ");
				}
			}
			byte[] content = (byte[])msg.body();
			return Bytes.concat( fullAddress.toString().getBytes("utf-8")  , content);
			
		}
		
	}
	
}
