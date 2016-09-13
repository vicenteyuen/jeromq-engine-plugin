package org.vsg.cusp.event.codes;

import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.event.MessageCodec;

public class NullMessageCodec implements MessageCodec<String, String> {

	public static final byte SYSTEMCODEC_ID = 0;		
	
	@Override
	public void encodeToWire(Buffer buffer, String s) {
	}

	@Override
	public String decodeFromWire(int pos, Buffer buffer) {
		return null;
	}

	@Override
	public String transform(String s) {
		return null;
	}

	@Override
	public String name() {
		return "null";
	}

	@Override
	public byte systemCodecID() {
		return SYSTEMCODEC_ID;
	}

}
