package org.vsg.cusp.event.codes;

import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.event.MessageCodec;

public class FloatMessageCodec implements MessageCodec<Float, Float> {

	public static final byte SYSTEMCODEC_ID = 6;	
	
	@Override
	public void encodeToWire(Buffer buffer, Float f) {
		buffer.appendFloat(f);
	}

	@Override
	public Float decodeFromWire(int pos, Buffer buffer) {
		return buffer.getFloat(pos);
	}

	@Override
	public Float transform(Float f) {
		// Floats are immutable so just return it
		return f;
	}

	@Override
	public String name() {
		return "float";
	}

	@Override
	public byte systemCodecID() {
		return SYSTEMCODEC_ID;
	}

}
