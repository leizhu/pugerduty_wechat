package pivotal.cf.cloudops;

import org.apache.log4j.Logger;
import pivotal.cf.cloudops.db.ConnectionManager;
import pivotal.cf.cloudops.service.CoreService;
import pivotal.cf.cloudops.service.PugerDutyService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by pivotal on 8/11/15.
 */
public class PagerDutyServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(PagerDutyServlet.class);

    PugerDutyService pagerdutyService = new PugerDutyService();

    public void init() throws ServletException {
        logger.info("Init DB connection ...");
        ConnectionManager.openConnection();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        logger.info("Received message from pagerduty...");
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
        response.setCharacterEncoding("UTF-8");


        pagerdutyService.processRequest(request);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        logger.info("[Get] - Received message from pagerduty...");
    }
}
