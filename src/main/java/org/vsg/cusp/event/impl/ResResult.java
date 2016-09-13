package org.vsg.cusp.event.impl;

import java.io.Serializable;

public class ResResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 620123800274093391L;
	
	
	private short cmdNum;
	
	private String msg;
	
	private Object data;

	public short getCmdNum() {
		return cmdNum;
	}

	public void setCmdNum(short cmdNum) {
		this.cmdNum = cmdNum;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	

}
