package org.vsg.cusp.event.codes;

import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.event.MessageCodec;

public class ByteArrayMessageCodec implements MessageCodec<byte[], byte[]> {

	public static final byte SYSTEMCODEC_ID = 11;

	@Override
	public void encodeToWire(Buffer buffer, byte[] byteArray) {
		buffer.appendInt(byteArray.length);
		buffer.appendBytes(byteArray);
	}

	@Override
	public byte[] decodeFromWire(int pos, Buffer buffer) {
		int length = buffer.getInt(pos);
		pos += 4;
		return buffer.getBytes(pos, pos + length);
	}

	@Override
	public byte[] transform(byte[] bytes) {
		byte[] copied = new byte[bytes.length];
		System.arraycopy(bytes, 0, copied, 0, bytes.length);
		return copied;
	}

	@Override
	public String name() {
		return "bytearray";
	}

	@Override
	public byte systemCodecID() {
		return SYSTEMCODEC_ID;
	}

}
