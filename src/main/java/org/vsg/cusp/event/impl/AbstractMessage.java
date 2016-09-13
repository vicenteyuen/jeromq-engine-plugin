package org.vsg.cusp.event.impl;

import org.vsg.cusp.event.Message;
import org.vsg.cusp.event.MessageAware;
import org.vsg.cusp.eventbus.CaseInsensitiveHeaders;
import org.vsg.cusp.eventbus.MultiMap;

public abstract class AbstractMessage<T> implements Message<T> , MessageAware<T>{
	
	private byte msgType;

	@Override	
	public byte msgType() {
		return msgType;
	}

	@Override
	public void setMsgType(byte msgType) {
		this.msgType = msgType;
	}

	private String address;
	
	@Override
	public String address() {
		return this.address;
	}

	private MultiMap headers;	

	@Override
	public MultiMap headers() {
		if (headers == null) {
			// decodeHeaders();
		}
		if (headers == null) {
			headers = new CaseInsensitiveHeaders();
		}
		return headers;
	}
	
	public void setHeaders(MultiMap headers) {
		this.headers = headers;
	}
	
	private T body;


	@Override
	public T body() {
		return body;
	}
	
	@Override
	public void setAddress(String address) {
		// TODO Auto-generated method stub
		this.address = address;
	}

	@Override
	public void setBody(T body) {
		// TODO Auto-generated method stub
		this.body = body;		
	}
	
	private int headPos;
	
	void setHeadPos(int headPos) {
		this.headPos = headPos;
	}
	
	private int bodyPos;
	
	void setBodyPos(int bodyPos) {
		this.bodyPos = bodyPos;
	}

}
