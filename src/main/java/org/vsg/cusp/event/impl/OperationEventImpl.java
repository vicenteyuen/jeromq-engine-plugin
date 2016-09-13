/**
 * 
 */
package org.vsg.cusp.event.impl;

import java.util.Collection;

import org.vsg.cusp.event.EventMethodDescription;
import org.vsg.cusp.event.OperationEvent;
import org.vsg.cusp.event.RuntimeParam;

/**
 * @author Vicente Yuen
 *
 */
public class OperationEventImpl implements OperationEvent {
	
	
	
	private Collection<RuntimeParam> arguments;



	@Override
	public void setRuntimeArgument(Collection<RuntimeParam> arguments) {
		this.arguments = arguments;
	}

	@Override
	public Collection<RuntimeParam> getRuntimeArgument() {
		return arguments;
	}

	@Override
	public EventMethodDescription getMethodDescription() {
		return methodDescription;
	}
	
	private EventMethodDescription methodDescription;

	@Override
	public void setMethodDescription(EventMethodDescription methodDescription) {
		this.methodDescription = methodDescription;
		
	}
	


	

}
