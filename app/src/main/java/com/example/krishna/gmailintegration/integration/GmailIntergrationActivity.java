package com.example.krishna.gmailintegration.integration;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.krishna.gmailintegration.R;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class GmailIntergrationActivity extends ActionBarActivity implements EmailListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_email);
		initialize();

		btnSendMail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendEmail();
			}
		});

		btnInbox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String fromMail="dev.krishna@appsforbb.net";
				String password="krishna1654";
				Mail m = new Mail(fromMail, password);
				m.getInboxCount(GmailIntergrationActivity.this);
			}
		});
	}

	private EditText etFromMail, etFromPassword, etSubject, etMessage, etAttachment, etToAddress;
	private Button btnSendMail;

	public void initialize(){
		etFromMail=(EditText) findViewById(R.id.et_username);
		etFromPassword=(EditText) findViewById(R.id.et_password);
		etSubject=(EditText) findViewById(R.id.et_subject);
		etMessage=(EditText) findViewById(R.id.et_message);
		etAttachment=(EditText) findViewById(R.id.et_attachment);
		etToAddress=(EditText) findViewById(R.id.et_to_address);
		btnSendMail=(Button) findViewById(R.id.send_email);		
		btnInbox = (Button) findViewById(R.id.btn_get_inbox);

		etFromMail.setText("dev.krishna@appsforbb.net");
		etSubject.setText("Email Subject");
		etMessage.setText("Email Message");
		etToAddress.setText("pvkrishnavasamsetti@gmail.com");
		etAttachment.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				showFileChooser();
				return false;
			}
		});
	}

	public void sendEmail(){
		String fromMail=etFromMail.getText().toString().trim();
		String password=etFromPassword.getText().toString().trim();
		String subject=etSubject.getText().toString().trim();
		String message=etMessage.getText().toString().trim();
		String attachment=etAttachment.getText().toString().trim();
		String toAddress=etToAddress.getText().toString().trim();

		Mail m = new Mail(fromMail, password);

		String[] toArr = { toAddress };
		m.set_to(toArr);
		m.set_from(fromMail);
		m.set_subject(subject);
		m.setBody(message);

		if(attachment.length()>0){
			File file=new File(attachment);
			if(!file.exists()){
				etAttachment.setError("Invalid filepath");
				return;
			}
			try {
				m.addAttachment(attachment);
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("Email", "A:"+e.getMessage());
				Toast.makeText(GmailIntergrationActivity.this, "Error in attached file.",
						Toast.LENGTH_LONG).show();
			}
		}

		try {
			m.send(this);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("Email", "B:"+e.getMessage());
			Toast.makeText(GmailIntergrationActivity.this, "Error in sending mail",
					Toast.LENGTH_LONG).show();
		}
	}

	private static final int FILE_SELECT_CODE = 0;
	private static final String TAG = GmailIntergrationActivity.class.getSimpleName();
	private Button btnInbox;

	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
		intent.setType("*/*"); 
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		try {
			startActivityForResult(
					Intent.createChooser(intent, "Select a File to Upload"),
					FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			// Potentially direct the user to the Market with a Dialog
			Toast.makeText(this, "Please install a File Manager.", 
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_SELECT_CODE:
			if (resultCode == RESULT_OK) {
				// Get the Uri of the selected file 
				Uri uri = data.getData();
				Log.d(TAG, "File Uri: " + uri.toString());
				// Get the path
				String path;
				try {
					path = getPath(this, uri);
					etAttachment.setText(path);
					Log.d(TAG, "File Path: " + path);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.d("Email", "C:"+e.getMessage());
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static String getPath(Context context, Uri uri) throws URISyntaxException {
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;

			try {
				cursor = context.getContentResolver().query(uri, projection, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {
				// Eat it
			}
		}
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	} 

	@Override
	public void sendedSuccessfull() {
		Toast.makeText(GmailIntergrationActivity.this, "Email sended successfully...",
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void emailFailed() {
		Toast.makeText(GmailIntergrationActivity.this, "Email failed",
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void getInboxMessages(ArrayList<MyInbox> msgs) {
		Toast.makeText(GmailIntergrationActivity.this, "Email inbox count:"+msgs.size(), Toast.LENGTH_LONG).show();

		for(MyInbox msg: msgs){
			String sub=msg.getSubject();

			Log.d("Email", "Msg: "+sub);
		}
	}

}
interface EmailListener{
	void sendedSuccessfull();
	void emailFailed();
	void getInboxMessages(ArrayList<MyInbox> msgs);
}
