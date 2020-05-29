package sections.datamodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MtProxy {


    @Expose
    @SerializedName("link")
    private String link;
    @Expose
    @SerializedName("isActive")
    private String isActive;
    @Expose
    @SerializedName("type")
    private String type;
    @Expose
    @SerializedName("secret")
    private String secret;
    @Expose
    @SerializedName("port")
    private String port;
    @Expose
    @SerializedName("HostName")
    private String HostName;
    @Expose
    @SerializedName("proxy_id")
    private String proxy_id;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getHostName() {
        return HostName;
    }

    public void setHostName(String HostName) {
        this.HostName = HostName;
    }

    public String getProxy_id() {
        return proxy_id;
    }

    public void setProxy_id(String proxy_id) {
        this.proxy_id = proxy_id;
    }
}
