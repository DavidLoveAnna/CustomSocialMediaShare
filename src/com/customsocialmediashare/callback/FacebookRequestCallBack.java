
package com.customsocialmediashare.callback;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;

public class FacebookRequestCallBack implements Request.Callback {

	private Context mContext;

	public FacebookRequestCallBack(Context context) {
		mContext = context;
	}

	@Override
	public void onCompleted(Response response) {
		JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
		String postId = null;
		try {
			postId = graphResponse.getString("id");
		}
		catch (JSONException e) {
		}
		FacebookRequestError error = response.getError();
		if (error != null) {
			Toast.makeText(mContext, error.getErrorMessage(), Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(mContext, postId, Toast.LENGTH_LONG).show();
		}

	}

}
