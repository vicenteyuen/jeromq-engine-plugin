package org.vsg.cusp.event.codes;

import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.event.MessageCodec;

public class CharMessageCodec implements MessageCodec<Character, Character> {

	public static final byte SYSTEMCODEC_ID = 9;
	
	@Override
	public void encodeToWire(Buffer buffer, Character chr) {
		buffer.appendShort((short) chr.charValue());
	}

	@Override
	public Character decodeFromWire(int pos, Buffer buffer) {
		return (char) buffer.getShort(pos);
	}

	@Override
	public Character transform(Character c) {
		// Characters are immutable so just return it
		return c;
	}

	@Override
	public String name() {
		return "char";
	}

	@Override
	public byte systemCodecID() {
		return SYSTEMCODEC_ID;
	}

}
