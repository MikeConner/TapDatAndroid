package co.tapdat.tapdatapp.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by arash on 9/23/14.
 */
public class TapUser {

    private String mUserEmail;
    private String mNickName;
    private String mOutboundBTCaddress;
    private int mBalance;
    private String mPhoneSecret;
    private String mInboundBTCaddress;
    private String mAuthToken;


    // something to hold a picture

    private SharedPreferences mPreferences;

    private static final String TASKS_URL = "http://10.0.2.2:3000/api/v1/tasks.json";
    private final static String LOGIN_API_ENDPOINT_URL = "http://10.0.2.2:3000/api/v1/sessions.json";
    private final static String TAP_REGISTER_API_ENDPOINT_URL = "http://10.0.2.2:3000/mobile/1/registrations.json";
    private final static String TAP_LOGIN_API_ENDPOINT_URL = "http://10.0.2.2:3000/mobile/1/sessions.json";
    private final static String TAP_USER_API_ENDPOINT_URL = "http://10.0.2.2:3000/mobile/1/users/me";
    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

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

    public static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder();
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    public  boolean GeneratePhoneSecret(Context mContext){
        mPreferences = mContext.getSharedPreferences("CurrentUser", mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        mPhoneSecret = getRandomString(16);
        editor.putString("PhoneSecret", mPhoneSecret);
        editor.commit();
        return true;
        // DO a check to make sure Unique (Web Call, generate new token)

    }
    public String CreateUser(Context mContext){

        mPreferences = mContext.getSharedPreferences("CurrentUser", mContext.MODE_PRIVATE);
        RegisterTask registerTask = new RegisterTask(mContext);
        registerTask.setMessageLoading("Registering new account...");
        registerTask.execute(TAP_REGISTER_API_ENDPOINT_URL);
        return "Success";
    }

    public boolean   GetAuthToken(String mPhoneSecret, Context mContext){
        LoginTask loginTask = new LoginTask(mContext);
        loginTask.setMessageLoading("Logging in...");
        loginTask.execute(LOGIN_API_ENDPOINT_URL);
        //    Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
        //   startActivityForResult(intent, 0);

        return true;

    }



    public boolean LoadUser(Context mContext, String mAuthToken){
        mPreferences = mContext.getSharedPreferences("CurrentUser", mContext.MODE_PRIVATE);
        GetUserInfo mUserInfo = new GetUserInfo(mContext);
        mUserInfo.setMessageLoading("Loading User...");
        mUserInfo.execute(TAP_USER_API_ENDPOINT_URL + "?auth_token=" + mAuthToken);
        return true;
    }


    public boolean UpdateUser(){

        return true;
    }

    public String NewNetNickName(){
        return "soggy banana";
    }



    private class LoginTask extends UrlJsonAsyncTask {
        public LoginTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject userObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            try {
                try {
                    // setup the returned values in case
                    // something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");
                    // add the user email and password to
                    // the params
                    userObj.put("email", mUserEmail);
                    userObj.put("password", "bob");
                    holder.put("user", userObj);
                    StringEntity se = new StringEntity(holder.toString());
                    post.setEntity(se);

                    // setup the request headers
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute( post, responseHandler);
                    json = new JSONObject(response);

                } catch (HttpResponseException e) {
                    e.printStackTrace();
                    Log.e("ClientProtocol", "" + e);
                    json.put("info", "Email and/or password are invalid. Retry!");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO", "" + e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON", "" + e);
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getBoolean("success")) {
                    // everything is ok
                    SharedPreferences.Editor editor = mPreferences.edit();
                    // save the returned auth_token into
                    // the SharedPreferences
                    editor.putString("AuthToken", json.getJSONObject("data").getString("auth_token"));
                    editor.commit();


                }
                Toast.makeText(context, json.getString("info"), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                // something went wrong: show a Toast
                // with the exception message
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }

    private class RegisterTask extends UrlJsonAsyncTask {
        public RegisterTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject userObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            try {
                try {
                    // setup the returned values in case
                    // something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");

                    // add the users's info to the post params
                    userObj.put("phone_secret_key", mPhoneSecret);
                    holder.put("user", userObj);
                    StringEntity se = new StringEntity(holder.toString());
                    post.setEntity(se);

                    // setup the request headers
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(post, responseHandler);
                    json = new JSONObject(response);

                } catch (HttpResponseException e) {
                    e.printStackTrace();
                    Log.e("ClientProtocol", "" + e);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO", "" + e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON", "" + e);
            }


            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (!json.getString("response").isEmpty()) {
                    // everything is ok
                    SharedPreferences.Editor editor = mPreferences.edit();
                    // save the returned auth_token into
                    // the SharedPreferences
                    editor.putString("AuthToken", json.getJSONObject("response").getString("auth_token"));
                    editor.putString("NickName", json.getJSONObject("response").getString("nickname"));
                    editor.commit();

                    // launch the HomeActivity and close this one

                }
                //Toast.makeText(context, json.getString("info"), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                // something went wrong: show a Toast
                // with the exception message
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }







    private class GetUserInfo extends UrlJsonAsyncTask {
        public GetUserInfo(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                mNickName = json.getJSONObject("response").getString("nickname");
                mInboundBTCaddress = json.getJSONObject("response").getString("inbound_btc_address");
                mOutboundBTCaddress = json.getJSONObject("response").getString("outbound_btc_address");
                mBalance = json.getJSONObject("response").getInt("satoshi_balance");
                mUserEmail = json.getJSONObject("response").getString("email");
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }

}
