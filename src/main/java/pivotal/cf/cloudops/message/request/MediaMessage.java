package pivotal.cf.cloudops.message.request;

/**
 * Created by pivotal on 8/12/15.
 */
public class MediaMessage extends BaseMessage{

    private String mediaId;

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

}
