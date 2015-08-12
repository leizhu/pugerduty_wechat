package pivotal.cf.cloudops.db;


import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pivotal on 8/11/15.
 */
public class PagerDutyDBClient {

    private static final Logger logger = Logger.getLogger(PagerDutyDBClient.class);

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    public static void saveIncident(Connection connection, JSONObject jsonObject) {
        try {
            JSONArray array = jsonObject.getJSONArray("messages");
            JSONObject object = array.getJSONObject(0);
            String type = object.getString("type");
            JSONObject dataObj = object.getJSONObject("data");
            JSONObject incidentObj = dataObj.getJSONObject("incident");
            String incident_id = incidentObj.getString("id");
            String created_on = incidentObj.getString("created_on");
            String html_url = incidentObj.getString("html_url");
            String service_id = incidentObj.getJSONObject("service").getString("id");
            String escalation_policy_id = incidentObj.getJSONObject("escalation_policy").getString("id");
            String assigned_to_user = incidentObj.getJSONObject("assigned_to_user").getString("name");
            String trigger_data_subject = incidentObj.getJSONObject("trigger_summary_data").getString("subject");

            // save to mysql db
            if(isIncidentExist(connection,incident_id))
                updateIncident(connection,incident_id,type,created_on,html_url,service_id,escalation_policy_id,assigned_to_user,trigger_data_subject);
            else
                insertIncident(connection,incident_id,type,created_on,html_url,service_id,escalation_policy_id,assigned_to_user,trigger_data_subject);

            logger.info("Incident[" + incident_id + "] was saved to db successfully.");
        } catch (JSONException e) {
            logger.error("Parse Json error: " + e.getMessage());
        }
    }

    private static void insertIncident(Connection dbConnection, String incident_id, String type, String created_on, String html_url, String service_id, String escalation_policy_id, String assigned_to_user, String trigger_data_subject) {
        PreparedStatement preparedStatement = null;

        String insertTableSQL = "INSERT INTO incidents"
                + "(incident_id, type, html_url, service_id, escalation_policy_id, assigned_to_user, trigger_data_subject, created_on) VALUES"
                + "(?,?,?,?,?,?,?,?)";

        try {
            preparedStatement = dbConnection.prepareStatement(insertTableSQL);

            preparedStatement.setString(1, incident_id);
            preparedStatement.setString(2, type);
            preparedStatement.setString(3, html_url);
            preparedStatement.setString(4, service_id);
            preparedStatement.setString(5, escalation_policy_id);
            preparedStatement.setString(6, assigned_to_user);
            preparedStatement.setString(7, trigger_data_subject);

            Date date = dateFormat.parse(created_on);
            preparedStatement.setDate(8, new java.sql.Date(date.getTime()));

            // execute insert SQL stetement
            preparedStatement.executeUpdate();

            logger.info("Record is inserted into incidents table!");

        } catch (SQLException e) {
            logger.error(e.getMessage());
        } catch (ParseException e) {
            logger.error(e.getMessage());
        } finally {

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    private static void updateIncident(Connection dbConnection, String incident_id, String type, String created_on, String html_url, String service_id, String escalation_policy_id, String assigned_to_user, String trigger_data_subject) {
        PreparedStatement preparedStatement = null;

        String updateTableSQL = "UPDATE incidents SET type = ?,html_url = ?,service_id = ?,escalation_policy_id = ?,assigned_to_user = ?,trigger_data_subject = ?,created_on = ?"
                + " WHERE incident_id = ?";

        try {
            preparedStatement = dbConnection.prepareStatement(updateTableSQL);
            preparedStatement.setString(1, type);
            preparedStatement.setString(2, html_url);
            preparedStatement.setString(3, service_id);
            preparedStatement.setString(4, escalation_policy_id);
            preparedStatement.setString(5, assigned_to_user);
            preparedStatement.setString(6, trigger_data_subject);

            Date date = dateFormat.parse(created_on);
            preparedStatement.setDate(7, new java.sql.Date(date.getTime()));
            preparedStatement.setString(8, incident_id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } catch (ParseException e) {
            logger.error(e.getMessage());
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }
            }
        }

    }

    private static boolean isIncidentExist(Connection dbConnection, String incident_id) {
        boolean ret = false;
        PreparedStatement preparedStatement = null;

        String selectSQL = "SELECT incident_id FROM incidents WHERE incident_id = ?";

        try {
            preparedStatement = dbConnection.prepareStatement(selectSQL);
            preparedStatement.setString(1, incident_id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                ret = true;
                break;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }
            }

        }
        return ret;
    }

    public static void main(String[] args) {
        try {
            String str = FileUtils.readFileToString(new File(Thread.currentThread().getContextClassLoader().getResource("incident.json").getFile()));
            JSONObject jsonObject = new JSONObject(str);
            PagerDutyDBClient.saveIncident(null, jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
