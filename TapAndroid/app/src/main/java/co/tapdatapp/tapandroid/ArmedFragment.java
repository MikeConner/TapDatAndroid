package co.tapdatapp.tapandroid;



import android.app.DialogFragment;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ArmedFragment extends DialogFragment {
    private String mAuthToken;
    private float mAmount;



    public ArmedFragment() {
        // Required empty public constructor
    }
    public ArmedFragment(String auth_token, float amount) {
            mAuthToken = auth_token;
            mAmount = amount;
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_armed, container, false);


    }
    @Override
    public void onResume(){
        super.onResume();
        TextView tv = (TextView)  getView().findViewById(R.id.txtFrag);
        tv.setText( " About to TAP with Auth_token " + mAuthToken + " to a stranger for $" + mAmount);
    }


}
