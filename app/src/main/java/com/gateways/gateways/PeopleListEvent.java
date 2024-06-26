package com.gateways.gateways;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.widget.CompoundButtonCompat;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;



public class PeopleListEvent extends RoundedBottomSheetDialogFragment implements  ActivityToFragment {

    public static PeopleListEvent newInstance() {
        return new PeopleListEvent();
    }

    View v;
    CheckBox scb;

    String submittedBy;
    String teamName = "";
    private FragmentToActivity mCallback;
    String role,event;
    ArrayList<String> participantId = new ArrayList<String>();
    Fragment me=this;
    final ArrayList<String> tags = new ArrayList<String>();
    ArrayList<String> pname= new ArrayList<String>();
    String finalCollegeName;
//    ArrayList<String> result =
EditText title;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        try {
            role  = getArguments().getString("role");
            event= getArguments().getString("event");
            participantId.add(getArguments().getString("participantId"));
            submittedBy =  getArguments().getString("submittedBy");
            v =  inflater.inflate(R.layout.fragment_people_list_event, container, false);
            Button add = v.findViewById(R.id.add);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback = (FragmentToActivity)getContext();
                    mCallback.communicate("Add");

                }
            });

            Button submit = v.findViewById(R.id.submit);

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Size"+pname.size()+"----"+pname.get(0), Toast.LENGTH_SHORT).show();
                    if(pname.size()>1)
                    {
                        teamName = title.getText().toString();
                        writeApiRequest(tags,pname, finalCollegeName, teamName,event,submittedBy);
                    }
                    else if(pname.size() == 1)
                    {
                        teamName = "solo";
                        writeApiRequest(tags,pname, finalCollegeName, teamName,event,submittedBy);
                    }


                }
            });

            readApiRequest(participantId);

        }
        catch (Exception e)
        {

        }


         return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    private void readApiRequest(ArrayList<String> pid) {
        Log.v("query","length"+pid.size());
        String query = "  select participant_name,teams.college_name,participants.id from participants inner join teams on participants.unique_team_code = teams.unique_team_code where participants.id in (";

        for(int i = 0 ; i< pid.size();i++)
        {
            Log.v("query","data"+pid.get(i));
            query += pid.get(i)+",";
        }
        query = query.substring(0,query.lastIndexOf(','));
        query +=");";
        Log.v("query",query);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "select");
            jsonObject.put("query", query);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post("http://Gateways-env.d9kekdzq4q.ap-south-1.elasticbeanstalk.com/get")
                .addJSONObjectBody(jsonObject) // posting json
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
//                        Toast.makeText(getContext(), "response"+response, Toast.LENGTH_SHORT).show();
//                        Log.v("data",response);
                        generateView(response);
                    }
                    @Override
                    public void onError(ANError anError) {
//                        Toast.makeText(getContext(), "error"+pid ,Toast.LENGTH_LONG   ).show();
                        Log.v("data",anError.getErrorBody());
                    }
                });
    }
    private void writeApiRequest(ArrayList<String> tags, ArrayList<String> pname, final String cname, final String tname, final String eventName, String submittedBy) {
        String query = "insert into gateways.events values";
        String pnamee = "";

        for(int  i = 0;i < tags.size(); i++)
        {
            query += "(\""+tags.get(i)+"\",\""+tname+"\",\""+pname.get(i)+"\",\""+eventName+"\",\""+cname+"\",\""+submittedBy+"\"),";
            pnamee += pname.get(i)+",";
        }

        pnamee = pnamee.substring(0,pnamee.lastIndexOf(','));

        query = query.substring(0,query.lastIndexOf(','));
        JSONObject jsonObject = new JSONObject();
        Log.v("daa",query);
        Log.v("daa",pnamee);
        Toast.makeText(getContext(), ""+pnamee, Toast.LENGTH_SHORT).show();
        Toast.makeText(getContext(), ""+query, Toast.LENGTH_LONG).show();
        try {
            jsonObject.put("type", "insert");
            jsonObject.put("query", query);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String finalPnamee = pnamee;
        AndroidNetworking.post("http://Gateways-env.d9kekdzq4q.ap-south-1.elasticbeanstalk.com/put")
                .addJSONObjectBody(jsonObject) // posting json
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
//                        Toast.makeText(getContext(), ""+response, Toast.LENGTH_SHORT).show();
                        updateSheet(finalPnamee,tname,cname,eventName);
                        //generateView(response);
                    }
                    @Override
                    public void onError(ANError anError) {
                        Log.v("data",""+anError.getErrorBody());

                    }
                });
    }

    LinearLayout f;
    void generateView(final String response)
    {



         f = v.findViewById(R.id.frame);

        JSONArray jsonarray = null;


        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            jsonarray = new JSONArray(response);
            ScrollView sc = new ScrollView(getContext());
            double newheight = 0.0;
            if(jsonarray.length() == 1 )
            {
                newheight = height * 0.15;
            }
            else if(jsonarray.length() > 1 )
            {
                title= new     EditText(getContext());
                title.setHint("Enter Team Name");
                title.setTextSize(30);
                title.setGravity(Gravity.CENTER);
                f.addView(title);
                newheight = height * 0.35;
            }

            sc.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) newheight));
            LinearLayout m = new LinearLayout(getContext());
            m.setOrientation(LinearLayout.VERTICAL);
            m.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            m.setGravity(Gravity.BOTTOM);
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject jsonobject = jsonarray.getJSONObject(i);
                String id = jsonobject.getString("id");
                String name = jsonobject.getString("participant_name");

                scb = new CheckBox(getContext());
