/**
 * 
 */
package org.vsg.cusp.eventbus.impl;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

import org.vsg.cusp.event.impl.ZmqcmdHelper;

/**
 * @author Vicente Yuen
 *
 */
public class EventBusOptions {
	
	 /**
	  * The default port to use when clustering = 0 (meaning assign a random port)
	  */
	public static final int DEFAULT_CLUSTER_PORT = 5066;
	
	
	private int workerPort = 5066;
	
	public int getPort() {
		return workerPort;
	}
	
	
	private int brokerPort = 5051;
	
	public int getBrokerPort() {
		return this.brokerPort;
	}
	
	
	private ExecutorService executorService;

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}
	
	
	private List<String> senderHosts = new Vector<String>();

	public List<String> getSenderHosts() {
		return senderHosts;
	}

	public void setSenderHosts(List<String> senderHosts) {
		this.senderHosts = senderHosts;
	}
	
	private String implClsName;

	public String getImplClsName() {
		return implClsName;
	}

	public void setImplClsName(String implClsName) {
		this.implClsName = implClsName;
	}
	
	
	private ZmqcmdHelper cmdHelper;

	public ZmqcmdHelper getCmdHelper() {
		return cmdHelper;
	}

	public void setCmdHelper(ZmqcmdHelper cmdHelper) {
		this.cmdHelper = cmdHelper;
	}
	
	
	
	
	

}
