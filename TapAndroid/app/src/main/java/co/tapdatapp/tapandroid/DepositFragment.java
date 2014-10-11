package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import co.tapdatapp.tapandroid.service.TapCloud;
import co.tapdatapp.tapandroid.service.TapUser;


public class DepositFragment extends DialogFragment {

    private String mAuthToken;


    public  void setValues (String message, String payload_url){

        TextView tv = (TextView) getView().findViewById(R.id.txtYap);
        ImageView iv = (ImageView) getView().findViewById(R.id.imageYapa);
        tv.setText(message);
        iv.setImageDrawable(TapCloud.LoadImageFromWebOperations(payload_url));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

    }

    public DepositFragment() {
        // Required empty public constructor
    }
    public DepositFragment(String auth_token ) {
        mAuthToken = auth_token;

        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.fragment_deposit, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onResume(){
        super.onResume();
        TextView btcInbound = (TextView) getView().findViewById(R.id.txtInboundAddy);
        TapUser mTapUser = TapCloud.getTapUser(getActivity());
        String mBTCaddy = mTapUser.getBTCinbound();
        ImageView iv = (ImageView) getView().findViewById(R.id.imgQRCODE);
        iv.setImageDrawable(TapCloud.LoadImageFromWebOperations(mTapUser.getQR()));
        btcInbound.setText("Send ALL of your bitcoin to: " + mBTCaddy);
    }

}
