package com.example.krishna.gmailintegration.gmail;

/**
 * Created by krishna on 4/6/15.
 */
public interface GmailSyncListener {

    public static final int REQUEST_OK=1;
    public static final int REQUEST_CANCEL=2;
    public static final int REQUEST_MESSAGE=3;

    public void getResponse(int requestId, String response);
}
