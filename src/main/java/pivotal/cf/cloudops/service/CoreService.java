package pivotal.cf.cloudops.service;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import pivotal.cf.cloudops.constant.ConstantWeChat;
import pivotal.cf.cloudops.util.MessageUtil;
import pivotal.cf.cloudops.message.response.TextMessage;


public class CoreService{

    public static Logger logger = Logger.getLogger(CoreService.class);

    PugerDutyService pugerDutyService = new PugerDutyService();


    public String processRequest(HttpServletRequest request) {
        String respMessage = null;
        try {
            // xml请求解析
            Map<String, String> requestMap = MessageUtil.parseXml(request);

            // 发送方帐号（open_id）
            String fromUserName = requestMap.get("FromUserName");
            // 公众帐号
            String toUserName = requestMap.get("ToUserName");
            // 消息类型
            String msgType = requestMap.get("MsgType");

            TextMessage textMessage = new TextMessage();
            textMessage.setToUserName(fromUserName);
            textMessage.setFromUserName(toUserName);
            textMessage.setCreateTime(new Date().getTime());
            textMessage.setMsgType(ConstantWeChat.RESP_MESSAGE_TYPE_TEXT);
            textMessage.setFuncFlag(0);

            logger.info(" --- Get text input from user --- ");
            logger.info("FromUserName: " + fromUserName);
            logger.info("ToUserName: " + toUserName);
            logger.info("MsgType: " + msgType);

            // 文本消息
            if (msgType.equals(ConstantWeChat.REQ_MESSAGE_TYPE_TEXT)) {
                // 接收用户发送的文本消息内容
                String content = requestMap.get("Content");
                if (content.startsWith("get")) {
                    String[] array = content.split(",");
                    String result = pugerDutyService.getIncidents(array[1],array[2]);
                    textMessage.setContent(result);
                    respMessage = MessageUtil.textMessageToXml(textMessage);
                }
                else {
                    textMessage.setContent("无效的输入, 请按照提示输入:\n例如返回最近3天的未处理的incidents，请输入'get,open,3'");
                    respMessage = MessageUtil.textMessageToXml(textMessage);
                }
            } else if (msgType.equals(ConstantWeChat.REQ_MESSAGE_TYPE_EVENT)) {

            } else if (msgType.equals(ConstantWeChat.REQ_MESSAGE_TYPE_IMAGE)) {

            } else {

            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return respMessage;
    }


}
