package co.tapdatapp.tapandroid.service;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by arash on 9/28/14.
 */
public class TapTxn {
    private String mTagID;
    private float mAmount;
    private TapCloud mTapCloud;
    private int mSatoshi;
    private String mPayloadURL;
    private String mMessage;


    public String getPayloadURL(){
        return mPayloadURL;

    }
    public String getMessage(){
        return mMessage;
    }
    public int getSatoshi(){
        return mSatoshi;
    }


    public void TapAfool(String auth_token, String tag_id, float amount){
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        //END



        JSONObject json = new JSONObject();
        JSONObject output;
        try {
            json.put("auth_token", auth_token);
            json.put("tag_id", tag_id   );
            json.put("amount", amount);


            //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?
            output = mTapCloud.httpPost(TapCloud.TAP_TXN_API_ENDPOINT_URL + "auth_token=" + auth_token, json);
            mSatoshi = output.getJSONObject("response").getInt("satoshi");
            mPayloadURL = output.getJSONObject("response").getJSONObject("payload").getString("uri");
            mMessage = output.getJSONObject("response").getJSONObject("payload").getString("text");

//            /{"response":{"satoshi":936593,"payload":{"uri":"https:\/\/s3.amazonaws.com\/tapyapa\/new_key_needed","text":"Enter Your message here"}}}
            String b = "sdlfkjsdf";
//            mAuthToken = output.getJSONObject("response").getString("auth_token");
//            mNickName = output.getJSONObject("response").getString("nickname");

        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }
    }

}
