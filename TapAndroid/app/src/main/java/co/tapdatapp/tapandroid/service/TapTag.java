package co.tapdatapp.tapandroid.service;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by arash on 9/28/14.
 */
public class TapTag {
    private String mTagID;
    private String mTagName;
    private String mAuthToken;
    private TapCloud mTapCloud;
    private final static String TAP_TAGS_API_ENDPOINT_URL = "http://10.0.2.2:3000/mobile/1/nfc_tags.json";

    public String generateNewTag(String auth_token){
        mAuthToken = auth_token;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        //END

        JSONObject user = new JSONObject();
        JSONObject json = new JSONObject();
        JSONObject output;
        try {
            user.put("auth_token", mAuthToken);
            //json.put("user", user);
            //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?

            //TODO: Update this to session controller instead of registration controller
            output = mTapCloud.httpPost(TAP_TAGS_API_ENDPOINT_URL, user);
            mTagID = output.getJSONObject("response").getString("id");
            mTagName = output.getJSONObject("response").getString("name");
        //    Log.e(output.toString(), "" );
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }

        return mTagID;
    }

}
