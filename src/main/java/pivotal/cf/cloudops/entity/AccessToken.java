package pivotal.cf.cloudops.entity;

/**
 * Created by pivotal on 8/7/15.
 */
public class AccessToken {

    /**
     *  获取到的凭证
     */
    private String token;

    /**
     *  凭证有效时间，单位：秒
     */
    private int expiresIn;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public AccessToken(String token, int expiresIn) {
        super();
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public AccessToken() {
        super();
    }
}