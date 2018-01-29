package net.xmeter.ethereum.sampler;

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
public class DeployContract extends AbstractSampler implements Constants {
	private static final long serialVersionUID = 1L;
	
	public boolean isUseCorrelatedData() {
		return getPropertyAsBoolean("useCorrelatedData");
	}

	public void setUseCorrelatedData(boolean useCorrelatedData) {
		setProperty("useCorrelatedData", useCorrelatedData);
	}
	
	public String getBinStr() {
		return getPropertyAsString(BINCODE, "");
	}
	
	public void setBinStr(String binStr) {
		setProperty(BINCODE, binStr);
	}
	
	public String getGas() {
		return getPropertyAsString("gas", "");
	}
	
	public void setGas(String gas) {
		setProperty("gas", gas);
	}
	
	public String getFromUser() {
		return getPropertyAsString("fromUser", "");
	}
	
	public void setFromUser(String fromUser) {
		setProperty("fromUser", fromUser);
	}
	
	@Override
	public SampleResult sample(Entry arg0) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
		try {
			String binStr, gas;
			if (isUseCorrelatedData()) {
				JMeterVariables vars = JMeterContextService.getContext().getVariables();
				binStr = (vars.get("binStr") == null) ? "null" : vars.get("binStr");
				gas = (vars.get("gas") == null) ? "null" : vars.get("gas");
			} else {
				binStr = getBinStr();
				gas= getGas();
			}
			String fromUser = getFromUser();
			
			result.sampleStart();
	        String postData = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_sendTransaction\",\"params\":[{\"from\":\"" 
	        		+ fromUser + "\",\"gas\":\"" + gas + "\",\"data\":\"" + binStr + "\"}],\"id\":1003}";
	        result.setRequestHeaders(postData);
	        String responseMsg = EthTestUtil.invokeAPI(null, postData);
	        result.sampleEnd();
	        
	        Object document = Configuration.defaultConfiguration().jsonProvider().parse(responseMsg);
	        try {
	        	JsonPath.read(document, "$.error").toString();  // detect possible error first
	        	result.setResponseCode("400");
				result.setResponseData(responseMsg);
	        } catch (com.jayway.jsonpath.PathNotFoundException e) {
	        	String txHash = JsonPath.read(document, "$.result").toString();
	        	result.setResponseCode("200");
		        result.setSuccessful(true);
				result.setResponseData(responseMsg);
	        	JMeterVariables vars = JMeterContextService.getContext().getVariables();
				vars.put("txHash", txHash);
	        }
		} catch(Exception ex) {
			if (result.getEndTime() == 0) result.sampleEnd(); // avoid twice call sampleEnd()
			result.setResponseCode("500");
			result.setResponseData(ex.getMessage().getBytes());
		}
		
		return result;
	}
	
}
