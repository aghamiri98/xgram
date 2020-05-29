package sections.datamodel;

import android.os.Parcel;
import android.os.Parcelable;

public class Category implements Parcelable {


    private int cat_id;
    private String cat_name;
    private int size=0;
    private int current_user;

    public int getCat_id() {
        return cat_id;
    }

    public void setCat_id(int cat_id) {
        this.cat_id = cat_id;
    }

    public String getCat_name() {
        return cat_name;
    }

    public void setCat_name(String cat_name) {
        this.cat_name = cat_name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getCurrent_user() {
        return current_user;
    }

    public void setCurrent_user(int current_user) {
        this.current_user = current_user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.cat_id);
        dest.writeString(this.cat_name);
        dest.writeInt(this.size);
        dest.writeInt(this.current_user);
    }

    public Category() {
    }

    protected Category(Parcel in) {
        this.cat_id = in.readInt();
        this.cat_name = in.readString();
        this.size = in.readInt();
        this.current_user = in.readInt();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}
