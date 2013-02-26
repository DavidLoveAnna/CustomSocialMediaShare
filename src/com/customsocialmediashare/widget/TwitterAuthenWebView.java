package com.customsocialmediashare.widget;

import android.content.Context;
import android.webkit.WebView;


public class TwitterAuthenWebView extends WebView{

	private String guthenticationURL;
	
	public TwitterAuthenWebView(Context context, String guthenticationURL){
		super(context);
		this.guthenticationURL = guthenticationURL;
	}
	public TwitterAuthenWebView(Context context) {
		super(context);

		this.setWebViewClient( new TwitterAuthenWebViewClient());
		this.loadUrl(guthenticationURL);
	}

	
}
