/**
 * 
 */
package org.vsg.cusp.event.register;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.vsg.cusp.event.EventMethodDescription;
import org.vsg.cusp.event.EventMethodRegister;

/**
 * @author ruanweibiao
 *
 */
public class EhcacheEventMethodRegister implements EventMethodRegister {
	
	
	private Map<String, Set<EventMethodDescription>> localCache = new LinkedHashMap<String,Set<EventMethodDescription>>();

	/* (non-Javadoc)
	 * @see org.vsg.cusp.event.EventMethodRegister#registerEvent(java.lang.String, org.vsg.cusp.event.EventMethodDescription)
	 */
	@Override
	public void registerEvent(String eventName,
			EventMethodDescription methodDescription) {
		
		Set<EventMethodDescription> eventMethodDescVec = new LinkedHashSet<EventMethodDescription>();
		
		if (localCache.containsKey(eventName)) {
			eventMethodDescVec = localCache.get( eventName ); 
		}
		
		eventMethodDescVec.add( methodDescription );
		
		// --- put the collection to exist key ---
		localCache.put( eventName , eventMethodDescVec);
		
	}

	/* (non-Javadoc)
	 * @see org.vsg.cusp.event.EventMethodRegister#findAllRegisterEventsById(java.lang.String)
	 */
	@Override
	public Set<EventMethodDescription> findAllRegisterEventsByName(String eventName) {
		return localCache.getOrDefault(eventName  , new LinkedHashSet<EventMethodDescription>());
	}

	/* (non-Javadoc)
	 * @see org.vsg.cusp.event.EventMethodRegister#unRegisterEvent(java.lang.String)
	 */
	@Override
	public void unRegisterEvent(String eventName) {
		
		if (localCache.containsKey(eventName)) {
			localCache.remove(eventName); 
		}
	}

}
