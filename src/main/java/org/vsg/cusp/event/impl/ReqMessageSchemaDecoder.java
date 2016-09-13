package org.vsg.cusp.event.impl;

import org.vsg.cusp.event.ReqMessageModel;

public interface ReqMessageSchemaDecoder {

	ReqMessageModel decode(byte[] inputContent);	
	
}
