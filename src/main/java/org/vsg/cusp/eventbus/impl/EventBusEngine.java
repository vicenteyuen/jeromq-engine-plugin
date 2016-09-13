package org.vsg.cusp.eventbus.impl;

import java.util.concurrent.ExecutorService;

import org.vsg.cusp.engine.zmq.ReqRepBroker;

public class EventBusEngine  {
	
	private EventBusOptions options;
	
	public EventBusEngine(EventBusOptions options) {
		this.options = options;
	}
	
	
	public void startup() throws Exception {
		ReqRepBroker rrbroker = new ReqRepBroker(options.getBrokerPort());
		
		ExecutorService eservice = options.getExecutorService();
		eservice.submit( rrbroker );		
	}

	
	
	
	
}
