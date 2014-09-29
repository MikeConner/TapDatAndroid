package co.tapdatapp.tapandroid.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Random;

/**
 * Created by arash on 9/28/14.
 */
public class TapUser {
    private static final int PHONE_SECRET_SIZE = 16;
    private static final String TASKS_URL = "http://10.0.2.2:3000/api/v1/tasks.json";
    private final static String LOGIN_API_ENDPOINT_URL = "http://10.0.2.2:3000/api/v1/sessions.json";
    private final static String TAP_REGISTER_API_ENDPOINT_URL = "http://10.0.2.2:3000/mobile/1/registrations.json";
    private final static String TAP_LOGIN_API_ENDPOINT_URL = "http://10.0.2.2:3000/mobile/1/sessions.json";
    private final static String TAP_TAGS_API_ENDPOINT_URL = "http://10.0.2.2:3000/mobile/1/nfc_tags.json";
    private final static String TAP_USER_API_ENDPOINT_URL = "http://10.0.2.2:3000/mobile/1/users/me";
    private final static String TAP_USERNICK_API_ENDPOINT_URL = "http://10.0.2.2:3000/mobile/1/users/reset_nickname";
    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    private String mUserEmail;
    private String mNickName;
    private String mOutboundBTCaddress;
    private int mBalance;
    private String mPhoneSecret;
    private String mInboundBTCaddress;
    private String mAuthToken;
    private Map<String, String> mtagMap;
    private TapCloud mTapCloud;


    public String CreateUser (String phone_secret){
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();

        //END

        mPhoneSecret = phone_secret;
        JSONObject user = new JSONObject();
        JSONObject json = new JSONObject();
        JSONObject output;
        try {
            user.put("phone_secret_key", phone_secret);
            json.put("user", user);
            //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?
            output = mTapCloud.httpPost(TAP_REGISTER_API_ENDPOINT_URL, json);
            mAuthToken = output.getJSONObject("response").getString("auth_token");
            mNickName = output.getJSONObject("response").getString("nickname");
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }
        return mAuthToken;
    }

    public void LoadUser(String auth_token){
        mAuthToken = auth_token;
        String mURL = TAP_USER_API_ENDPOINT_URL + "?auth_token=" + mAuthToken;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        JSONObject output;
        try {
            output = mTapCloud.httpGet(mURL);
            mNickName = output.getJSONObject("response").getString("nickname");
            mInboundBTCaddress = output.getJSONObject("response").getString("inbound_btc_address");
            mOutboundBTCaddress = output.getJSONObject("response").getString("outbound_btc_address");
            mBalance = output.getJSONObject("response").getInt("satoshi_balance");
            mUserEmail = output.getJSONObject("response").getString("email");
        }
        catch (Exception e)
        {
            //TODO: any errors possible here?
        }

    }
    public void UpdateUser(){

    }

    public String getNickname(){

        return mNickName;
    }
    public void setNickName(String mNewNickname){
        mNickName = mNewNickname;
        this.UpdateUser();
    }
    public String getEmail(){
        return mUserEmail;
    }
    public void setEmail(String mNewEmail){
        mUserEmail = mNewEmail;
        this.UpdateUser();
    }
    public String getBTCinbound(){
        return mInboundBTCaddress;
    }
    public void setBTCinbound(String mNewBTCinBound){
        mInboundBTCaddress = mNewBTCinBound;
        this.UpdateUser();
    }
    public String getBTCoutbound(){
        return mOutboundBTCaddress;
    }
    public void setBTCoutbound(String mNewBTCoutbound){
        mOutboundBTCaddress = mNewBTCoutbound;
        this.UpdateUser();
    }


    public String generatePhoneSecret(){
        mPhoneSecret =  getRandomString(PHONE_SECRET_SIZE);
        return mPhoneSecret;
    }

    public static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder();
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
}
