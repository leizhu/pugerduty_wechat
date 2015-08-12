package pivotal.cf.cloudops.service;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import pivotal.cf.cloudops.db.ConnectionManager;
import pivotal.cf.cloudops.db.PagerDutyDBClient;
import pivotal.cf.cloudops.restclient.HttpRestClient;
import pivotal.cf.cloudops.util.MessageUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pivotal on 8/6/15.
 */
public class PugerDutyService {
    private static final Logger logger = Logger.getLogger(PugerDutyService.class);

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    HttpRestClient httpRestClient;

    public PugerDutyService() {
        Map<String,String> headerMap = new HashMap<String, String>();
        headerMap.put("Authorization","Token token=yAD3WLwgJYSp1wjV872b");
        headerMap.put("Content-type","application/json");
        httpRestClient = new HttpRestClient(headerMap);
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
        String endDate = dateFormat.format(new Date());
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
}
