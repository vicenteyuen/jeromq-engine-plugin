package org.vsg.cusp.event.codes;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.event.MessageCodec;

public class StringMessageCodec implements MessageCodec<String, String> {

	public static final byte SYSTEMCODEC_ID = 8;	
	
	@Override
	public void encodeToWire(Buffer buffer, String s) {
		byte[] strBytes = s.getBytes(Charset.forName("UTF-8"));
		buffer.appendInt(strBytes.length);
		buffer.appendBytes(strBytes);
	}

	@Override
	public String decodeFromWire(int pos, Buffer buffer) {
		int length = buffer.getInt(pos);
		pos += 4;
		byte[] bytes = buffer.getBytes(pos, pos + length);
		String content = null;
		try {
			content = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return content;
	}

	@Override
	public String transform(String s) {
		// Strings are immutable so just return it
		return s;
	}

	@Override
	public String name() {
		return "string";
	}

	@Override
	public byte systemCodecID() {
		return SYSTEMCODEC_ID;
	}

}
