package com.gateways.gateways;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;
//import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerEvent extends AppCompatActivity implements ZXingScannerView.ResultHandler,FragmentToActivity{
    public ZXingScannerView mScannerView;
    //private ZXingScannerView zXingScannerView;
    private Vibrator myVib;
    String role,event,submittedBy;
    FragmentManager manager;
    PeopleListEvent addBottomDialogFragment;
    public  ActivityToFragment mCallback = null;

    ListView listView;
    ArrayList<String> list;
    ArrayAdapter<String > adapter;

    private static final String[] COUNTRIES = new String[] { "Belgium",
            "France", "France_", "Italy", "Germany", "Spain" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_event);
        ActionBar actionBar = getSupportActionBar();
        Toast.makeText(this, ""+actionBar, Toast.LENGTH_SHORT).show();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);



        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.actionbar, null);

        actionBar.setCustomView(v);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        AutoCompleteTextView textView = (AutoCompleteTextView) v
                .findViewById(R.id.editText1);
        textView.setThreshold(2);
        textView.setAdapter(adapter);
        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
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
//        mScannerView = new ZXingScannerView(this) {
//
//            @Override
//            protected IViewFinder createViewFinderView(Context context) {
//                return new CustomZXingScannerView(context);
//            }
//
//        };
        permission();
        AndroidNetworking.initialize(getApplicationContext());
//        zXingScannerView =new ZXingScannerView(getApplicationContext());
//        setContentView(zXingScannerView);


        //mScannerView.addView(m);


        mScannerView.setResultHandler(this);


        mScannerView.startCamera();
//        zXingScannerView.setResultHandler(this);
//        zXingScannerView.startCamera();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "granteddd", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "granteddd not", Toast.LENGTH_SHORT).show();
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
//        Toast.makeText(this, "Resumed", Toast.LENGTH_SHORT).show();
        scan(this.mScannerView);

    }

    public void communicate(String comm)
    {
        onResume();
//        Toast.makeText(this, "comm->>>>>>>>>>>>>>>>>>>> "+comm, Toast.LENGTH_SHORT).show();
        if(comm.equals("End"))
        {
            Toast.makeText(this, "close", Toast.LENGTH_SHORT).show();
            mCallback = null;
        }
        else
        {
            Toast.makeText(this, "add", Toast.LENGTH_SHORT).show();
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

    @Override
    public void handleResult(Result result) {
        String qrResult = result.getText();
        try {
            String split[] = qrResult.split("_");
            String teamId = split[0];
            String participantId = split[1];
            Toast.makeText(getApplicationContext(), result.getText() + "---" + teamId + "---" + participantId, Toast.LENGTH_SHORT).show();

//        apiRequest();
        myVib.vibrate(50);
            manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
//            transaction.add(R.id.container,YOUR_FRAGMENT_NAME,YOUR_FRAGMENT_STRING_TAG);

        Bundle bundle = new Bundle();
        bundle.putString("role", role);
        bundle.putString("event", event);
//            bundle.putString("event", "Coding");
        bundle.putString("teamId", teamId);
        bundle.putString("participantId", participantId);
        bundle.putString("submittedBy", submittedBy);
//        bundle.putString("submittedBy", "dummy");

        // Get FragmentManager and FragmentTransaction object.
        // Create FragmentOne instance.

            //Toast.makeText(this, "ghotalaa  "+addBottomDialogFragment, Toast.LENGTH_SHORT).show();

//        FragmentUtil.printActivityFragmentList(fragmentManager);

            if(mCallback!=null) {
                mCallback.communicate(participantId);

            }
            else {
                //Toast.makeText(this, "Created", Toast.LENGTH_SHORT).show();
                addBottomDialogFragment =
                        PeopleListEvent.newInstance();
                addBottomDialogFragment.setArguments(bundle);
                addBottomDialogFragment.setCancelable(false);
//                Toast.makeText(this, ""+addBottomDialogFragment.getClass().getName(), Toast.LENGTH_SHORT).show();
                transaction.addToBackStack(null);
                transaction.commit();
                //addBottomDialogFragment.set
                addBottomDialogFragment.show(getSupportFragmentManager(),
                        "event");
            }

//
            //mCallback =this;

        }
        catch (Exception e)
        {
            Log.v("dataa",e.toString());
            Toast.makeText(this, "Not A Valid QR \n Scan Again  ", Toast.LENGTH_SHORT).show();
            onResume();
        }
// set Fragmentclass Arguments


//        zXingScannerView.resumeCameraPreview(this);
//        mScannerView.resumeCameraPreview(this);

    }

    public void permission()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "not granted", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show();
            // Permission has already been granted
        }


    }
}
