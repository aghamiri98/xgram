package sections.datamodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Add_Object {


    @Expose
    @SerializedName("isActive")
    private String isActive;
    @Expose
    @SerializedName("type")
    private String type;
    @Expose
    @SerializedName("channel_username")
    private String channel_username;
    @Expose
    @SerializedName("channel_id")
    private String channel_id;
    @Expose
    @SerializedName("force_id")
    private String force_id;

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

    public String getChannel_username() {
        return channel_username;
    }

    public void setChannel_username(String channel_username) {
        this.channel_username = channel_username;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getForce_id() {
        return force_id;
    }

    public void setForce_id(String force_id) {
        this.force_id = force_id;
    }
}
