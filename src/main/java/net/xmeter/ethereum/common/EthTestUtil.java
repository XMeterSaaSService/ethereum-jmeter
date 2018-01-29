package net.xmeter.ethereum.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

public class EthTestUtil {
	private static final Logger logger = Logger.getLogger(EthTestUtil.class.getCanonicalName());
	
	public static CloseableHttpClient getConn() {
		return HttpClients.createDefault();
	}
		
	private static HttpRequestBase setHeaders(HttpRequestBase httpRequest, Map<String, String> headers) {
		if (headers != null) {
			Iterator<String> iter = headers.keySet().iterator();
			while(iter.hasNext()) {
				String name = iter.next();
				String value = headers.get(name);
				httpRequest.setHeader(name, value);
			}
		}
		return httpRequest;
	}
	
	public static String getContentByPost(CloseableHttpClient httpclient, HttpPost post, Map<String, String> headers) throws Exception {
		try {
			setHeaders(post, headers);
			CloseableHttpResponse response = httpclient.execute(post);
			
			int code = response.getStatusLine().getStatusCode();
			if (code != 200) {
				String message = MessageFormat.format("Failed to post data to url {0} with response code {1}.", post.getURI(), code);
				logger.severe(message);
				return null;
			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
				StringBuffer contents = new StringBuffer();
				String line = null;
				while ((line = reader.readLine()) != null) {
					contents.append(line);
				}
				reader.close();
				return contents.toString();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}
	
	// JavaSampler: thread var, then JavaSampler parameter
	// UISampler: context == null, thread var only
	public static String getValue(String key, JavaSamplerContext context, String defaultValue) {
		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		String value = vars.get(key);
		if (value == null && context != null) {
			value = context.getParameter(key, defaultValue);
		}
		return value;
	}
	
	public static String invokeAPI(JavaSamplerContext context, String postData) throws Exception {
		String api_endpoint = getValue("api_endpoint", context, "http://server:8545");
		
		String threadName = JMeterContextService.getContext().getThread().getThreadName();
		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		CloseableHttpClient httpClient = (CloseableHttpClient) vars.getObject("httpClient");
		if (httpClient == null) {  // reuse httpClient across the thread !
			logger.info("*** creating httpClient .. " + threadName);
			httpClient = EthTestUtil.getConn();
			vars.putObject("httpClient", httpClient);
		} else {
			logger.info("*** reuse httpClient .. " + threadName);
		}
		
		HttpPost httpPost = new HttpPost(api_endpoint);
		logger.info("** postData = " + postData);
		StringEntity stringEntity = new StringEntity(postData, "UTF-8");  
		stringEntity.setContentType("application/x-www-form-urlencoded");  
		httpPost.setEntity(stringEntity);  
		String responseMsg = getContentByPost(httpClient, httpPost, null);
		logger.info("** responseMsg = " + responseMsg);
		
		return responseMsg;
	}

}
