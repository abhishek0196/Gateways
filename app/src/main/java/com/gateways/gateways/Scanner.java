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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.developer.kalert.KAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;
//import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class Scanner extends AppCompatActivity implements ZXingScannerView.ResultHandler,FragmentToActivity{
    public ZXingScannerView mScannerView;
    //private ZXingScannerView zXingScannerView;
    private Vibrator myVib;
    String role,event,submittedBy;

    ArrayList<String> teamName;
    ArrayList<String> teamId;
    ArrayAdapter<String > adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        teamName = new ArrayList<String>();//Creating arraylist
        teamId  = new ArrayList<String>();//Creating arraylist
        getTeamNames();
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if(bd != null)
        {
            role = (String) bd.get("role");
            event = (String) bd.get("event");
            submittedBy = bd.getString("email");
//            Toast.makeText(this, "............"+submittedBy, Toast.LENGTH_SHORT).show();

        }
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(false);
//        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.hide();
        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        mScannerView = findViewById(R.id.zxscan);
        permission();
        AndroidNetworking.initialize(getApplicationContext());
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                Toast.makeText(Scanner.this, "hua", Toast.LENGTH_SHORT).show();
                getTeamNames();
                pullToRefresh.setRefreshing(false);
            }
        });

    }
    private void getTeamNames() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "select");
            jsonObject.put("query", "select team_name,unique_team_code from teams where unique_team_code in (select unique_team_code from participants) ;");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = getResources().getString(R.string.server_url);

        AndroidNetworking.post(url+"/get")
                .addJSONObjectBody(jsonObject) // posting json
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray jsonarray = null;
                        try {
                            jsonarray = new JSONArray(response);
                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject jsonobject = jsonarray.getJSONObject(i);
                                teamName.add(jsonobject.getString("team_name"));
                                teamId.add(jsonobject.getString("unique_team_code"));

                                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Scanner.this,
                                        android.R.layout.simple_dropdown_item_1line, teamName);
                                AutoCompleteTextView textView = findViewById(R.id.teamNames);
                                textView.setAdapter(adapter);
                                textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        String teamNamee = parent.getItemAtPosition(position).toString();
//                                        Toast.makeText(Scanner.this, "here"+adapter.getPosition(teamNamee), Toast.LENGTH_SHORT).show();
                                        int positionn = teamName.indexOf(teamNamee);
                                        String teamIdd = teamId.get(positionn);

//                                        // create Toast with user selected value
//
//                                        Toast.makeText(Scanner.this, "id-->"+teamIdd, Toast.LENGTH_SHORT).show();
                                        PeopleListRegistration addBottomDialogFragment =
                                                PeopleListRegistration.newInstance();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("teamIdd", teamIdd);
                                        bundle.putString("role", role);
                                        bundle.putString("event", event);
                                        bundle.putString("submittedBy", submittedBy);
                                        addBottomDialogFragment.setArguments(bundle);
                                        addBottomDialogFragment.setCancelable(false);

                                        //addBottomDialogFragment.set
                                        addBottomDialogFragment.show(getSupportFragmentManager(),
                                                "add_photo_dialog_fragment");

////                addBottomDialogFragment.readApiRequest(teamIdd);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        Log.v("data",response);
//                        generateView(response);
                    }
                    @Override
                    public void onError(ANError anError) {
//                        Toast.makeText(Scanner.this, "error"+anError.getErrorBody(),Toast.LENGTH_LONG   ).show();
                        Log.v("data",anError.getErrorBody());
                    }
                });

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
    public void onBackPressed() {
        //super.onBackPressed();
        return;
    }

    @Override
    protected void onPause() {
        super.onPause();
//        zXingScannerView.stopCamera();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        String qrResult = result.getText();
        String split[] = qrResult.split("_");
        String teamId = split[0];
        String participantId = split[1];
//      Toast.makeText(getApplicationContext(),result.getText()+"---"+teamId+"---"+participantId,Toast.LENGTH_SHORT).show();
//        apiRequest();
        myVib.vibrate(50);
//        bundle.putString("submittedBy", "dummy");

        getAlreadyExistingParticipants(teamId);




    }

    private void getAlreadyExistingParticipants(final String teamId) {

        String query = "select teams.team_name,gateways.temporaryData.submittedby from teams inner join gateways.temporaryData on teams.unique_team_code = gateways.temporaryData.id where gateways.temporaryData.id =\""+teamId+"\" ;";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "select");
            jsonObject.put("query", query);


        } catch (JSONException e) {
            e.printStackTrace();
        }

//        Toast.makeText(this, ""+query, Toast.LENGTH_SHORT).show();
        Log.v("query",query);


        final KAlertDialog pDialog1 = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE);
        pDialog1.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog1.setTitleText("Loading");
        pDialog1.setCancelable(false);
        pDialog1.show();
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
                        JSONArray jsonarray = null;
                        pDialog1.hide();
//                        Toast.makeText(Scanner.this, "aaya"+response, Toast.LENGTH_SHORT).show();
                        try {
                            jsonarray = new JSONArray(response);
                            if(jsonarray.length() > 0)
                            {
                                JSONObject jsonobject = jsonarray.getJSONObject(0);
                                String name = jsonobject.getString("submittedby");
                                String tname = jsonobject.getString("team_name");
                                name = name.substring(0,name.indexOf("."));
                                name = name.substring(0,1).toUpperCase() + name.substring(1);


                                KAlertDialog pDialog = new KAlertDialog(Scanner.this, KAlertDialog.ERROR_TYPE);
                                pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                                pDialog.setContentText(name+" is already with team "+tname);
                                pDialog.setConfirmClickListener(new KAlertDialog.KAlertClickListener()
                                {
                                    @Override
                                    public void onClick(KAlertDialog sDialog)
                                    {
                                        sDialog.dismissWithAnimation();
                                        onResume();
                                    }
                                });
                                pDialog.setCancelable(false);
                                pDialog.show();

                            }
                            else
                            {

                                Bundle bundle = new Bundle();
                                bundle.putString("role", role);
                                bundle.putString("event", event);
                                bundle.putString("teamId", teamId);

                                bundle.putString("submittedBy", submittedBy);

                                PeopleListRegistration addBottomDialogFragment =
                                        PeopleListRegistration.newInstance();
                                addBottomDialogFragment.setArguments(bundle);
                                addBottomDialogFragment.setCancelable(false);

                                //addBottomDialogFragment.set
                                addBottomDialogFragment.show(getSupportFragmentManager(),
                                        "add_photo_dialog_fragment");
                            }
                        } catch (Exception e) {
//                            Toast.makeText(Scanner.this, ""+e.toString(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
//                        Log.v("data",response);
//                        generateView(response);
                    }
                    @Override
                    public void onError(ANError anError) {
//                        Toast.makeText(Scanner.this, "error"+anError.getErrorBody(),Toast.LENGTH_LONG   ).show();
                        Log.v("data",anError.getErrorBody());
                    }
                });

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
