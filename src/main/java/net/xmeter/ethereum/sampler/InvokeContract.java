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

public class InvokeContract extends AbstractSampler implements Constants {
	private static final long serialVersionUID = 1L;
	
	public boolean isUseCorrelatedData() {
		return getPropertyAsBoolean("useCorrelatedData");
	}

	public void setUseCorrelatedData(boolean useCorrelatedData) {
		setProperty("useCorrelatedData", useCorrelatedData);
	}
	
	public String getAccount() {
		return getPropertyAsString(ACCOUNT, "null");
	}
	
	public void setAccount(String account) {
		setProperty(ACCOUNT, account);
	}
	
	public String getContractAddress() {
		return getPropertyAsString(CONTRACT_ADDRESS, "null");
	}
	
	public void setContractAddress(String contractAddress) {
		setProperty(CONTRACT_ADDRESS, contractAddress);
	}
	
	public String getInvokeData() {
		return getPropertyAsString(INVOKE_DATA, "null");
	}
	
	public void setInvokeData(String data) {
		setProperty(INVOKE_DATA, data);
	}
	
	public String getAbiFile() {
		return getPropertyAsString(ABI_FILE, ABI_FILE_DEFAULT);
	}
	
	public void setAbiFile(String abiFile) {
		setProperty(ABI_FILE, abiFile);
	}
	
	public String getFunctionName() {
		return getPropertyAsString(FUNCTION_TO_INVOKE, FUNCTION_TO_INVOKE_DEFAULT);
	}
	
	public void setFunctionName(String funcName) {
		setProperty(FUNCTION_TO_INVOKE, funcName);
	}
	
	public String getFunctionParaTypes() {
		return getPropertyAsString(FUNCTION_PARA_TYPES, FUNCTION_PARA_TYPES_DEFAULT);
	}
	
	public void setFunctionParaTypes(String types) {
		setProperty(FUNCTION_PARA_TYPES, types);
	}
	
	public String getFunctionParaVals() {
		return getPropertyAsString(FUNCTION_PARA_VALS, FUNCTION_PARA_VALS_DEFAULT);
	}
	
	public void setFunctionParaVals(String val) {
		setProperty(FUNCTION_PARA_VALS, val);
	}
	
	@Override
	public SampleResult sample(Entry arg0) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
		try {
			/*String realFile = FileUtil.handleSamplerFileName(FileServer.getFileServer().getBaseDir(), getAbiFile());
			String abi = "<realFile content>";
			
			String[] paraTypes = getFunctionParaTypes().split(",");
			if(getFunctionParaTypes().trim().equals("")) {
				paraTypes = new String[0];
			}
			
			String[] paraVals = new String[paraTypes.length];
			String[] tmp = getFunctionParaVals().split(",");
			if(getFunctionParaTypes().trim().equals("")) {
				tmp = new String[0];
			}
			System.arraycopy(tmp, 0, paraVals, 0, tmp.length); */
			
			String contractAddr;
			if (isUseCorrelatedData()) {
				JMeterVariables vars = JMeterContextService.getContext().getVariables();
				contractAddr = (vars.get("contractAddr") == null) ? "null" : vars.get("contractAddr");
			} else {
				contractAddr = getContractAddress();
			}
			String fromUser = getAccount();
			String invokeData = getInvokeData();
			
			result.sampleStart();
	        String postData = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_call\",\"params\":[{\"from\":\"" + fromUser + "\",\"to\":\"" + contractAddr + "\",\"data\":\"" + invokeData
	        		+ "\"}, \"latest\"],\"id\":1005}";
	        result.setRequestHeaders(postData); // A place to hold the request data
        	String responseMsg = EthTestUtil.invokeAPI(null, postData);
        	result.sampleEnd();
        	
        	Object document = Configuration.defaultConfiguration().jsonProvider().parse(responseMsg);
        	try {
        		JsonPath.read(document, "$.error").toString();  // detect possible error first
	        	result.setResponseCode("400");
				result.setResponseData(responseMsg.getBytes()); 
			} catch (com.jayway.jsonpath.PathNotFoundException e) {
        		result.setResponseCode("200");
				result.setSuccessful(true);
				result.setResponseData(responseMsg.getBytes());
        	}
		} catch(Exception ex) {
			if (result.getEndTime() == 0) result.sampleEnd(); 
			result.setResponseCode("500");
			result.setResponseData(ex.getMessage().getBytes());
		}
		
		return result;
		
	}
}
