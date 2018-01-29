package net.xmeter.ethereum.sampler;

import java.util.concurrent.TimeUnit;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import net.xmeter.ethereum.common.Constants;
import net.xmeter.ethereum.common.EthTestUtil;

@SuppressWarnings("deprecation")
public class GetTxReceipt extends AbstractSampler implements Constants {
	private static final long serialVersionUID = 1L;
	private static final int pollInterval = 3; // unit: seconds
	
	public boolean isUseCorrelatedData() {
		return getPropertyAsBoolean("useCorrelatedData");
	}

	public void setUseCorrelatedData(boolean useCorrelatedData) {
		setProperty("useCorrelatedData", useCorrelatedData);
	}
	
	public String getTxHash() {
		return getPropertyAsString("txHash", "");
	}
	
	public void setTxHash(String txHash) {
		setProperty("txHash", txHash);
	}
	
	@Override
	public SampleResult sample(Entry arg0) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
		try {
			String txHash;
			if (isUseCorrelatedData()) {
				JMeterVariables vars = JMeterContextService.getContext().getVariables();
				txHash = (vars.get("txHash") == null) ? "null" : vars.get("txHash");
			} else {
				txHash = getTxHash();
			}
			
			result.sampleStart();
	        String postData = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionReceipt\",\"params\":[\"" + txHash + "\"],\"id\":1004}";
	        result.setRequestHeaders(postData);
	        String contractAddr, responseMsg;
	        while (true) {
	        	responseMsg = EthTestUtil.invokeAPI(null, postData);
	        	Object document = Configuration.defaultConfiguration().jsonProvider().parse(responseMsg);
	        	try {
	        		JsonPath.read(document, "$.error").toString();  // detect possible error first
    	        	result.setResponseCode("400");
    				result.setResponseData(responseMsg); 
    				result.sampleEnd();
    				return result;
	        	} catch (com.jayway.jsonpath.PathNotFoundException e) {
	        		try {
	        			contractAddr = JsonPath.read(document, "$.result.contractAddress").toString();
	        			break;
	        		} catch (com.jayway.jsonpath.PathNotFoundException e2) {
	        			TimeUnit.SECONDS.sleep(pollInterval);
	        		}
	        	}
	        }
	        result.sampleEnd();
        	result.setResponseCode("200");
	        result.setSuccessful(true);
			result.setResponseData(responseMsg);
        	JMeterVariables vars = JMeterContextService.getContext().getVariables();
			vars.put("contractAddr", contractAddr);
		} catch(Exception ex) {
			if (result.getEndTime() == 0) result.sampleEnd(); // avoid twice call sampleEnd()
			result.setResponseCode("500");
			result.setResponseData(ex.getMessage().getBytes());
		}
		
		return result;
	}
	
}
