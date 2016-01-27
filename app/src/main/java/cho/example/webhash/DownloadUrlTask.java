package cho.example.webhash;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.TextView;

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
public class DownloadUrlTask extends AsyncTask<URL,Void,String> {

    Activity mActivity=null;
    public DownloadUrlTask(Context a){
        mActivity = (Activity)a;
    }
    @Override
    protected String doInBackground(URL... uri) {

        Writer out = new StringWriter();
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) uri[0].openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                IOUtils.copy(in, out);
            } catch (IOException e) {
            e.printStackTrace();
            }
        return out.toString();


    }

    @Override
    protected void onPostExecute(String webPage){

        MessageDigest md;
        byte[] webPageDigest=null;
        try {
            md = MessageDigest.getInstance("MD5");
            webPageDigest = md.digest(webPage.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ((TextView) mActivity.findViewById(R.id.outputTextView)).setText(webPageDigest.toString());

    }

}
