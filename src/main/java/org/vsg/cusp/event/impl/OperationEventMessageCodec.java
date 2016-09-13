/**
 * 
 */
package org.vsg.cusp.event.impl;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Vector;

import org.vsg.cusp.core.Buffer;
import org.vsg.cusp.event.EventMethodDescription;
import org.vsg.cusp.event.MessageCodec;
import org.vsg.cusp.event.OperationEvent;
import org.vsg.cusp.event.RuntimeParam;

import com.google.common.primitives.Bytes;

/**
 * @author Vicente Yuen
 *
 */
public class OperationEventMessageCodec implements MessageCodec<OperationEvent, OperationEvent> {
	
	public static final byte SYSTEMCODEC_ID = -1;
	
	public static final String NAME = "operation-event";

	@Override
	public void encodeToWire(Buffer buffer, OperationEvent s) {
		
		EventMethodDescription  evtMethodDesc = s.getMethodDescription();
		
		/**
		 * set the event name
		 */
		buffer.appendString( evtMethodDesc.getEventName() );
		buffer.appendString( "|" );
		
		/**
		 * set the event method descprtion
		 */
		buffer
			.appendString( evtMethodDesc.getMethodName() )
			.appendString(":")
			.appendString( evtMethodDesc.getClzName())
			;
		buffer.appendString( "|" );
		
		
		/**
		 * convert parameter to string
		 */
		buffer.appendBytes( convertToBytes( s.getRuntimeArgument() ));


	}

	@Override
	public OperationEvent decodeFromWire(int pos, Buffer buffer) {
	
		
		StringTokenizer st = new StringTokenizer( new String(buffer.getBytes()) , "\\|" );
		
		OperationEventImpl oeImpl = new OperationEventImpl();
		
		// --- parse event name ---
		String eventName =st.nextToken();
		
		
		// --- parse event 
		String classAndEventId = st.nextToken();
		String[] clsAndEvtIds = classAndEventId.split(":");
		
		EventMethodDescription evtMethodDesc = new EventMethodDescription();
		evtMethodDesc.setEventName( eventName );
		evtMethodDesc.setMethodName( clsAndEvtIds[0] );	
		evtMethodDesc.setClzName( clsAndEvtIds[1] );

		
		String runtimeParamStr = st.nextToken();
		Collection<RuntimeParam> runtimeParam = null;
		
		try {
			runtimeParam = parseStringToRuntimeParam(runtimeParamStr);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			oeImpl.setRuntimeArgument( runtimeParam );
			
		}

		
		return oeImpl;
	}
	
	@Override
	public OperationEvent transform(OperationEvent s) {
		return s;
	}

	private Collection<RuntimeParam> parseStringToRuntimeParam(String runtimeParamStr) throws ClassNotFoundException {
		Collection<RuntimeParam> runtimeParamColl = new Vector<RuntimeParam>();
		 
		StringTokenizer st = new StringTokenizer( runtimeParamStr , ";" );		 
		
		String paramNames = st.nextToken();
		
		String paramTypes = st.nextToken();
		
		String paramValues = st.nextToken();

		String[] paraNamesArray = paramNames.split(",");
		String[] paramTypesArray = paramTypes.split(",");
		String[] paramValuesArray = paramValues.split(",");		
		
		
		for (int i = 0 ; i < paraNamesArray.length ;i++) {
			RuntimeParam runtimeParam = new RuntimeParam();
			runtimeParam.setParamName( paraNamesArray[i] );
			runtimeParam.setParamClzType(  Class.forName( paramTypesArray[i] ) );
			runtimeParam.setParamVal( parseValueToSerializable( paramValuesArray[i]  , runtimeParam.getParamClzType())  );
			
			runtimeParamColl.add( runtimeParam );
		}
		
		
		return runtimeParamColl;
	}	
	
	private java.io.Serializable parseValueToSerializable(String valReceived , Class<?> clzType) {
		String[] valCont = valReceived.split("`");
		
		
		if (clzType.equals( java.lang.String.class )) {
			return valCont[1];
		}
		
		return null;		
	}
	
	
	private byte[] convertToBytes(Collection<RuntimeParam> params) {
		byte[] result = null;
		
		// --- return  parameter ---
		StringBuilder paramTypeStr = new StringBuilder();
		StringBuilder paramNameStr = new StringBuilder();
		StringBuilder paramValueStr = new StringBuilder();
		
		RuntimeParam[] paramArrays = params.toArray(new RuntimeParam[0]);
		
		// --- parse param array ---
		for (int i = 0; i < paramArrays.length ; i++) {
			RuntimeParam param = paramArrays[i];
			
			if (paramNameStr.length() > 0) {
				paramNameStr.append(",");
			}
			
			if (paramTypeStr.length() > 0) {
				paramTypeStr.append(",");
			}
			
			paramNameStr.append( param.getParamName() );
			paramTypeStr.append( param.getParamClzType().getName() );
			byte[] contVal = convertParamValueToBytes( param.getParamVal() );
			paramValueStr.append( contVal.length ).append("`").append( new String(contVal,Charset.forName("UTF-8")) );
		}
		
		
		
		if (paramTypeStr.length() == 0) {
			result = "nil".getBytes(Charset.forName("UTF-8"));
		} else {
			
			// --- merge content ---
			result = Bytes.concat( 
				paramNameStr.toString().getBytes(Charset.forName("UTF-8")),
				";".getBytes(Charset.forName("UTF-8")),
				paramTypeStr.toString().getBytes(Charset.forName("UTF-8")),
				";".getBytes(Charset.forName("UTF-8")),
				paramValueStr.toString().getBytes(Charset.forName("UTF-8"))
				);
			
			
		}
		
		return result;
	}
	
	private byte[] convertParamValueToBytes(java.io.Serializable paramValue ) {
		byte[] result = new byte[0];
		if (paramValue instanceof String) {
			result = ((String)paramValue).getBytes(Charset.forName("UTF-8"));
		}
		
		return result;
		
	}
	
	
	

	

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return NAME;
	}

	@Override
	public byte systemCodecID() {
		// TODO Auto-generated method stub
		return SYSTEMCODEC_ID;
	}

	
	
}
