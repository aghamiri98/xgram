package sections.datamodel;

public class TempChatObject  {
    private int id, dialog_id, catCode;
    private String title;
    private String username;
    private int isChannel = 0, isGroup = 0, isHidden = 0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDialog_id() {
        return dialog_id;
    }

    public void setDialog_id(int dialog_id) {
        this.dialog_id = dialog_id;
    }

    public int getCatCode() {
        return catCode;
    }

    public void setCatCode(int catCode) {
        this.catCode = catCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIsChannel() {
        return isChannel;
    }

    public void setIsChannel(int isChannel) {
        this.isChannel = isChannel;
    }

    public int getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(int isGroup) {
        this.isGroup = isGroup;
    }

    public int getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(int isHidden) {
        this.isHidden = isHidden;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
