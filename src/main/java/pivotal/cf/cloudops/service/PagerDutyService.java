package pivotal.cf.cloudops.service;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import pivotal.cf.cloudops.constant.ConstantWeChat;
import pivotal.cf.cloudops.db.ConnectionManager;
import pivotal.cf.cloudops.db.PagerDutyDBClient;
import pivotal.cf.cloudops.message.response.Article;
import pivotal.cf.cloudops.message.response.NewsMessage;
import pivotal.cf.cloudops.restclient.HttpRestClient;
import pivotal.cf.cloudops.util.MessageUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by pivotal on 8/6/15.
 */
public class PagerDutyService {
    private static final Logger logger = Logger.getLogger(PagerDutyService.class);

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    HttpRestClient httpRestClient;

    public PagerDutyService() {
        Map<String,String> headerMap = new HashMap<String, String>();
        headerMap.put("Authorization","Token token=yAD3WLwgJYSp1wjV872b");
        headerMap.put("Content-type","application/json");
        httpRestClient = new HttpRestClient(headerMap);
    }

    public String getIncident(int incident_number) {
        String url = "https://pivotalwechat.pagerduty.com/api/v1/incidents/" + String.valueOf(incident_number);
        logger.info("URL of getIncident: " + url);
        try {
            return httpRestClient.sendGET(url);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public String getIncidents(String status, String lastDays) {
        String url = "https://pivotalwechat.pagerduty.com/api/v1/incidents?fields=incident_number,status,html_url";
        if(status.equalsIgnoreCase("open"))
            url += "&status=triggered,acknowledged";
        else if(status.equalsIgnoreCase("resolved"))
            url += "&status=resolved";
        else if(status.equalsIgnoreCase("ack"))
            url += "&status=acknowledged";
        String endDate = dateFormat.format(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0-Integer.parseInt(lastDays));
        String startDate = dateFormat.format(calendar.getTime());
        url += "&since="+startDate;
        url += "&until="+endDate;

        logger.info("URL of getIncidents: " + url);
        try {
            return httpRestClient.sendGET(url);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0-Integer.parseInt("30"));
        String startDate = dateFormat.format(calendar.getTime());
        System.out.println(startDate);
    }

    public void processRequest(HttpServletRequest request) {
        try {
            JSONObject jsonObject = MessageUtil.parseJson(request);
            logger.info("Parsed PagerDuty message: " + jsonObject.toString());

            Connection conn = ConnectionManager.getConnection();
            PagerDutyDBClient.saveIncident(conn, jsonObject);
            ConnectionManager.closeConnection(conn);
        } catch (Exception e) {
            logger.error("Parse Json message error: " + e.getMessage());
        }
    }

    public NewsMessage constructNewsMessage(String fromUserName, String toUserName, String status, String lastDays) {
        logger.info("construct news message ...");
        NewsMessage newsMessage = new NewsMessage();
        newsMessage.setToUserName(fromUserName);
        newsMessage.setFromUserName(toUserName);
        newsMessage.setCreateTime(new Date().getTime());
        newsMessage.setMsgType(ConstantWeChat.RESP_MESSAGE_TYPE_NEWS);
        newsMessage.setFuncFlag(0);
        String result = getIncidents(status,lastDays);
        constructNewsMessage(result, newsMessage);
        return newsMessage;
    }

    private void constructNewsMessage(String jsonStr, NewsMessage newsMessage) {
        // get all the incidents
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONArray jsonArray = jsonObj.getJSONArray("incidents");
            List<Article> articleList = new ArrayList<Article>();
            for (int i=0; i<jsonArray.length() && i<=9; i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int id = obj.getInt("incident_number");
                String incidentJsonStr = getIncident(id);
                articleList.add(constructArticle(incidentJsonStr));
            }
            newsMessage.setArticleCount(articleList.size());
            newsMessage.setArticles(articleList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Article constructArticle(String incidentJsonStr) {
        Article article = new Article();
        try {
            JSONObject incidentObj = new JSONObject(incidentJsonStr);
            String status = incidentObj.getString("status");
            String trigger_data_subject = incidentObj.getJSONObject("trigger_summary_data").getString("subject");
            String html_url = incidentObj.getString("html_url");
            article.setTitle("[" + status + "] " + trigger_data_subject);
            article.setDescription(incidentJsonStr);
            article.setPicUrl("https://pivotalwechat.pagerduty.com/assets/favicon.png");
            article.setUrl(html_url);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return article;
    }
}