//
                for (int k = 0 ; k< tags.size();k++)
                {
                    if (tags.get(k).equals(id)) {
                        scb.setChecked(true);
//                        scb.setEnabled(false);
                    }
                }

                scb.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));

                scb.setText(name);
                scb.setGravity(Gravity.CENTER | Gravity.LEFT);
                scb.setTextSize(25);
                int states[][] = {{android.R.attr.state_checked}, {}};
                int colors[] = {Color.DKGRAY, Color.GRAY};
                CompoundButtonCompat.setButtonTintList(scb, new ColorStateList(states, colors));
                scb.setTag(id);

                m.addView(scb);
                scb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {

                            tags.add("" + buttonView.getTag());
                            pname.add("" + buttonView.getText());
//                                Toast.makeText(getContext(), "" + tags.size(), Toast.LENGTH_SHORT).show();
                        } else {
                            tags.remove("" + buttonView.getTag());
                            pname.remove("" + buttonView.getText());
                        }
                    }
                });

                finalCollegeName = jsonobject.getString("college_name");

            }

            sc.addView(m);
            f.addView(sc);
        } catch (JSONException e) {
            e.printStackTrace();

        }



    }


    private void updateSheet(String pname, String cname, String tname,String ename) {

        Toast.makeText(getContext(), ""+pname, Toast.LENGTH_SHORT).show();
        String url = "https://script.google.com/macros/s/AKfycbxB0-qTu2WSPIly8ODWlg95Igu5EoY1nzzMlZA-FoZBGxCYW6Q/exec?collegeName="+cname+"&participantName="+pname+"&teamName="+tname+"&sheetName=123&email="+submittedBy;
        AndroidNetworking.post("http://Gateways-env.d9kekdzq4q.ap-south-1.elasticbeanstalk.com/sheet/Gaming")

                .setTag("test")

                .addQueryParameter("url", url)
                .setPriority(Priority.MEDIUM)
                .build()

                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("data", "DocumentSnapshot data: " + response);

                        mCallback = (FragmentToActivity)getContext();
                        mCallback.communicate("End");
                        getFragmentManager().beginTransaction().remove(me).commit();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.v("data","sss=>"+anError.getErrorBody());

                    }
                });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((ScannerEvent)getActivity()).mCallback = this;
//        Toast.makeText(getContext(), ""+this, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void communicate(String comm) {
//        Toast.makeText(getContext(), "datat->>>"+comm  , Toast.LENGTH_SHORT).show();
        participantId.add(comm);
        f.removeAllViews();
        readApiRequest(participantId);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
