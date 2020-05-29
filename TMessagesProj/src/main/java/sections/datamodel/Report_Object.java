package sections.datamodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Report_Object {


    @Expose
    @SerializedName("isActive")
    private String isActive;
    @Expose
    @SerializedName("description")
    private String description;
    @Expose
    @SerializedName("rp_type")
    private String rp_type;
    @Expose
    @SerializedName("channel_link")
    private String channel_link;
    @Expose
    @SerializedName("channel_id")
    private String channel_id;
    @Expose
    @SerializedName("report_id")
    private String report_id;

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRp_type() {
        return rp_type;
    }

    public void setRp_type(String rp_type) {
        this.rp_type = rp_type;
    }

    public String getChannel_link() {
        return channel_link;
    }

    public void setChannel_link(String channel_link) {
        this.channel_link = channel_link;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getReport_id() {
        return report_id;
    }

    public void setReport_id(String report_id) {
        this.report_id = report_id;
    }
}
