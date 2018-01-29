package net.xmeter.ethereum.javasampler;

import java.util.logging.Logger;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import net.xmeter.ethereum.common.Constants;
import net.xmeter.ethereum.common.EthTestUtil;

@SuppressWarnings("deprecation")
public class NewAccount extends AbstractJavaSamplerClient implements Constants,ThreadListener {
	private static final Logger logger = Logger.getLogger(NewAccount.class.getCanonicalName());
	
	@Override
	public Arguments getDefaultParameters() {
		Arguments defaultParameters = new Arguments();
		defaultParameters.addArgument("api_endpoint", "http://server:8545");
		defaultParameters.addArgument("secret", TOKEN_DEFAULT);
		return defaultParameters;
	}

	// threadStarted() is not called before runTest()
	@Override
	public void threadStarted() {
		logger.info("*** in threadStarted");
		//httpClient = ConnUtil.getConn(); 
	}
	
	@Override
	public void threadFinished() {
		logger.info("*** in threadFinished");
		/*try {
			httpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		String secret = EthTestUtil.getValue("secret", context, "<secret>");
		String postData = "{\"jsonrpc\":\"2.0\",\"method\":\"personal_newAccount\", \"params\":[\"" + secret + "\"],\"id\":1}";

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
	        	//String accountAddr = JsonPath.read(document, "$.result").toString();
				result.setResponseCode("200");
		        result.setSuccessful(true);
				result.setResponseData(responseMsg);
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
