package co.tapdatapp.tapandroid.service;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by arash on 10/5/14.
 */
public class TapYapa {
    private String mURL;
    private String mContent;
    private int mThreshold;
    private String mYapaID;
    private TapCloud mTapCloud;
    private String mYapaURL;
    private String mThumbnailURL;
    private String mAuthToken;


    public void loadYapa(String auth_token, String yapa_id, String tag_id){
        String mAPIURL = TapCloud.TAP_ONE_YAPA_API_ENDPOINT_URL + yapa_id + ".json?auth_token=" + auth_token + "&tag_id=" + tag_id;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        JSONObject output;
        try {
            output = mTapCloud.httpGet(mAPIURL);
            mURL = output.getJSONObject("response").getString("uri");
            mContent = output.getJSONObject("response").getString("text");
            mThreshold = output.getJSONObject("response").getInt("threshold");
            mYapaURL = output.getJSONObject("response").getString("payload_image");
            mThumbnailURL = output.getJSONObject("response").getString("payload_thumb");


        }
        catch (Exception e)
        {
            //TODO: any errors possible here?
        }
    }

    public String getFullYapa(){
        return mYapaURL;
    }
    public void setFullYapa(String new_value){
        mYapaURL=new_value;
    }

    public String getThumbYapa(){
        return mThumbnailURL;
    }
    public void setThumbYapa(String new_value){
        mThumbnailURL=new_value;
    }



    public String getURL(){
        return mURL;
    }
    public void setURL(String new_value){
        mURL=new_value;
    }

    public String getContent(){
        return mContent;
    }
    public void setContent(String new_value){
        mContent=new_value;
    }
    public String getYapaID(){
        return mYapaID;
    }
    public void setYapaID(String new_value){
        mYapaID=new_value;
    }

    public int getThreshold(){
        return mThreshold;
    }
    public void setThreshold(int new_value){
        mThreshold=new_value;
    }

    public void updateYapa(String auth_token){
        mAuthToken = auth_token;

        JSONObject json = new JSONObject();
        JSONObject output;

        String mURL = TapCloud.TAP_ONE_YAPA_API_ENDPOINT_URL + "?auth_token=" + mAuthToken;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();

        try {

            json.put("auth_token", mAuthToken);
//            json.put("id", "0");



            json.put("id", mYapaID);
            json.put("tag_id", mYapaID);

            json.put("name", mThreshold);
            json.put("name", mContent);
            json.put("name", mURL);
            json.put("name", mYapaURL);

            json.put("name", mThumbnailURL);


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
}

