/**
 * 
 */
package org.vsg.cusp.event.engine.modules;

import java.io.IOException;
import java.io.InputStream;

import org.vsg.cusp.core.ServEngine;
import org.vsg.cusp.core.Service;
import org.vsg.cusp.engine.zmq.MasterEventBusServEngine;
import org.vsg.cusp.engine.zmq.ReqRepBroker;
import org.vsg.cusp.engine.zmq.ReqRepWorker;
import org.vsg.cusp.event.EventMethodRegister;
import org.vsg.cusp.event.EventTrigger;
import org.vsg.cusp.event.Message;
import org.vsg.cusp.event.MessageBus;
import org.vsg.cusp.event.MessageConsumerBoxFactory;
import org.vsg.cusp.event.MessageEncoder;
import org.vsg.cusp.event.impl.CodecManager;
import org.vsg.cusp.event.impl.DefaultMessageExchangeEncoder;
import org.vsg.cusp.event.impl.EventTriggerImpl;
import org.vsg.cusp.event.impl.MessageBusImpl;
import org.vsg.cusp.event.impl.MessageProvider;
import org.vsg.cusp.event.impl.MessageQueueBoxBean;
import org.vsg.cusp.event.impl.OperationEventMessageCodec;
import org.vsg.cusp.event.impl.ResResultMessageCodec;
import org.vsg.cusp.event.register.EhcacheEventMethodRegister;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;


/**
 * @author Vicente Yuen
 *
 */
public class EventBusMasterEngineModule extends AbstractModule {



	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		
		
		// --- define pre handle object ---
		this.bind(Message.class).toProvider( MessageProvider.class );
		this.bind(MessageEncoder.class).to( DefaultMessageExchangeEncoder.class ).in(  Scopes.SINGLETON  );
		

		CodecManager codecManager = new CodecManager();
		codecManager.registerCodec(new OperationEventMessageCodec());
		codecManager.registerCodec(new ResResultMessageCodec());

		
		this.bind(CodecManager.class).toInstance( codecManager );
		
		
		// --- set the service --
		this.bind(Runnable.class).annotatedWith(Names.named("RequestResponseBroker")).to(ReqRepBroker.class).in( Scopes.SINGLETON );

		
		//this.bind( MessageQueueBox.class ).to( MessageQueueBoxImpl.class ).in( Scopes.SINGLETON );
		
		//this.bind( MessageConsumer.class ).to(  MessageConsumerImpl.class).in( Scopes.SINGLETON );
		// --- create queue box bean ---
		this.bind(MessageConsumerBoxFactory.class).to(MessageQueueBoxBean.class).in( Scopes.SINGLETON );
		
		
		this.bind(Runnable.class).annotatedWith(Names.named("RequestResponseWorker")).to(ReqRepWorker.class).in( Scopes.SINGLETON );
		
		
		// --- start mqbroker and master worker ---
		this.bind( ServEngine.class ).annotatedWith( Names.named( MasterEventBusServEngine.class.getName())).to( MasterEventBusServEngine.class ).in(Scopes.SINGLETON);
		
		bindClientEndpoint();
		bindEventMethodCache();
		
		this.bind( Service.class ).annotatedWith(Names.named(EventWorkerService.class.getName())).to( EventWorkerService.class ).in(Scopes.SINGLETON);

	}

	
	private void bindClientEndpoint() {
	
		// ---- load file  ---
		InputStream inputStream = this.getClass().getResourceAsStream("/conf/flow.json");
		
		try {
			
			JSONObject jsonConf = (JSONObject)JSON.parseObject(inputStream, JSONObject.class);
			String managerImpl = jsonConf.getString("impl");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.bind(EventTrigger.class ).to( EventTriggerImpl.class ).in( Scopes.SINGLETON );
		
		this.bind( MessageBus.class ).annotatedWith(Names.named("EventMethodMessageBus")).to( MessageBusImpl.class ).in( Scopes.SINGLETON );
		
	}
	
	
	private void bindEventMethodCache() {
		this.bind( EventMethodRegister.class ).to(EhcacheEventMethodRegister.class).in( Scopes.SINGLETON );
	}

	
	
}
