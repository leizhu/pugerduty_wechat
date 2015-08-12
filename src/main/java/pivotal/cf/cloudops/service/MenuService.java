package pivotal.cf.cloudops.service;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import pivotal.cf.cloudops.util.WechatUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by pivotal on 8/7/15.
 */
public class MenuService {

    public static Logger logger = Logger.getLogger(MenuService.class);

    /**
     * 菜单创建（POST） 限100（次/天）
     */
    public static String MENU_CREATE = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";

    /**
     * 菜单查询
     */
    public static String MENU_GET = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=ACCESS_TOKEN";

    /**
     * 创建菜单
     *
     * @param jsonMenu
     *            json格式
     * @return 状态 0 表示成功、其他表示失败
     */
    public static Integer createMenu(String jsonMenu) {
        int result = 0;
        String token = WechatUtil.getToken();
        if (token != null) {
            // 拼装创建菜单的url
            String url = MENU_CREATE.replace("ACCESS_TOKEN", token);
            // 调用接口创建菜单
            JSONObject jsonObject = WechatUtil.httpsRequest(url, "POST", jsonMenu);

            if (null != jsonObject) {
                try {
                    if (0 != jsonObject.getInt("errcode")) {
                        result = jsonObject.getInt("errcode");
                        logger.error("创建菜单失败 errcode:" + jsonObject.getInt("errcode")
                                + "，errmsg:" + jsonObject.getString("errmsg"));
                    }
                } catch (JSONException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return result;
    }


//    public static Integer createMenu(Menu menu) {
//        return createMenu(JSONObject.fromObject(menu).toString());
//    }



    public static JSONObject getMenuJson() {
        JSONObject result = null;
        String token = WechatUtil.getToken();
        if (token != null) {
            String url = MENU_GET.replace("ACCESS_TOKEN", token);
            result = WechatUtil.httpsRequest(url, "GET", null);
        }
        return result;
    }


    public static void main(String[] args) {
        try {
            String str = FileUtils.readFileToString(new File(Thread.currentThread().getContextClassLoader().getResource("menu.json").getFile()));
            System.out.println(getMenuJson());
            createMenu(str);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
