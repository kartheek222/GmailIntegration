package com.example.krishna.gmailintegration.integration;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Mail extends javax.mail.Authenticator {
	private String _user;
	private String _pass;

	private String[] _to;
	private String _from;

	private String _port;
	private String _sport;

	private String _host;

	private String _subject;
	private String _body;

	private boolean _auth;

	private boolean _debuggable;

	private Multipart _multipart;

	public Mail() {
		_host = "smtp.gmail.com"; // default smtp server
		_port = "465"; // default smtp port
		_sport = "465"; // default socketfactory port

		_user = ""; // username
		_pass = ""; // password
		_from = ""; // email sent from
		_subject = ""; // email subject
		_body = ""; // email body

		_debuggable = false; // debug mode on or off - default off
		_auth = true; // smtp authentication - default on

		_multipart = new MimeMultipart();

		// There is something wrong with MailCap, javamail can not find a
		// handler for the multipart/mixed part, so this bit needs to be added.
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap
				.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);
	}

	public Mail(String user, String pass) {
		this();

		_user = user;
		_pass = pass;
	}

	public boolean send(EmailListener listener) throws Exception {
		Properties props = _setProperties();

		if (!_user.equals("") && !_pass.equals("") && _to.length > 0
				&& !_from.equals("") && !_subject.equals("")
				&& !_body.equals("")) {
			Session session = Session.getInstance(props, this);

			MimeMessage msg = new MimeMessage(session);

			msg.setFrom(new InternetAddress(_from));

			InternetAddress[] addressTo = new InternetAddress[_to.length];
			for (int i = 0; i < _to.length; i++) {
				addressTo[i] = new InternetAddress(_to[i]);
			}
			msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);

			msg.setSubject(_subject);
			msg.setSentDate(new Date());

			// setup message body
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(_body);
			_multipart.addBodyPart(messageBodyPart);

			// Put parts in message
			msg.setContent(_multipart);

			//			Transport.send(msg);
			sendEmail(msg, listener);

			return true;
		} else {
			return false;
		}
	}//EmailListener

	public void sendEmail(final MimeMessage msg, EmailListener listener){
		new RetrieveFeedTask(listener).execute(msg);
	}

	class RetrieveFeedTask extends AsyncTask<MimeMessage, Void, Boolean> {

		private EmailListener listener;

		public RetrieveFeedTask(EmailListener listener) {
			this.listener = listener;		}

		protected Boolean doInBackground(MimeMessage... msgs) {
			// send email
			Log.d("Email", "email sending started...");
			try {
				Transport.send(msgs[0]);
				Log.d("Email", "sended email");
				return true;
			} catch (MessagingException e) {
				e.printStackTrace();
				Log.d("Email", "Error in sending email"+e);
				return false;
			}
		}

		protected void onPostExecute(Boolean isSended) {
			if(isSended){
				Log.d("Email", "Successfully email sent.");
				listener.sendedSuccessfull();
			}else{
				Log.d("Email", "Email sending failed.");
				listener.emailFailed();
			}
		}
	}

	public void addAttachment(String filename) throws Exception {
		BodyPart messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(filename);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(filename);

		_multipart.addBodyPart(messageBodyPart);
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(_user, _pass);
	}

	private Properties _setProperties() {
		Properties props = new Properties();

		props.put("mail.smtp.host", _host);

		if (_debuggable) {
			props.put("mail.debug", "true");
		}

		if (_auth) {
			props.put("mail.smtp.auth", "true");
		}

		props.put("mail.smtp.port", _port);
		props.put("mail.smtp.socketFactory.port", _sport);
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");

		return props;
	}


	class RetrieveInboxTask extends AsyncTask<Void, Void, ArrayList<MyInbox>> {

		private EmailListener listener;

		public RetrieveInboxTask(EmailListener listener) {
			this.listener = listener;		}

		protected ArrayList<MyInbox> doInBackground(Void... msgs) {
			Properties props = new Properties();
			//IMAPS protocol
			props.setProperty("mail.store.protocol", "imaps");
			//Set host address
			props.setProperty("mail.imaps.host", "imap.gmail.com");
			//Set specified port
			props.setProperty("mail.imaps.port", "993");
			//Using SSL
			props.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.setProperty("mail.imaps.socketFactory.fallback", "false");

			try{
				//Setting IMAP session
				Session imapSession = Session.getInstance(props);

				Store store = imapSession.getStore("imaps");
				//Connect to server by sending username and password.
				//Example mailServer = imap.gmail.com, username = abc, password = abc

				store.connect("imap.gmail.com", _user, _pass);
				//Get all mails in Inbox Forlder
				Folder inbox = store.getFolder("Inbox");
				inbox.open(Folder.READ_ONLY);
				
				/***/
//				FlagTerm ft      = new FlagTerm(new Flags(Flags.Flag.SEEN), false)        ;
//		        Message[] result   = inbox.search(ft);
		        /***/
		        		
		        
				//Return result to array of message
		        /***/
				Message[] result = inbox.getMessages();
		        /***/
				
				/***/
				//Message[] result = inbox.getMessages(10, 14);
		        /***/
				
				int count=inbox.getMessageCount();
				
				Log.d("MsgCount", "Count:"+count);
				
				ArrayList<MyInbox> myinbox=new ArrayList<MyInbox>();
				
				for(Message msg: result){
					try {
						String sub=msg.getSubject();
						String desc=msg.getDescription();
						
						Log.d("Email", "Msg: "+sub+"     Desc:"+desc);
						MyInbox bean=new MyInbox();
						bean.setSubject(sub);
						bean.setDesc(desc);
						myinbox.add(bean);
					} catch (MessagingException e) {
						e.printStackTrace();
					}
				}
				
				return myinbox;		
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(ArrayList<MyInbox> inboxMsgs) {
			if(inboxMsgs!=null){
				Log.d("Email", "Email Inbox count:"+inboxMsgs.size());
				listener.getInboxMessages(inboxMsgs);
			}else{
				Log.d("Email", "Email inbox getting failed.");
				listener.emailFailed();
			}
		}
	}

	public void getInboxCount(EmailListener listener){
		new RetrieveInboxTask(listener).execute();
	}
	
	// the getters and setters
	public String getBody() {
		return _body;
	}

	public void setBody(String _body) {
		this._body = _body;
	}

	public String[] get_to() {
		return _to;
	}

	public void set_to(String[] _to) {
		this._to = _to;
	}

	public void set_from(String _from) {
		this._from = _from;
	}

	public String get_from() {
		return _from;
	}

	public void set_subject(String _subject) {
		this._subject = _subject;
	}

	public String get_subject() {
		return _subject;
	}
}