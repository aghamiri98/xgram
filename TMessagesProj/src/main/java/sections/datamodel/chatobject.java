package sections.datamodel;

import android.os.Parcel;
import android.os.Parcelable;


public class chatobject implements Parcelable {

    private int id, dialog_id, catCode;
    private int isChannel = 0, isGroup = 0, isHidden = 0;


    public chatobject() {
    }


    protected chatobject(Parcel in) {
        id = in.readInt();
        dialog_id = in.readInt();
        catCode = in.readInt();
        isChannel = in.readInt();
        isGroup = in.readInt();
        isHidden = in.readInt();
    }

    public static final Creator<chatobject> CREATOR = new Creator<chatobject>() {
        @Override
        public chatobject createFromParcel(Parcel in) {
            return new chatobject(in);
        }

        @Override
        public chatobject[] newArray(int size) {
            return new chatobject[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(dialog_id);
        dest.writeInt(catCode);
        dest.writeInt(isChannel);
        dest.writeInt(isGroup);
        dest.writeInt(isHidden);
    }
}
