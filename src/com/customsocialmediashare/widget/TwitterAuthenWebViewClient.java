
package com.customsocialmediashare.widget;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.customsocialmediashare.Const;

public class TwitterAuthenWebViewClient extends WebViewClient {

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		if (url.contains(Const.CALLBACK_URL)) {
			Uri uri = Uri.parse(url);
			String oauthVerifier = uri.getQueryParameter("oauth_verifier");
//			Intent mIntent = getIntent();
//			mIntent.putExtra("oauth_verifier", oauthVerifier);
//			setResult(RESULT_OK, mIntent);
//			finish();
			return true;
		}
		return false;
	}
}
