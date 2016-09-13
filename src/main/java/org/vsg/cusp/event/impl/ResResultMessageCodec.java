package org.vsg.cusp.event.impl;

import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.event.MessageCodec;

public class ResResultMessageCodec implements MessageCodec<ResResult, ResResult> {

	public static final byte SYSTEMCODEC_ID = -1;

	@Override
	public void encodeToWire(Buffer buffer, ResResult s) {


	}

	@Override
	public ResResult decodeFromWire(int pos, Buffer buffer) {
		return null;
	}

	@Override
	public ResResult transform(ResResult s) {
		// TODO Auto-generated method stub
		return s;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "resresult";
	}

	@Override
	public byte systemCodecID() {
		// TODO Auto-generated method stub
		return SYSTEMCODEC_ID;
	}

}