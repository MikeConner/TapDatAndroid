package co.tapdatapp.tapandroid.service;

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

        }
        catch (Exception e)
        {
            //TODO: any errors possible here?
        }
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


}

