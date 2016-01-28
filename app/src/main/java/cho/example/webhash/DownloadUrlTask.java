package cho.example.webhash;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by zv0 on 27.01.16..
 */
public class DownloadUrlTask extends AsyncTask<String,Void,String> {

    Activity mActivity=null;
    Context mContext = null;
    URL mUri=null;
    String mStoredHash = null;
    public DownloadUrlTask(Context a,URL uri){
        mActivity = (Activity)a;
        mContext = a;
        mUri = uri;
        mStoredHash = checkIfUrlAlreadyStored(mUri);
    }
    @Override
    protected String doInBackground(String...str) {
        //checking if webpage of URL was previously hashed

        if (mStoredHash != "-1")
        {
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
        if (mStoredHash != "-1") {
            byte[] hashBytes = webPageDigest.getBytes(Charsets.UTF_8);
            byte firstByteOfHash = hashBytes[0];
                if (firstByteOfHash % 2 == 0) {
                    //save to preferences since its a new webpage and byte is even
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    sharedPreferences.edit().putString(mUri.toString(),webPageDigest)
                }
                else{
                    //save to database since byte is odd
                }

        }

    }

    protected String checkIfUrlAlreadyStored(URL uri) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String storedHash = null;
        if ((storedHash = sharedPreferences.getString(mUri.toString(), "-1")) == "-1") {
            //check database since no such url stored in preferences

            return "-1";
        } else {
            return storedHash;
        }
    }
}
