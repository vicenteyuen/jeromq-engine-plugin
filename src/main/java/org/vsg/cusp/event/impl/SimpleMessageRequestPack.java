/**
 * 
 */
package org.vsg.cusp.event.impl;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.core.utils.ByteUtils;
import org.vsg.cusp.core.utils.CorrelationIdGenerator;
import org.vsg.cusp.event.MessageCodec;
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
import com.google.common.primitives.Chars;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;

/**
 * @author Vicente Yuen
 *
 */
public class SimpleMessageRequestPack implements MessageRequestPack{

	public static byte APICODEID = 1;
	
	public static short VERSION = 2;
	
	private MessageCodec mesCodes;
	
	private CorrelationIdGenerator correlationIdGenerator;
	
	public CorrelationIdGenerator getCorrelationIdGenerator() {
		return correlationIdGenerator;
	}

	public void setCorrelationIdGenerator(
			CorrelationIdGenerator correlationIdGenerator) {
		this.correlationIdGenerator = correlationIdGenerator;
	}
	
	private byte[] clientMac;

	public byte[] getClientMac() {
		return clientMac;
	}

	public void setClientMac(byte[] clientMac) {
		this.clientMac = clientMac;
	}

	public MessageCodec getMesCodes() {
		return mesCodes;
	}

	public void setMesCodes(MessageCodec mesCodes) {
		this.mesCodes = mesCodes;
	}

	
	public Object  unpackBytes(byte[] inputContent) {
		return null;
	}

	@Override
	public byte[] headerPack() {
		
		long corrId = correlationIdGenerator.generate(0);
		
		byte[] headerBytes = Bytes.concat(
			new byte[]{APICODEID},
			Shorts.toByteArray(VERSION),
			Longs.toByteArray( corrId),
			clientMac
		);
		return headerBytes;
	}
	
	private List<byte[]> sendMsgColl = new LinkedList<byte[]>();
	

	@Override
	public byte[] addMessageBody(Object body) {
		// attribute field ---
		byte systemCodeId = mesCodes.systemCodecID();
		
		// --- check body content ---
		byte[] contentResult = internalMsgBodyConvert(body);
		
		byte[] sysCodeIdBytes = new byte[]{systemCodeId};
		byte[] compressBytes = new byte[]{0}; // none->0 , gzip->1 , snappy->2
		byte[] timestampBytes = Longs.toByteArray(System.currentTimeMillis());

		
		
		// --- msg content leng ---
		byte[] bodyContentLength = Ints.toByteArray( contentResult.length );
		
		byte[] byteContent =  Bytes.concat( sysCodeIdBytes , compressBytes , timestampBytes , bodyContentLength, contentResult );
		
		
		// --- add message ---
		sendMsgColl.add( byteContent );

		return byteContent;
	}
	
	
	private byte[] internalMsgBodyConvert(Object body) {
		byte systemCodeId = mesCodes.systemCodecID();
		byte[] result = null;
		if (NullMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			// --- body is null , byte == 0
			result = new byte[0];
		}
		else if (ByteMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			Byte byteBody = (Byte)body;
			result = new byte[]{byteBody};
		}
		else if (BooleanMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			Boolean booleanBody = (Boolean)body;
			byte vOut = (byte)(booleanBody?1:0);
			result = new byte[]{vOut};
		}
		else if (ShortMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			result = Shorts.toByteArray( (Short)body );
		}		
		else if (IntMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			result = Ints.toByteArray( (Integer)body );
		}
		else if (LongMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			result = Longs.toByteArray( (Long)body );
		}
		else if (FloatMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			result = ByteUtils.float2byte( (Float)body ); 
		}
		else if (DoubleMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			
			result = ByteUtils.double2byte( (Double)body );
		}
		else if (StringMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			
			try {
				String strBody = (String)body;
				result = strBody.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}
		else if (CharMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			Character cha = (Character) body;
			result = Chars.toByteArray(cha);
		}
		else if (BufferMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			Buffer bufferByte = (Buffer)body;
			result = bufferByte.getBytes();
		}		
		else if (ByteArrayMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			result = (byte[])body;
		}
		// --- custom event ---
		else if (OperationEventMessageCodec.SYSTEMCODEC_ID == systemCodeId) {
			result = (byte[])body;
		}
			
		
		return result;
		
	}

	@Override
	public byte[] messagePack() {
		
		long tmpOffSet = 0;
		
		byte[] msgBytes = new byte[]{};
		
		for (int i = 0 ; i < sendMsgColl.size() ; i++) {
			byte[] msgContent = sendMsgColl.get(i);
			byte[] offsetBytes = Longs.toByteArray( tmpOffSet );
			byte[] contLengBytes = Ints.toByteArray( msgContent.length );
			
			msgBytes = Bytes.concat (msgBytes, Bytes.concat( offsetBytes , contLengBytes , msgContent )  );
			
			// --- current message byte ---
			tmpOffSet = tmpOffSet + offsetBytes.length + contLengBytes.length + msgContent.length;
		}

		

		return msgBytes;
	}
	
	
	
	
}
