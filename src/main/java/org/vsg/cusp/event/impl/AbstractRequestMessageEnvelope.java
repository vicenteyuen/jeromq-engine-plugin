/**
 * 
 */
package org.vsg.cusp.event.impl;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.vsg.cusp.core.utils.CorrelationIdGenerator;

/**
 * @author Vicente Yuen
 *
 */
public abstract class AbstractRequestMessageEnvelope implements RequestMessageEnvelope {
	
	
	public AbstractRequestMessageEnvelope() {
		init();
	}
	
	
	protected void init() {
		try {
			clientAddress = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	private byte apiId;

	/* (non-Javadoc)
	 * @see org.vsg.cusp.event.impl.RequestMessage#getApiId()
	 */
	@Override
	public byte getApiId() {
		// TODO Auto-generated method stub
		return apiId;
	}
	
	protected void setApiId(byte apiId) {
		this.apiId = apiId;
	}
	
	private short apiVersion;

	/* (non-Javadoc)
	 * @see org.vsg.cusp.event.impl.RequestMessage#getApiVersion()
	 */
	@Override
	public short getApiVersion() {
		// TODO Auto-generated method stub
		return apiVersion;
	}
	
	protected void setApiVersion(short apiVersion) {
		this.apiVersion = apiVersion;
	}
	
	private byte[] clientAddress;

	@Override
	public byte[] getClientAddress() {
		return clientAddress;
	}
	
	

}
