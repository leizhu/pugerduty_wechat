package pivotal.cf.cloudops.restclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

/**
 * Created by pivotal on 8/6/15.
 */
public class HttpRestClient {
    private static final Logger logger = Logger.getLogger(HttpRestClient.class);

    Map<String,String> headerMap;

    public HttpRestClient() {
        this.headerMap = null;
    }

    public HttpRestClient(Map<String,String> headerMap) {
        this.headerMap = headerMap;
    }



    public String sendGET(String getURL) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(getURL);
        if(headerMap != null) {
            Iterator iterator = headerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String,String> obj = (Map.Entry) iterator.next();
                httpGet.addHeader(obj.getKey(), obj.getValue());
            }
        }

        logger.info("Request: " + httpGet.getURI());

        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

        logger.info("GET Response Status:: "
                + httpResponse.getStatusLine().getStatusCode());

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpResponse.getEntity().getContent()));

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        reader.close();

        // print result
        logger.info("Response:" + response.toString());
        httpClient.close();
        return response.toString();
    }

    public void sendPOST(String postURL) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(postURL);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("userName", "Pankaj Kumar"));

        HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);
        httpPost.setEntity(postParams);

        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

        logger.info("POST Response Status:: "
                + httpResponse.getStatusLine().getStatusCode());

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpResponse.getEntity().getContent()));

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        reader.close();

        // print result
        logger.info(response.toString());
        httpClient.close();

    }

    public static void main(String[] args) throws IOException {
//        String GET_URL = "https://pivotalwechat.pagerduty.com/api/v1/incidents?since=2015-08-06T00:00+08&until=2015-08-07T00:00+08&status=triggered&fields=incident_number,status,html_url";
//        Map<String,String> headerMap = new HashMap<String, String>();
//        headerMap.put("Authorization","Token token=yAD3WLwgJYSp1wjV872b");
//        headerMap.put("Content-type","application/json");
//        HttpRestClient httpRestClient = new HttpRestClient(headerMap);
//        System.out.println(httpRestClient.sendGET(GET_URL));

    }

}
