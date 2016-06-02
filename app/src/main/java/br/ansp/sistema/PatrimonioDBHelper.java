/**
 * 
 */
package br.ansp.sistema;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Antonio
 *
 */
public class PatrimonioDBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "sistema.patrimonio.db";
	private static final int DATABASE_VERSION = 3;
	public static final String TABLE1_NAME = "rack";
	public static final String TABLE2_NAME = "patrimonio";
	public static final String ID = "id";
	public static final String RACK_ID = "rack_id";
	public static final String APELIDO = "apelido";
	public static final String POSICAO = "posicao";
	
	public PatrimonioDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table " + TABLE1_NAME + " (" + ID + " integer, " + RACK_ID + " integer, " + POSICAO + " varchar(20), " + APELIDO + " varchar(40));");
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(PatrimonioDBHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
        + newVersion + ", which will destroy all old data");
       db.execSQL("DROP TABLE IF EXISTS " + TABLE1_NAME);
       onCreate(db);
    }
	
}
