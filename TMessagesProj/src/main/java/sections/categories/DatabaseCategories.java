package sections.categories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import telegram.messenger.xtelex.util.Const;

import java.util.ArrayList;
import java.util.List;

import sections.datamodel.Category;
import sections.datamodel.chatobject;

public class DatabaseCategories extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_CATEGORIES = "categories";
    private static final String DATABASE_CATCHATS = "catchats";
    private static final String KEY_CAT_ID = "cat_id";
    private static final String KEY_NAME = "cat_name";
    private static final String KEY_CURRENT_USER = "current_user";


    private static final String SQL_COMMAND_CREATE_CAT_TABLE = "CREATE TABLE " + DATABASE_CATEGORIES + " (" +
            KEY_CAT_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
            KEY_CURRENT_USER + " INTEGER, " +
            KEY_NAME + " TEXT " +
            ")";

    private static final String SQL_COMMAND_CREATE_CATCHATS_CHATS = "CREATE TABLE \"catchats\"" +
            " (\"id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE ," +
            " \"dialog_id\" INTEGER NOT NULL , " +
            "\"catCode\" INTEGER  NOT NULL ," +
            " \"isChannel\" INTEGER NOT NULL  DEFAULT 0," +
            " \"isGroup\" INTEGER NOT NULL  DEFAULT 0," +
            " \"isHidden\" INTEGER NOT NULL  DEFAULT 0)";


    private static final String KEY_ID = "id";
    private static final String KEY_DIALOG_ID = "dialog_id";
    private static final String KEY_HIDE_CODE = "catCode";
    private static final String KEY_IS_CHANNEL = "isChannel";
    private static final String KEY_IS_GROUP = "isGroup";
    private static final String KEY_IS_HIDDEN = "isHidden";


    public DatabaseCategories(Context context) {
        super(context, Const.CAT_DATABASE_NAME, null, Const.CAT_DATABASE_VERSION);
        this.context = context;


    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_COMMAND_CREATE_CAT_TABLE);
            db.execSQL(SQL_COMMAND_CREATE_CATCHATS_CHATS);
        } catch (Exception e) {
            Log.e(Const.TAG, "Create Category Db Table Faild");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public void insert(Category category) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CURRENT_USER, category.getCurrent_user());
        values.put(KEY_NAME, category.getCat_name());
        sqLiteDatabase.insert(DATABASE_CATEGORIES, null, values);

    }

    public void delete(int UID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        sqLiteDatabase.delete(DATABASE_CATEGORIES, KEY_CAT_ID + "= ?", new String[]{String.valueOf(UID)});
        sqLiteDatabase.close(); // Closing database connection
    }

    public List<Category> getAllItms() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DATABASE_CATEGORIES + " WHERE current_user=" + Const.currentAccount, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            while (!cursor.isAfterLast()) {
                Category category = new Category();
                category.setCat_id(cursor.getInt(0));
                category.setCurrent_user(cursor.getInt(1));
                category.setCat_name(cursor.getString(2));
                categories.add(category);
                cursor.moveToNext();

            }
        }
        cursor.close();
        sqLiteDatabase.close();
        return categories;
    }

    public chatobject getItm(int ID) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DATABASE_CATCHATS + " WHERE dialog_id == '" + ID + "'", null);
        chatobject nam = new chatobject();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            nam.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
            nam.setDialog_id(cursor.getInt(cursor.getColumnIndex(KEY_DIALOG_ID)));
            nam.setCatCode(cursor.getInt(cursor.getColumnIndex(KEY_HIDE_CODE)));
            nam.setIsChannel(cursor.getInt(cursor.getColumnIndex(KEY_IS_CHANNEL)));
            nam.setIsGroup(cursor.getInt(cursor.getColumnIndex(KEY_IS_GROUP)));
            nam.setIsHidden(cursor.getInt(cursor.getColumnIndex(KEY_IS_HIDDEN)));

        }
        assert cursor != null;
        cursor.close();
        return nam;
    }


    public int isCategoried(int id) {
        try {
            // open();
            chatobject item = getItm(id);
            close();
            if (item == null)
                return -1;
            else
                return item.getCatCode();
        } catch (Exception e) {

        }

        return -1;

    }

    public void deleteAll(int catID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        sqLiteDatabase.delete(DATABASE_CATCHATS, KEY_HIDE_CODE + "= ?", new String[]{String.valueOf(catID)});
        sqLiteDatabase.close(); // Closing database connection
    }

    public int getCatSize(int id) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) FROM catchats Where catCode == '" + id + "';", null);
        boolean isOk = cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public boolean isExist(chatobject chatobject) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) FROM catchats Where dialog_id == '" + chatobject.getDialog_id() + " '  AND catCode == '" + chatobject.getCatCode() + " ';", null);
        boolean isOk = cursor.moveToFirst();
        return cursor.getInt(0) > 0;

    }

    public void insertChat(chatobject chatobject) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //Open connection to write data
        if (!isExist(chatobject)) {
            ContentValues values = new ContentValues();
            values.put(KEY_DIALOG_ID, chatobject.getDialog_id());
            values.put(KEY_HIDE_CODE, chatobject.getCatCode());
            values.put(KEY_IS_CHANNEL, chatobject.getIsChannel());
            values.put(KEY_IS_GROUP, chatobject.getIsGroup());
            values.put(KEY_IS_HIDDEN, chatobject.getIsHidden());
            // Inserting Row
            sqLiteDatabase.insert(DATABASE_CATCHATS, null, values);
        }

    }


}
