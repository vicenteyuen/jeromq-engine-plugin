package org.vsg.cusp.event.impl;

import org.vsg.cusp.event.ReqMessageModel;

public interface ReqMessageSchemaEncoder {

	byte[] encode(ReqMessageModel model);
	
	ReqMessageModel genFromBodyContent(byte[] bodyContent ,  RequestMessageEnvelope requestMessage);
	
}
