package co.tapdatapp.tapandroid.service;

/**
 * Created by arash on 10/5/14.
 */
public class TapYapa {
    private String mURL;
    private String mContent;
    private int mThreshold;
    private String mYapaID;

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

