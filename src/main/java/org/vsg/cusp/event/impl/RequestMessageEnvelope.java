package org.vsg.cusp.event.impl;


/**
 * 
 * RequestMessage => ApiKey ApiVersion CorrelationId ClientId RequestMessage
  ApiKey => int16
  ApiVersion => int16
  CorrelationId => int32
  ClientId => string
  RequestMessage => MetadataRequest | ProduceRequest | FetchRequest | OffsetRequest | OffsetCommitRequest | OffsetFetchRequest
 * 
 * 
 * 
 * @author Vicente Yuen
 */
public interface RequestMessageEnvelope {
	
	byte getApiId();
	
	short getApiVersion();
	
	byte[] getClientAddress();
}
