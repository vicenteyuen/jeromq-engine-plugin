package org.vsg.cusp.event.codes;

import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.event.MessageCodec;

public class ByteMessageCodec implements MessageCodec<Byte, Byte> {

	public static final byte SYSTEMCODEC_ID = 1;
	
	@Override
	public void encodeToWire(Buffer buffer, Byte b) {
		buffer.appendByte(b);
	}

	@Override
	public Byte decodeFromWire(int pos, Buffer buffer) {
		return buffer.getByte(pos);
	}

	@Override
	public Byte transform(Byte b) {
		// Bytes are immutable so just return it
		return b;
	}

	@Override
	public String name() {
		return "byte";
	}

	@Override
	public byte systemCodecID() {
		return SYSTEMCODEC_ID;
	}

}
