package com.example.krishna.gmailintegration.gmail;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by krishna on 4/6/15.
 */
public class GetAccDetailsTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private String email;
    private String scope;
    private GmailSyncListener listener;

    public GetAccDetailsTask(Context context, String email, String scope, GmailSyncListener listener){
        this.context = context;
        this.email = email;
        this.scope = scope;
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            listener.getResponse(GmailSyncListener.REQUEST_MESSAGE, "Started process");
            fetchNameFromProfileServer();
        } catch (IOException ex) {
            onError("Following Error occured, please try again. "+ ex.getMessage(), ex);
        } catch (JSONException e) {
            onError("Bad response: " + e.getMessage(), e);
        }

        return null;
    }

    private void fetchNameFromProfileServer() throws IOException, JSONException {
        String token = fetchToken();
        URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token="+ token);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        int sc = con.getResponseCode();
        if (sc == 200) {
            InputStream is = con.getInputStream();
            String GOOGLE_USER_DATA = readResponse(is);
            is.close();

            Log.d("GetAccDetailsTask", "fetchNameFromProfileServer (Line:57) :"+GOOGLE_USER_DATA);
            listener.getResponse(GmailSyncListener.REQUEST_OK, GOOGLE_USER_DATA);
            return;
        } else if (sc == 401) {
            GoogleAuthUtil.invalidateToken(context, token);
            onError("Server auth error, please try again.", null);
            return;
        } else {
            onError("Server returned the following error code: " + sc, null);
            return;
        }
    }

    private String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken(context, email, scope);
        } catch (GooglePlayServicesAvailabilityException playEx) {
            // GooglePlayServices.apk is either old, disabled, or not present.
        } catch (UserRecoverableAuthException userRecoverableException) {
            // Unable to authenticate, but the user can fix this.
            // Forward the user to the appropriate activity.

            ((Activity)context).startActivityForResult(userRecoverableException.getIntent(), 100);
        } catch (GoogleAuthException fatalException) {
            onError("Unrecoverable error " + fatalException.getMessage(), fatalException);
        }
        return null;
    }

    private void onError(String msg, Exception ex) {
            listener.getResponse(GmailSyncListener.REQUEST_CANCEL, msg);
    }

    /**
     * Reads the response from the input stream and returns it as a string.
     */
    private static String readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while ((len = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, len);
        }
        return new String(bos.toByteArray(), "UTF-8");
    }
}
