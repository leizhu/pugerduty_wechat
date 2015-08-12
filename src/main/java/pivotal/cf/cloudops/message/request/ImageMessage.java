package pivotal.cf.cloudops.message.request;

/**
 * Created by pivotal on 8/12/15.
 */
public class ImageMessage extends MediaMessage{

    private String picUrl;

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public ImageMessage(String picUrl) {
        super();
        this.picUrl = picUrl;
    }

}