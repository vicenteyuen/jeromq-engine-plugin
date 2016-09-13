package org.vsg.cusp.event.impl;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.vsg.cusp.core.utils.CommonUtils;
import org.vsg.cusp.event.Message;
import org.vsg.cusp.event.MessageEncoder;
import org.vsg.cusp.event.ReqMessageModel;
import org.vsg.cusp.event.RequestMessageEncoder;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;

/**
 * Define Message Encoder default instance 
 * @author Vicente Yuen
 *
 */
public class DefaultMessageExchangeEncoder implements MessageEncoder {

	@Override
	public byte[] encode(Message<byte[]> msg) {

		// --- local mac ---
        byte[] mac = null;
		try {
			InetAddress IP = InetAddress.getLocalHost();
			mac = NetworkInterface.getByInetAddress(IP).getHardwareAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		String uid = CommonUtils.getUid( mac );

		// --- paurse corrent id array ---
		String[] correlationIds = uid.split("\\.");
		byte[] correlationIdsPrefix =Longs.toByteArray( Long.parseLong( correlationIds[0] ) ) ;
		byte[] correlationIdsSuffix =Longs.toByteArray( Long.parseLong( correlationIds[1] ) ) ;
		byte[] correlationIdsSeq = Ints.toByteArray( 0 );

		// --- construct base msg ---
		byte[] msgBytes = Bytes.concat(
				new byte[]{msg.msgType()},
				mac,
				Longs.toByteArray( System.currentTimeMillis() ),
				correlationIdsPrefix,
				correlationIdsSuffix,
				correlationIdsSeq,
				msg.body()
				);
		byte[] allBytes = Bytes.concat(Longs.toByteArray( msgBytes.length ) ,msgBytes);

		return allBytes;
	}
	


	@Override
	public Message<byte[]> decode(byte[] msgBytes) {

		ByteArrayMessageImpl _inst = new ByteArrayMessageImpl();
		
		// --- parse message header ---
		parseMessageFromBytes(msgBytes, (AbstractMessage<byte[]>) _inst);
		
		return _inst;
	}
	
	
	private void parseMessageFromBytes(byte[] inputContent , AbstractMessage<byte[]> msg) {
		int locFrom = 0;
		int locTo = locFrom + Long.BYTES;
		
		long msgTotalLength = Longs.fromByteArray( java.util.Arrays.copyOfRange(inputContent, locFrom, locTo) );
		msg.setHeadPos( locTo );
		
		locFrom = locTo;
		locTo = locFrom + 1;
		
		byte msgType = java.util.Arrays.copyOfRange(inputContent, locFrom, locTo)[0];
		msg.setMsgType( msgType );
		
		// --- get clinet mac ---
		locFrom = locTo;
		locTo = locFrom + 6;
		try {
			byte[] macBytes = java.util.Arrays.copyOfRange(inputContent, locFrom, locTo);
			
            StringBuffer sb = new StringBuffer(); 			
            for(int i=0;i<macBytes.length;i++){  
                if(i!=0){  
                    sb.append("-");  
                }  
                //mac[i] & 0xFF 是为了把byte转化为正整数  
                String s = Integer.toHexString(macBytes[i] & 0xFF);  
                sb.append(s.length()==1?0+s:s);  
            } 			
			msg.headers().add(Message.HeaderKey.PUBLISHER, sb.toString());
		} finally {
			
		}

		locFrom = locTo;
		locTo = locFrom + Long.BYTES;		
		long publisherSentTime = Longs.fromByteArray( java.util.Arrays.copyOfRange(inputContent, locFrom, locTo) );
		msg.headers().add(Message.HeaderKey.SENT_TIME, Long.toString( publisherSentTime ));

		locFrom = locTo;
		locTo = locFrom + Long.BYTES;
		long corrIdPrefix = Longs.fromByteArray( java.util.Arrays.copyOfRange(inputContent, locFrom, locTo) );
		
		locFrom = locTo;
		locTo = locFrom + Long.BYTES;
		long corrIdSubfix = Longs.fromByteArray( java.util.Arrays.copyOfRange(inputContent, locFrom, locTo) );
		
		locFrom = locTo;
		locTo = locFrom + Integer.BYTES;
		int corrIdSeq = Ints.fromByteArray( java.util.Arrays.copyOfRange(inputContent, locFrom, locTo) );
		
		StringBuilder corrId = new StringBuilder(Long.toString( corrIdPrefix ));
		corrId.append(".").append(Long.toString( corrIdSubfix ));
		corrId.append(".").append( Integer.toString( corrIdSeq ) );
		msg.headers().add(Message.HeaderKey.CORRID, corrId.toString());

		
		
		/**
		 * build array content
		 */
		msg.setBodyPos(locTo);
		locFrom = locTo;
		locTo = inputContent.length;
		
		/**
		 * parse body from msgContent
		 * 
		 */
		byte[] body = java.util.Arrays.copyOfRange(inputContent, locFrom, locTo) ;
		msg.setBody(body);
		
	}
	
	
	

}
