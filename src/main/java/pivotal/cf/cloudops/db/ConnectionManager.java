/*
 * Copyright 2011 - 2014 Cetas Software, Inc. All rights reserved. This is Cetas
 * proprietary and confidential material and its use is subject to license terms.
 */

package pivotal.cf.cloudops.db;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class ConnectionManager {

    private static final Logger logger = Logger.getLogger(ConnectionManager.class);

    private static ComboPooledDataSource ds = new ComboPooledDataSource();

    public static void openConnection() {

        try {
            String username = null;
            String password = null;
            String jdbcUrl = null;

            String svcConfStr = System.getenv("VCAP_SERVICES");
            if(null == svcConfStr || svcConfStr.equals("")){
                return;
            }

            logger.debug("p-mysql service credential string: " + svcConfStr);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, List<Map<String, Object>>> svcConfMap = objectMapper.readValue(svcConfStr, Map.class);
            List<Map<String, Object>> mysqlInfoList = svcConfMap.get("p-mysql");
            if (mysqlInfoList==null || mysqlInfoList.size()==0) {
                return;
            }
            Map<String, Object> mysqlInfo = mysqlInfoList.get(0);
            Map<String, Object> credentials = (Map<String, Object>) mysqlInfo.get("credentials");
            username = (String) credentials.get("username");
            password = (String) credentials.get("password");
            int port = (Integer)credentials.get("port");
            String hostname = (String) credentials.get("hostname");
            String dbname = (String) credentials.get("name");

            jdbcUrl = "jdbc:mysql://" + hostname + ":" + port +  "/" + dbname
                     + "?characterEncoding=UTF-8";

            logger.info(jdbcUrl);

            if (null != jdbcUrl && null != username && null != password) {
                ds.setJdbcUrl(jdbcUrl);
                ds.setUser(username);
                ds.setPassword(password);
            } else {
                logger.error("Failed to get p-mysql service credentials.");
            }
        } catch (Exception e) {
            logger.error("Failed to get p-mysql service credentials: " + e.getMessage());
        }

    }

    public static Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeConnection(Connection connection, Statement statement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

