package sections.datamodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Setting {


    @Expose
    @SerializedName("active_ad")
    private String active_ad;
    @Expose
    @SerializedName("setting_id")
    private String setting_id;
    @Expose
    @SerializedName("channel_address")
    private String channel_address;

    public String getActive_ad() {
        return active_ad;
    }

    public void setActive_ad(String active_ad) {
        this.active_ad = active_ad;
    }

    public String getSetting_id() {
        return setting_id;
    }

    public void setSetting_id(String setting_id) {
        this.setting_id = setting_id;
    }

    public String getChannel_address() {
        return channel_address;
    }

    public void setChannel_address(String channel_address) {
        this.channel_address = channel_address;
    }
}
