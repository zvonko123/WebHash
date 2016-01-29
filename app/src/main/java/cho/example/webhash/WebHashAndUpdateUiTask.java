package cho.example.webhash;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.TextView;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zv0 on 27.01.16..
 */
public class WebHashAndUpdateUiTask extends AsyncTask<String,Void,String> {

    Activity mActivity=null;
    Context mContext = null;
    URL mUri=null;
    String mStoredHash = null;
    SQLiteDatabase mMyDatabase=null;
    public WebHashAndUpdateUiTask(Context a, URL uri){
        mActivity = (Activity)a;
        mContext = a;
        mUri = uri;
        mMyDatabase = mContext.openOrCreateDatabase("web_hashes",Context.MODE_PRIVATE,null);
        mMyDatabase.execSQL("CREATE TABLE IF NOT EXISTS hashes(uri VARCHAR,hash VARCHAR);");
        mStoredHash = checkIfUrlAlreadyStored(mUri);
    }
    @Override
    protected String doInBackground(String...str) {
        //checking if webpage of URL was previously hashed

        if (mStoredHash != "-1")
        {
            //Toast.makeText(mContext, mContext.getString(R.string.fetched_hash), Toast.LENGTH_SHORT).show();
            return mStoredHash;
        }
        //we dont strictly need else keyword here,since code is not reached after return
        else {
            Writer out = new StringWriter();
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) mUri.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                IOUtils.copy(in, out);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return Hashing.md5().hashString(out.toString(), Charsets.UTF_8).toString();
        }

    }

    @Override
    protected void onPostExecute(String webPageDigest){


        ((TextView) mActivity.findViewById(R.id.outputTextView)).setText(webPageDigest);
        //check mStoredHash member again to see if webpage was previously hashed
        //if not, save it
        //double checking of same thing?
        if (mStoredHash == "-1") {
            byte[] hashBytes = webPageDigest.getBytes(Charsets.UTF_8);
            byte firstByteOfHash = hashBytes[0];
                if ((firstByteOfHash % 2) == 0) {
                    //save to preferences since its a new webpage and byte is even
                    SharedPreferences sharedPreferences = mContext.getSharedPreferences("web_hashes",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(mUri.toString(),webPageDigest);
                    editor.commit();
                }
                else{
                    //save to database since byte is odd
                    mMyDatabase.execSQL("INSERT INTO hashes" +
                            "(uri,hash) VALUES ('" +mUri.toString()
                            +"','"+webPageDigest+"');");


                }
        }

    }

    protected String checkIfUrlAlreadyStored(URL uri) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("web_hashes", Context.MODE_PRIVATE);
        String storedHash = sharedPreferences.getString(uri.toString(), "-1");
        if (storedHash == "-1") {
            //check database since no such url stored in preferences

            Cursor resultSet = mMyDatabase.rawQuery("SELECT * FROM hashes where uri='"+mUri.toString()+"';",null);
            if (resultSet.moveToFirst() == false){
                return "-1";
            }
            else{
                storedHash = resultSet.getString(1);
                return storedHash;
            }
        } else {
            return storedHash;
        }
    }
}
