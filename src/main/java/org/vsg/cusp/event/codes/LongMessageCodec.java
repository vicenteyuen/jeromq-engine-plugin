/**
 * 
 */
package org.vsg.cusp.event.codes;

import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.event.MessageCodec;

/**
 * @author Vicente Yuen
 *
 */
public class LongMessageCodec implements MessageCodec<Long, Long> {

	public static final byte SYSTEMCODEC_ID = 5;	
	
	@Override
	public void encodeToWire(Buffer buffer, Long l) {
		buffer.appendLong(l);
	}

	@Override
	public Long decodeFromWire(int pos, Buffer buffer) {
		return buffer.getLong(pos);
	}

	@Override
	public Long transform(Long l) {
		// Longs are immutable so just return it
		return l;
	}

	@Override
	public String name() {
		return "long";
	}

	@Override
	public byte systemCodecID() {
		return SYSTEMCODEC_ID;
	}

}
