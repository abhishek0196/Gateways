package com.gateways.gateways;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
//import android.widget.Toast;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.developer.kalert.KAlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerEvent extends AppCompatActivity implements ZXingScannerView.ResultHandler,FragmentToActivity{
    public ZXingScannerView mScannerView;
    private Vibrator myVib;
    String role,event,submittedBy;
    FragmentManager manager;
    PeopleListEvent addBottomDialogFragment;
    public  ActivityToFragment mCallback = null;

    ListView listView;
    ArrayList<String> list;
    ArrayAdapter<String > adapter;
    String teamId,participantId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_event);
        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.actionbar, null);
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if(bd != null)
        {
            role = (String) bd.get("role");
            event = (String) bd.get("event");
            submittedBy = bd.getString("email");

        }

        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        mScannerView = findViewById(R.id.zxscan);
        permission();
        AndroidNetworking.initialize(getApplicationContext());

        mScannerView.setResultHandler(this);


        mScannerView.startCamera();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "granteddd", Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(this, "granteddd not", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        scan(this.zXingScannerView);
////        Toast.makeText(this, "Resumed", Toast.LENGTH_SHORT).show();
        scan(this.mScannerView);

    }

    public void communicate(String comm)
    {
        onResume();
////        Toast.makeText(this, "comm->>>>>>>>>>>>>>>>>>>> "+comm, Toast.LENGTH_SHORT).show();
        if(comm.equals("End"))
        {
//            Toast.makeText(this, "close", Toast.LENGTH_SHORT).show();
            mCallback = null;
        }
        else
        {
//            Toast.makeText(this, "add", Toast.LENGTH_SHORT).show();
            mCallback = (PeopleListEvent)addBottomDialogFragment;
        }

        //super.onResume();
        //scan(this.mScannerView);
    }



    public void scan(View view){
        //mScannerView = findViewById(R.id.zxscan);
//        zXingScannerView =new ZXingScannerView(getApplicationContext());
        //setContentView(mScannerView);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();

    }

    @Override
    protected void onPause() {
        super.onPause();
//        zXingScannerView.stopCamera();
        mScannerView.stopCamera();
    }
    KAlertDialog pDialog1;
    @Override
    public void handleResult(Result result) {
        String qrResult = result.getText();
        myVib.vibrate(50);
        try {

            String split[] = qrResult.split("_");
            Log.v("dataa",split[0]);
             teamId = split[0];
             participantId = split[1];
             Log.v("dataa",teamId+"---"+participantId);
            pDialog1 = new KAlertDialog(ScannerEvent.this, KAlertDialog.PROGRESS_TYPE);
            pDialog1.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog1.setTitleText("Loading");
            pDialog1.setCancelable(false);
            pDialog1.show();
           checkOnSpotRegistration(participantId);



        }
        catch (Exception e)
        {
            KAlertDialog pDialog;
            pDialog = new KAlertDialog(ScannerEvent.this, KAlertDialog.ERROR_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Invalid QR!");
            pDialog.setCancelable(false);
            pDialog.show();
            Log.v("dataa",e.toString());
//            Toast.makeText(this, "Not A Valid QR \n Scan Again  ", Toast.LENGTH_SHORT).show();
            onResume();
        }

    }

    private void checkOnSpotRegistration(final String participantId) {
        String query = "select pid from gateways.attendance where pid ="+participantId+";";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "select");
            jsonObject.put("query", query);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = getResources().getString(R.string.server_url);

        AndroidNetworking.post(url+"/get")
//        AndroidNetworking.post("http://Gateways-env.d9kekdzq4q.ap-south-1.elasticbeanstalk.com/get")
                .addJSONObjectBody(jsonObject) // posting json
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        pDialog1.hide();
                        JSONArray jsonarray = null;

//                        Toast.makeText(ScannerEvent.this, "aaya"+response, Toast.LENGTH_SHORT).show();
                        try {
                            jsonarray = new JSONArray(response);
                            if (jsonarray.length() == 0) {

//                                Toast.makeText(ScannerEvent.this, "nhi hoga", Toast.LENGTH_SHORT).show();
                                KAlertDialog pDialog = new KAlertDialog(ScannerEvent.this, KAlertDialog.ERROR_TYPE);
                                pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                                pDialog.setTitleText("Registration Is Incomplete!");
                                pDialog.setCancelable(false);
                                pDialog.show();
                                onResume();
                            }
                            else {
                                manager = getSupportFragmentManager();
                                FragmentTransaction transaction = manager.beginTransaction();
                                Bundle bundle = new Bundle();
                                bundle.putString("role", role);
                                bundle.putString("event", event);
                                bundle.putString("teamId", teamId);
                                bundle.putString("participantId", participantId);
                                bundle.putString("submittedBy", submittedBy);


                                if(mCallback!=null) {
                                    mCallback.communicate(participantId);
                                }
                                else {
                                    addBottomDialogFragment = PeopleListEvent.newInstance();
                                    addBottomDialogFragment.setArguments(bundle);
                                    addBottomDialogFragment.setCancelable(false);
                                    transaction.addToBackStack(null);
                                    transaction.commit();
                                    addBottomDialogFragment.show(getSupportFragmentManager(),
                                            "event");
                                }
                            }

                        }
                        catch (JSONException e)
                        {
Log.v("ff",e.toString());
                        }
                    }
                    @Override
                    public void onError(ANError anError) {
                        Log.v("data",anError.getErrorBody());
                    }
                });

    }

    @Override
    public void onBackPressed() {
        new KAlertDialog(this, KAlertDialog.WARNING_TYPE)
                .setTitleText("Sign Out ?")
                .setContentText("You might have to login again ")
                .setCancelText("No")
                .setConfirmText("Yes")
                .showCancelButton(true)
                .setCancelClickListener(new KAlertDialog.KAlertClickListener() {
                    @Override
                    public void onClick(KAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                    @Override
                    public void onClick(KAlertDialog sDialog) {
                        FirebaseAuth.getInstance().signOut();
                        ScannerEvent.this.finish();
                    }
                })
                .show();
//        super.onBackPressed();
        return;
    }

    public void permission()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "not granted", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    1);
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
//            Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show();
            // Permission has already been granted
        }


    }
}
