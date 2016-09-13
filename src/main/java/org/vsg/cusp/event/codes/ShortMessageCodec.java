package org.vsg.cusp.event.codes;

import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.event.MessageCodec;

public class ShortMessageCodec implements MessageCodec<Short, Short> {

	public static final byte SYSTEMCODEC_ID = 3;		
	
	@Override
	public void encodeToWire(Buffer buffer, Short s) {
		buffer.appendShort(s);
	}

	@Override
	public Short decodeFromWire(int pos, Buffer buffer) {
		return buffer.getShort(pos);
	}

	@Override
	public Short transform(Short s) {
		// Shorts are immutable so just return it
		return s;
	}

	@Override
	public String name() {
		return "short";
	}

	@Override
	public byte systemCodecID() {
		return SYSTEMCODEC_ID;
	}

}
