package com.example.krishna.gmailintegration.gmail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.krishna.gmailintegration.R;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


public class GmailAccount extends ActionBarActivity implements GmailSyncListener {

    private AccountManager mAccountManager;
    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    private RadioGroup rgaccounts;
    private TextView tvname;
    private TextView tvgivenname;
    private TextView tvfamilyname;
    private TextView tvgender;
    private TextView tvlocale;
    private TextView tvbirthday;
    private android.widget.ImageView ivprofilepic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmail_account);
        this.ivprofilepic = (ImageView) findViewById(R.id.iv_profile_pic);
        this.tvbirthday = (TextView) findViewById(R.id.tv_birthday);
        this.tvlocale = (TextView) findViewById(R.id.tv_locale);
        this.tvgender = (TextView) findViewById(R.id.tv_gender);
        this.tvfamilyname = (TextView) findViewById(R.id.tv_family_name);
        this.tvgivenname = (TextView) findViewById(R.id.tv_given_name);
        this.tvname = (TextView) findViewById(R.id.tv_name);
        this.rgaccounts = (RadioGroup) findViewById(R.id.rg_accounts);

        final RadioGroup rgAccounts = (RadioGroup) findViewById(R.id.rg_accounts);
        final Button btnSync = (Button) findViewById(R.id.btn_sync);

        final String[] accounts = getAccountNames();
        int accId = 0;

        for (String acc : accounts) {
            RadioButton rbAcc = new RadioButton(this);
            rbAcc.setText(acc);
            rbAcc.setId(accId++);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rbAcc.setLayoutParams(params);
            rgAccounts.addView(rbAcc);
        }

        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rbId = rgAccounts.getCheckedRadioButtonId();
                if (rbId == -1) {
                    Toast.makeText(getApplicationContext(), "Please select email to get details", Toast.LENGTH_SHORT).show();
                } else {
                    if (isNetworkAvailable()) {
                        String email = accounts[rbId];
                        Toast.makeText(getApplicationContext(), "getting details from " + email, Toast.LENGTH_SHORT).show();
                        GetAccDetailsTask task = new GetAccDetailsTask(GmailAccount.this, email, SCOPE, GmailAccount.this);
                        task.execute();
                    } else {
                        Toast.makeText(getApplicationContext(), "No Network Service!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private String[] getAccountNames() {
        mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager
                .getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }

    public boolean isNetworkAvailable() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.e("Network Testing", "***Available***");
            return true;
        }
        Log.e("Network Testing", "***Not Available***");
        return false;
    }

    @Override
    public void getResponse(final int requestId, final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (requestId == GmailSyncListener.REQUEST_OK) {
                    AccDetails details = new AccDetails(response);
                    tvname.setText(details.getName());
                    tvgivenname.setText(details.getGivenName());
                    tvfamilyname.setText(details.getFamilyName());
                    tvgender.setText(details.getGender());
                    tvlocale.setText(details.getLocale());
                    tvbirthday.setText(details.getBirthday());
                    new GetImageFromUrl().execute(details.getPicture());
                } else if (requestId == GmailSyncListener.REQUEST_MESSAGE) {
                    Toast.makeText(getApplicationContext(), "Response:" + response, Toast.LENGTH_SHORT).show();
                } else if (requestId == GmailSyncListener.REQUEST_CANCEL) {
                    tvname.setText(response);
                    Toast.makeText(getApplicationContext(), "Response:" + response, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class GetImageFromUrl extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap map = null;
            for (String url : urls) {
                map = downloadImage(url);
            }
            return map;
        }

        // Sets the Bitmap returned by doInBackground
        @Override
        protected void onPostExecute(Bitmap result) {
            ivprofilepic.setImageBitmap(result);
        }

        // Creates Bitmap from InputStream and returns it
        private Bitmap downloadImage(String url) {
            Bitmap bitmap = null;
            InputStream stream = null;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1;

            try {
                stream = getHttpConnection(url);
                bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
                stream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return bitmap;
        }

        // Makes HttpURLConnection and returns InputStream
        private InputStream getHttpConnection(String urlString)
                throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return stream;
        }
    }
}
