package com.nerdability.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.nerdability.android.rss.domain.Article;

public class DbAdapter{
	
	public static final String KEY_ROWID = BaseColumns._ID;
	public static final String KEY_GUID = "guid";
	public static final String KEY_READ = "read";
	public static final String KEY_OFFLINE = "offline";    
	
	private static final String DATABASE_NAME = "blogposts";
	private static final String DATABASE_TABLE = "blogpostlist";
	private static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_CREATE_LIST_TABLE = "create table " + DATABASE_TABLE + " (" + 
																KEY_ROWID +" integer primary key autoincrement, "+ 
																KEY_GUID + " text not null, " +
																KEY_READ + " boolean not null, " + 
																KEY_OFFLINE + " boolean not null);";


	private SQLiteHelper sqLiteHelper;
	private SQLiteDatabase sqLiteDatabase;
	private Context context;

	public DbAdapter(Context c){
		context = c;
	}

	public DbAdapter openToRead() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
		sqLiteDatabase = sqLiteHelper.getReadableDatabase();
		return this; 
	}

	public DbAdapter openToWrite() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
		sqLiteDatabase = sqLiteHelper.getWritableDatabase();
		return this; 
	}

	public void close(){
		sqLiteHelper.close();
	}

	public class SQLiteHelper extends SQLiteOpenHelper {
		public SQLiteHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_LIST_TABLE);
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE );
            onCreate(db);
		}
	}

    public long insertBlogListing(String guid) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_GUID, guid);
        initialValues.put(KEY_READ, false);
        initialValues.put(KEY_OFFLINE, false);
        return sqLiteDatabase.insert(DATABASE_TABLE, null, initialValues);
    }
    
    public Article getBlogListing(String guid) throws SQLException {
        Cursor mCursor =
        		sqLiteDatabase.query(true, DATABASE_TABLE, new String[] {
                		KEY_ROWID,
                		KEY_GUID, 
                		KEY_READ,
                		KEY_OFFLINE
                		}, 
                		KEY_GUID + "= '" + guid + "'", 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null && mCursor.getCount() > 0) {
        	mCursor.moveToFirst();
        	Article a = new Article();
   			a.setGuid(mCursor.getString(mCursor.getColumnIndex(KEY_GUID)));
   			a.setRead(mCursor.getInt(mCursor.getColumnIndex(KEY_READ)) > 0);
   			a.setDbId(mCursor.getLong(mCursor.getColumnIndex(KEY_ROWID)));
   			a.setOffline(mCursor.getInt(mCursor.getColumnIndex(KEY_OFFLINE)) > 0);
   			return a;
        }
        return null;
    }
    
    public boolean markAsUnread(String guid) {
        ContentValues args = new ContentValues();
        args.put(KEY_READ, false);
        return sqLiteDatabase.update(DATABASE_TABLE, args, KEY_GUID + "='" + guid+"'", null) > 0;
    }
    
    public boolean markAsRead(String guid) {
        ContentValues args = new ContentValues();
        args.put(KEY_READ, true);
        return sqLiteDatabase.update(DATABASE_TABLE, args, KEY_GUID + "='" + guid+"'", null) > 0;
    }

    public boolean saveForOffline(String guid) {
        ContentValues args = new ContentValues();
        args.put(KEY_OFFLINE, true);
        return sqLiteDatabase.update(DATABASE_TABLE, args, KEY_GUID + "='" + guid+"'", null) > 0;
    }
}