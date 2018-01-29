package net.xmeter.ethereum.sampler;

import java.util.List;

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
public class CompileContract extends AbstractSampler implements Constants {
	private static final long serialVersionUID = 1L;
	
	public void setCode(String code) {
		setProperty(SRCCODE, code);
	}
	
	public String getCode() {
		return getPropertyAsString(SRCCODE, CODE_DEFAULT);
	}
	
	@Override
	public SampleResult sample(Entry arg0) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
		try {
			String code = getCode(); // solidity source code from UI, as a string

			result.sampleStart();
	        String postData = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_compileSolidity\",\"params\":[\"" + code + "\"],\"id\":1001}";
	        result.setRequestHeaders(postData);
	        String responseMsg = EthTestUtil.invokeAPI(null, postData);
	        result.sampleEnd();	   
	        
			Object document = Configuration.defaultConfiguration().jsonProvider().parse(responseMsg);
	        try {
	        	JsonPath.read(document, "$.error").toString();  // detect possible error first
	        	result.setResponseCode("400");
				result.setResponseData(responseMsg);
	        } catch (com.jayway.jsonpath.PathNotFoundException e) {
	        	List <String> binStrList = JsonPath.read(document, "$.result..code");
				String binStr = binStrList.get(0);
				List <net.minidev.json.JSONArray> abiStrList = JsonPath.read(document, "$.result..info.abiDefinition");
				String abiStr = abiStrList.get(0).toString();
				
				result.setResponseCode("200");
				result.setSuccessful(true);
				result.setResponseData(responseMsg);
				JMeterVariables vars = JMeterContextService.getContext().getVariables();
				vars.put("binStr", binStr);  // store as thread variable
				vars.put("abiStr", abiStr);
	        }
		} catch(Exception ex) {
			if (result.getEndTime() == 0) result.sampleEnd(); 
			result.setResponseCode("500");
			result.setResponseData(ex.getMessage().getBytes());
		}
		
		return result;
		
	}
	
}
