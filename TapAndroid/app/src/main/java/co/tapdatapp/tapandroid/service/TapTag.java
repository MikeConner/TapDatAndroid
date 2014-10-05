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
   // private final static String TAP_TAGS_API_ENDPOINT_URL = "http://192.168.1.135:3000/mobile/1/nfc_tags.json";
  //  private final static String TAP_TAG_API_ENDPOINT_URL = "http://192.168.1.135:3000/mobile/1/nfc_tags/0.json";



    public void updateTag(String auth_token, String tag_id, String new_name){
        mAuthToken = auth_token;

        JSONObject json = new JSONObject();
        JSONObject output;

        String mURL = TapCloud.TAP_TAG_API_ENDPOINT_URL + "?auth_token=" + mAuthToken;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();

        try {

            json.put("auth_token", mAuthToken);
            json.put("id", "0");
            json.put("name", new_name);
            json.put("tag_id", tag_id);

            //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?
            output = mTapCloud.httpPut(mURL, json);
            // = output.getJSONObject("response").getString("nickname");
            //CHECK FOR BAD CASES HERE!
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }

    }


    //TODO: this does not work currently! Not really needed since we're passing everything in manually to the view
    public void loadTag(String auth_token, String tag_id){
        mAuthToken=auth_token;
        mTagID = tag_id;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        //END


        String mURL = TapCloud.TAP_TAGS_API_ENDPOINT_URL + "?auth_token=" + mAuthToken;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        JSONObject output;
        try {
            output = mTapCloud.httpGet(mURL);
                //TODO: get individual tag load here? (with count etc?) + any yapa it may have?
//            mTagName = output.getJSONObject("response").getString("name");
//            mInboundBTCaddress = output.getJSONObject("response").getString("inbound_btc_address");
//            mOutboundBTCaddress = output.getJSONObject("response").getString("outbound_btc_address");
//            mBalance = output.getJSONObject("response").getInt("satoshi_balance");
//            mUserEmail = output.getJSONObject("response").getString("email");
        }
        catch (Exception e)
        {
            //TODO: any errors possible here?
        }






    }



    public String generateNewTag(String auth_token){
        mAuthToken = auth_token;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        //END

        JSONObject tag   = new JSONObject();

        JSONObject output;
        try {
            tag.put("auth_token", mAuthToken);
            //json.put("user", user);
            //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?

            //TODO: Update this to session controller instead of registration controller
            output = mTapCloud.httpPost(TapCloud.TAP_TAGS_API_ENDPOINT_URL, tag);
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
