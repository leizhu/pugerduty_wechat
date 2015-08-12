package pivotal.cf.cloudops;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import pivotal.cf.cloudops.util.SignUtil;

/**
 * Created by pivotal on 8/4/15.
 */
@Path("/")
public class CloudOpsRestService {
    private static final Logger logger = Logger.getLogger(CloudOpsRestService.class);
    @GET
    @Path("/wechatCore")
    public Response wechatCore(@QueryParam("signature") String signature,
                               @QueryParam("timestamp") String timestamp,
                               @QueryParam("nonce") String nonce,
                               @QueryParam("echostr") String echostr) {
        logger.info("Receive access info from wechat...");
        if (SignUtil.checkSignature(signature, timestamp, nonce)) {
            logger.info("Access wechat successfully, echo string is: " + echostr);
            return Response.ok().entity(echostr).build();
        }else{
            logger.info("Access wechat failed!" + echostr);
            return Response.status(Response.Status.UNAUTHORIZED).entity("Not allowed to access wechat!").build();
        }
    }
}
