package sections.datamodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public  class Update_Model {

    @Expose
    @SerializedName("isActive")
    private String isActive;
    @Expose
    @SerializedName("update_version")
    private String update_version;
    @Expose
    @SerializedName("file_url")
    private String file_url;
    @Expose
    @SerializedName("update_id")
    private String update_id;

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getUpdate_version() {
        return update_version;
    }

    public void setUpdate_version(String update_version) {
        this.update_version = update_version;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public String getUpdate_id() {
        return update_id;
    }

    public void setUpdate_id(String update_id) {
        this.update_id = update_id;
    }
}
