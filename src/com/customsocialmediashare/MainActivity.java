
package com.customsocialmediashare;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploadFactory;
import twitter4j.media.MediaProvider;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.customsocialmediashare.callback.FacebookRequestCallBack;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;

public class MainActivity extends Activity {

	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	private FacebookRequestCallBack mFacebookRequestCallBack;

	private Button mFbTriggerBtn, mTwitterTriggerBtn;

	private static final String twitter_consumer_key = "OHDdIQdzXO03D3xSKr3A";
	private static final String twitter_secret_key = "7esISt5WRlwuiOCAdEHOHWwB3b1vGYdwQf50tAk00";
	private static final String twitter_pic_api_key = "2837539ab7af95cd84e70faaa6ea705a";
	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
	static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

	private Twitter mTwitter;
	private static RequestToken mRequestToken;
	private static SharedPreferences mSharedPreferences;
	private static String mAccessToken;
	private static String mAccessTokenSecret;

	private int TWITTER_AUTH = 1002;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mSharedPreferences = getSharedPreferences(Const.PREFERENCE_NAME, MODE_PRIVATE);

		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
			}
		}

		// Facebook Trigger Button
		mFbTriggerBtn = (Button) findViewById(R.id.fb_trigger_btn);
		mFbTriggerBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				publishStory();
			}
		});

		updateView();
		// Twitter Trigger Button
		mTwitterTriggerBtn = (Button) findViewById(R.id.twitter_trigger_btn);
		mTwitterTriggerBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new MyAsycn().execute();
			}
		});
	}

	public class MyAsycn extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... arg0) {
			// Twitter mTwitter and RequestToken mRequestToken
			// are private members of this activity
			mTwitter = new TwitterFactory().getInstance();
			mTwitter.setOAuthConsumer(twitter_consumer_key, twitter_secret_key);
			mRequestToken = null;
			try {
				mRequestToken = mTwitter.getOAuthRequestToken(Const.CALLBACK_URL);
			}
			catch (TwitterException e) {
				e.printStackTrace();
			}
			Intent i = new Intent(MainActivity.this, TwitterWebViewActivity.class);
			i.putExtra("URL", mRequestToken.getAuthenticationURL());
			startActivityForResult(i, TWITTER_AUTH);
			return null;
		}

	}

	public class AccessMyAsycn extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... arg0) {
			AccessToken at = null;
			try {
				// Pair up our request with the response
				at = mTwitter.getOAuthAccessToken(mRequestToken, arg0[0]);
				mSharedPreferences.edit().putString("twitter_access_token", at.getToken())
						.putString("twitter_access_token_secret", at.getTokenSecret()).commit();

				String accessToken = mSharedPreferences.getString("twitter_access_token", null);
				String accessTokenSecret = mSharedPreferences.getString("twitter_access_token_secret", null);

				Log.d("Twitter", "twitter_access_token :" + accessToken);
				Log.d("Twitter", "twitter_access_token_secret :" + accessTokenSecret);

				// Post Status
//				Configuration conf = new ConfigurationBuilder().setOAuthConsumerKey(twitter_consumer_key)
//						.setOAuthConsumerSecret(twitter_secret_key).setOAuthAccessToken(accessToken)
//						.setOAuthAccessTokenSecret(accessTokenSecret).build();
//				Twitter t = new TwitterFactory(conf).getInstance();
//				t.updateStatus("@MokaSocial You guys rock!");
				
				// Post Photo
				Configuration conf = new ConfigurationBuilder()
			    .setMediaProviderAPIKey( null )
			    .setOAuthConsumerKey( twitter_consumer_key )
			    .setOAuthConsumerSecret( twitter_secret_key )
			    .setOAuthAccessToken(accessToken)
			    .setOAuthAccessTokenSecret(accessTokenSecret)
			    .build();
				
				ImageUpload upload = new ImageUploadFactory(conf).getInstance(MediaProvider.TWITTER);
				
				Bitmap bitmap = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.ic_launcher);

				byte[] data = null;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
				data = baos.toByteArray();

		        ByteArrayInputStream bis = new ByteArrayInputStream(data);
				

		        // Upload both Image and Messages
		        StatusUpdate status = new StatusUpdate("Greate Try");
		        status.setMedia("new", bis);
		        mTwitter.updateStatus(status);
		        
//				String url = upload.upload("New", bis); // Only Image Upload
//				Twitter t = new TwitterFactory(conf).getInstance();
//				t.updateStatus("@MokaSocial You guys rock!:::"+url);

			}
			catch (TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {
			String oauthVerifier = (String) data.getExtras().get("oauth_verifier");
			Log.d("Twitter", "twitter_access_token :" + oauthVerifier);
			new AccessMyAsycn().execute(oauthVerifier);
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	private void updateView() {
		Session session = Session.getActiveSession();
		// publishStory();
		if (session.isOpened()) {
			// publishStory();
			// mFbTriggerBtn.setText(R.string.logout);
			// mFbTriggerBtn.setOnClickListener(new OnClickListener() {
			//
			// public void onClick(View view) {
			// // onClickLogout();
			//
			// publishStory();
			// }
			// });
		}
		else {
			onClickLogin();
			// mFbTriggerBtn.setText(R.string.login);
			// mFbTriggerBtn.setOnClickListener(new OnClickListener() {
			//
			// public void onClick(View view) {
			// // onClickLogin();
			// publishStory();
			// }
			// });
		}
	}

	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
		}
		else {
			Session.openActiveSession(this, true, statusCallback);
		}
	}

	// private void onClickLogout() {
	// Session session = Session.getActiveSession();
	// if (!session.isClosed()) {
	// session.closeAndClearTokenInformation();
	// }
	// }

	private class SessionStatusCallback implements Session.StatusCallback {

		@Override
		public void call(Session session, SessionState state, Exception exception) {
			// updateView();
		}
	}

	private void publishStory() {
		Session session = Session.getActiveSession();

		if (session != null && session.isOpened()) {

			// Check for publish permissions
			List<String> permissions = session.getPermissions();
			if (!isSubsetOf(PERMISSIONS, permissions)) {
				pendingPublishReauthorization = true;
				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this,
						PERMISSIONS);
				session.requestNewPublishPermissions(newPermissionsRequest);
				return;
			}

			// path = "Path OF YOUR IMAGE";
			Bundle postParams = new Bundle();
			postParams.putString("message", "MESSAGE YOU WANT TO POST");
			// File file = new File(path, "IMAGE_NAME.jpg");
			// Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
			Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher);

			byte[] data = null;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
			data = baos.toByteArray();
			if (data != null) {
				postParams.putByteArray("picture", data);
			}

			mFacebookRequestCallBack = new FacebookRequestCallBack(MainActivity.this);
			Request request = new Request(session, "me/photos", postParams, HttpMethod.POST, mFacebookRequestCallBack);
			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}
		else {
			onClickLogin();
		}

	}

	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}

}
