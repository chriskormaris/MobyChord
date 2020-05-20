package msc_aueb_gr_pol_liosis.mobychord;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by polyvios on 26/11/2016.
 */

//********************************* Dimitris Botonakis *********************************//
//********************************* Chris kormaris *************************************//
//********************************* Polyvios Liosis ************************************//

public class DBManager extends SQLiteOpenHelper {

    private final int m = ChordSize.m;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Chord.db";

    //Define db table Node
    private static final String TABLE_NAME_Node= "Node";
    private static final String COLUMN_NAME_NODE_ID = "id";
    private static final String COLUMN_NAME_NODE_IP = "ip";
    private static final String CREATE_TABLE_Node= "CREATE TABLE " + TABLE_NAME_Node+ " (" + COLUMN_NAME_NODE_ID + " text, " + COLUMN_NAME_NODE_IP + " text " + "); ";
    private static final String DROP_TABLE_Node = "DROP TABLE IF EXISTS" + TABLE_NAME_Node;

    public DBManager(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(CREATE_TABLE_Node);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        sqLiteDatabase.execSQL(DROP_TABLE_Node);
        onCreate(sqLiteDatabase);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        onUpgrade(db, oldVersion, newVersion);
    }

    //A new Nodeto the db table named Node
    public void addNodes()
    {
      //  Log.d("Database: " , "Inserting new Node");

      //  SQLiteDatabase db = this.getWritableDatabase();
      //  ContentValues contentValues = new ContentValues();
      //  contentValues.put(this.COLUMN_NAME_NODE_ID, node_id);
      //  contentValues.put(this.COLUMN_NAME_NODE_IP, node_ip);
      //  db.insert(TABLE_NAME_Node, null, contentValues);

      //   Log.d("Database: " , "New Nodeinserted to Chord Architecture (" + node_id + " , " + node_ip + " )");

      //  return true;


        //Clean possible info from previous connection on the ring of Chord Architecture
        this.eraseTableNode();

        for (int i = 0; i <(int) Math.pow(2,m) ; i++)
        {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(this.COLUMN_NAME_NODE_ID, String.valueOf(i));
            contentValues.put(this.COLUMN_NAME_NODE_IP, "0.0.0.0");
            db.insert(TABLE_NAME_Node, null, contentValues);
        }
    }

//    public String retrieveNodeIP(String node_id)
//    {
//        String node_ip = null;
//
//        return node_ip;
//    }

    //Update the IP of a specific Node
    public void updateNode(String node_id, String node_ip)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "UPDATE " + TABLE_NAME_Node+ " SET " + COLUMN_NAME_NODE_IP + " = '" + node_ip + "' WHERE " + COLUMN_NAME_NODE_ID + " = '" + node_id + "' ;";

        try
        {
            db.execSQL(query);

            Log.d("Database: " , "Node" + node_id + " updated: New ip ----> " + node_ip );
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void eraseTableNode()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from " + TABLE_NAME_Node);
    }
}
