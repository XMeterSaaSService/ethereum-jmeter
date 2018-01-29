package net.xmeter.ethereum.javasampler;

import java.util.logging.Logger;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import net.xmeter.ethereum.common.Constants;
import net.xmeter.ethereum.common.EthTestUtil;

@SuppressWarnings("deprecation")
public class EstimateGas extends AbstractJavaSamplerClient implements Constants {
	private static final Logger logger = Logger.getLogger(EstimateGas.class.getCanonicalName());
	
	@Override
	public Arguments getDefaultParameters() {
		Arguments defaultParameters = new Arguments();
		defaultParameters.addArgument("api_endpoint", "http://server:8545");
		defaultParameters.addArgument("fromUser", "<fromUser>");
		defaultParameters.addArgument("binStr", "<compiled bin code>");
		return defaultParameters;
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		String fromUser = EthTestUtil.getValue("fromUser", context, "<fromUser>");
		String binStr = EthTestUtil.getValue("binStr", context, "<compiled bin code>");
		
		String postData = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_estimateGas\",\"params\":[{\"from\":\"" + fromUser + "\",\"data\":\"" + binStr + "\"}], \"id\":1002}";
        
		SampleResult result = new SampleResult();
		result.setRequestHeaders(postData);
		try {
			result.sampleStart();
			String responseMsg = EthTestUtil.invokeAPI(context, postData);
			result.sampleEnd();
			
			Object document = Configuration.defaultConfiguration().jsonProvider().parse(responseMsg);
	        try {
	        	JsonPath.read(document, "$.error").toString();  // detect possible error first
	        	result.setResponseCode("400");
				result.setResponseData(responseMsg);
	        } catch (com.jayway.jsonpath.PathNotFoundException e) {
	        	String gas = JsonPath.read(document, "$.result").toString();
	        	result.setResponseCode("200");
		        result.setSuccessful(true);
				result.setResponseData(responseMsg);
	        	JMeterVariables vars = JMeterContextService.getContext().getVariables();
				vars.put("gas", gas);
	        }
		} catch(Exception ex) {
			if (result.getEndTime() == 0) result.sampleEnd(); 
			result.setResponseCode("500");
			result.setSuccessful(false);
			result.setResponseData(ex.getMessage().getBytes());
			logger.severe(ex.getMessage());
		}
		return result;
	}

}
