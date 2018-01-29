package net.xmeter.ethereum.javasampler;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import net.xmeter.ethereum.common.Constants;
import net.xmeter.ethereum.common.EthTestUtil;

@SuppressWarnings("deprecation")
public class Transfer extends AbstractJavaSamplerClient implements Constants {
	private static final Logger logger = Logger.getLogger(Transfer.class.getCanonicalName());
	
	@Override
	public Arguments getDefaultParameters() {
		Arguments defaultParameters = new Arguments();
		defaultParameters.addArgument("api_endpoint", "http://server:8545");
		defaultParameters.addArgument("fromAccount", "0x1234");
		defaultParameters.addArgument("toAccount", "0xabcd");
		defaultParameters.addArgument("amount", "0x100");
		return defaultParameters;
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		String fromAccount = EthTestUtil.getValue("fromAccount", context, "<fromAccount>");
		String toAccount = EthTestUtil.getValue("toAccount", context, "<toAccount>");
		String amount = EthTestUtil.getValue("amount", context, "<amount>");
		
		String postData = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_sendTransaction\",\"params\":[{\"from\": \"" +
				fromAccount + "\",\"to\":\"" + toAccount + "\",\"value\":\"" + amount + "\"}],\"id\":3}";
		
		SampleResult result = new SampleResult();
		result.setRequestHeaders(postData);
		try {
			String prevBalance = getBalance(toAccount);
		
			try {
				result.sampleStart();
				String responseMsg = EthTestUtil.invokeAPI(context, postData);
				
				Object document = Configuration.defaultConfiguration().jsonProvider().parse(responseMsg);
				try {
		        	JsonPath.read(document, "$.error").toString();  // detect possible error first
		        	result.setResponseCode("400");
					result.setResponseData(responseMsg);
					return result;
		        } catch (com.jayway.jsonpath.PathNotFoundException e) {
		        	//String txHash = JsonPath.read(document, "$.result").toString();
		        	// how to mark a transaction completion? 
					// 1. when balance is changed
					// 2. via eth_getTransactionReceipt when returned json result is not null
		        }
				
				while (prevBalance.equals(getBalance(toAccount))) {
					TimeUnit.SECONDS.sleep(3);
					logger.info("*** call getBalance for toAccount");
				}
				result.sampleEnd();
				
				result.setResponseCode("200");
				result.setSuccessful(true);
				result.setResponseData(responseMsg);
			} catch(Exception ex) {
				logger.severe(ex.getMessage());
				result.setResponseCode("500");
				result.setSuccessful(false);
				result.setResponseData(ex.getMessage().getBytes());
			}
		}  catch(Exception ex) {
			logger.severe(ex.getMessage());
			result.setResponseCode("500");
			result.setSuccessful(false);
			result.setResponseData(ex.getMessage().getBytes());
		}
		return result;
	}
	
	private String getBalance(String account) throws Exception {
		String postData = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBalance\",\"params\":[\"" +
				account + "\",\"latest\"],\"id\":5}";

		String responseMsg = EthTestUtil.invokeAPI(null, postData);
		Object document = Configuration.defaultConfiguration().jsonProvider().parse(responseMsg);
		String balance = JsonPath.read(document, "$.result").toString();

		return balance;
	}

}
