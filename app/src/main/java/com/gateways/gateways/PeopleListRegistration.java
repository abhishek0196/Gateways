package com.gateways.gateways;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
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
import android.widget.ImageButton;
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
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;



public class PeopleListRegistration extends RoundedBottomSheetDialogFragment {

    public static PeopleListRegistration newInstance() {
        return new PeopleListRegistration();
    }

    View v;
    final Fragment me=this;
    CheckBox scb;
    String teamId;
    String submittedBy;
    private FragmentToActivity mCallback;
    String event,role;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(getArguments().getString("teamId") != null)
        {
            teamId = getArguments().getString("teamId");
        }
        else if(getArguments().getString("teamIdd") != null)
        {
            teamId = getArguments().getString("teamIdd") ;

        }
        role = getArguments().getString("role");
        event = getArguments().getString("event");
        submittedBy =  getArguments().getString("submittedBy");

        readApiRequest(teamId);
        v =  inflater.inflate(R.layout.fragment_people_list_registration, container, false);
        return v;
    }

    public void readApiRequest(final String  teamId) {
        Toast.makeText(getContext(), ""+teamId, Toast.LENGTH_SHORT).show();

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "select");
            jsonObject.put("query", "select achromatix_gateways.participants.id,achromatix_gateways.participants.participant_name,college_name,team_name,case when achromatix_gateways.participants.id in (select gateways.attendance.pid from gateways.attendance) then 1 else 0 end as attendancee from   achromatix_gateways.participants inner join achromatix_gateways.teams on achromatix_gateways.participants.unique_team_code = achromatix_gateways.teams.unique_team_code where achromatix_gateways.participants.unique_team_code = \""+teamId+"\"; ");


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
                        Toast.makeText(getContext(), "response"+response, Toast.LENGTH_SHORT).show();
                        JSONArray jsonarray = null;
                        try {
                            jsonarray = new JSONArray(response);
                            if(jsonarray.length() == 0)
                            {
                                Toast.makeText(getContext(), "Empty", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                writeApiRequestTemporary(teamId,submittedBy);
                                generateView(response);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        Log.v("data",response);

                    }
                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(getContext(), "error"+teamId ,Toast.LENGTH_LONG   ).show();
                        Log.v("data",anError.getErrorBody());
                    }
                });
    }
    private void writeApiRequest(ArrayList<String> tags, ArrayList<String> pname, final String tname, final String cname, String submittedBy) {
//        Toast.makeText(getContext(), ""+teamId, Toast.LENGTH_SHORT).show();
        String query = "insert into gateways.attendance values";

        String pnamee = "";
        for(int  i = 0;i < tags.size(); i++)
        {

                query += "(\""+tags.get(i)+"\",\""+submittedBy+"\"),";
                pnamee += pname.get(i)+",";
        }
        pnamee = pnamee.substring(0,pnamee.lastIndexOf(','));

        query = query.substring(0,query.lastIndexOf(','));
//        Toast.makeText(getContext(), ""+query, Toast.LENGTH_SHORT).show();
//
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "insert");
            jsonObject.put("query", query);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String finalPnamee = pnamee;
        String url = getResources().getString(R.string.server_url);

        AndroidNetworking.post(url+"/put")
//        AndroidNetworking.post("http://Gateways-env.d9kekdzq4q.ap-south-1.elasticbeanstalk.com/put")
                .addJSONObjectBody(jsonObject) // posting json
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getContext(), ""+response, Toast.LENGTH_SHORT).show();
                        updateSheet(finalPnamee,tname,cname);
                        //generateView(response);
                    }
                    @Override
                    public void onError(ANError anError) {
                        Log.v("data",""+anError.getErrorBody());

                    }
                });
    }
    void generateView(final String response)
    {
        final ArrayList<String> tags = new ArrayList<String>();
        final ArrayList<String> pname= new ArrayList<String>();
        LinearLayout f = v.findViewById(R.id.frame);
        JSONArray jsonarray = null;
        String teamName = "",collegeName = "";
        LinearLayout uperFrame = v.findViewById(R.id.upperFrame);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;


        double newheight = height * 0.45;
        ScrollView sc = new ScrollView(getContext());
        sc.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) newheight));


        LinearLayout m = new LinearLayout(getContext());
        m.setOrientation(LinearLayout.VERTICAL);
        m.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        m.setGravity(Gravity.BOTTOM);
        try {
            jsonarray = new JSONArray(response);
              for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject jsonobject = jsonarray.getJSONObject(i);
                String id = jsonobject.getString("id");

//                Toast.makeText(getContext(), ""+id, Toast.LENGTH_SHORT).show();
                int attendance = jsonobject.getInt("attendancee");
                String name = jsonobject.getString("participant_name");
//                Toast.makeText(getContext(), "" + id, Toast.LENGTH_SHORT).show();
                scb = new CheckBox(getContext());
                scb.setTextColor(Color.parseColor("#23374d"));

                  Typeface typeface1 = ResourcesCompat.getFont(getContext(),R.font.nunitolight);
//            Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "font/playfairdisplayblack.ttf");
//            Typeface custom_font = Typeface.createFromFile(getActivity().getResources().getFont())
                  scb.setTypeface(typeface1);
                if (attendance == 1) {
                    scb.setChecked(true);
                    scb.setEnabled(false);
                }
                scb.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
//                Toast.makeText(getActivity(), ""+participantNames.get(i), Toast.LENGTH_SHORT).show();
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
//                                Toast.makeText(getContext(), "" + buttonView.getTag(), Toast.LENGTH_SHORT).show();
                                tags.add("" + buttonView.getTag());
                                pname.add("" + buttonView.getText());
//                                Toast.makeText(getContext(), "" + tags.size(), Toast.LENGTH_SHORT).show();
                            } else {
                                tags.remove("" + buttonView.getTag());
                                pname.remove("" + buttonView.getText());
                            }
                        }
                    });
                teamName = jsonobject.getString("team_name");
                collegeName = jsonobject.getString("college_name");

            }
            TextView title = v.findViewById(R.id.titlee);
            title.setText(teamName);
            Typeface typeface = ResourcesCompat.getFont(getContext(),R.font.palyfair);
//            Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "font/playfairdisplayblack.ttf");
//            Typeface custom_font = Typeface.createFromFile(getActivity().getResources().getFont())
            title.setTypeface(typeface);
            title.setTextColor(Color.parseColor("#23374d"));

            title.setTextSize(30);
            title.setGravity(Gravity.CENTER);
            //f.addView(title);

            ImageButton close = v.findViewById(R.id.close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteApiTemporaryData(teamId);
                }
            });

        Button myButton = v.findViewById(R.id.submit);

        sc.addView(m);
        f.addView(sc);

            final String finalCollegeName = collegeName;
            final String finalTeamName = teamName;
            myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), ""+tags.size(), Toast.LENGTH_SHORT).show();
                if(tags.size()>0) {
                    writeApiRequest(tags, pname, finalCollegeName, finalTeamName, submittedBy);

                }
            }

        });
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }

    private void deleteApiTemporaryData(String teamId) {
        String query = "delete from  gateways.temporaryData where id=(\""+teamId+"\");";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "insert");
            jsonObject.put("query", query);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = getResources().getString(R.string.server_url);

        AndroidNetworking.post(url+"/put")
//        AndroidNetworking.post("http://Gateways-env.d9kekdzq4q.ap-south-1.elasticbeanstalk.com/put")
                .addJSONObjectBody(jsonObject) // posting json
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getContext(), "hello"+response, Toast.LENGTH_SHORT).show();
                        mCallback = (FragmentToActivity)getContext();

                        mCallback.communicate("End");
                        getFragmentManager().beginTransaction().remove(me).commit();
                        //generateView(response);
                    }
                    @Override
                    public void onError(ANError anError) {
                        Log.v("data",""+anError.getErrorBody());

                    }
                });
    }


    private void writeApiRequestTemporary(String id,String submittedBy) {
        Toast.makeText(getContext(), "teamm"+id, Toast.LENGTH_SHORT).show();
        String query = "insert into gateways.temporaryData values(\""+id+"\",\""+submittedBy+"\");";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "insert");
            jsonObject.put("query", query);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = getResources().getString(R.string.server_url);

        AndroidNetworking.post(url+"/put")

//        AndroidNetworking.post("http://Gateways-env.d9kekdzq4q.ap-south-1.elasticbeanstalk.com/put")
                .addJSONObjectBody(jsonObject) // posting json
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getContext(), "hello"+response, Toast.LENGTH_SHORT).show();

                        //generateView(response);
                    }
                    @Override
                    public void onError(ANError anError) {
                        Log.v("data",""+anError.getErrorBody());

                    }
                });
    }


    private void updateSheet(String pname, String cname, String tname) {
        Toast.makeText(getContext(), ""+pname, Toast.LENGTH_SHORT).show();
        String url = "https://script.google.com/macros/s/AKfycbxB0-qTu2WSPIly8ODWlg95Igu5EoY1nzzMlZA-FoZBGxCYW6Q/exec?collegeName="+cname+"&participantName="+pname+"&teamName="+tname+"&sheetName=Registration&email="+submittedBy;

        AndroidNetworking.post("https://us-central1-gateways-c3a50.cloudfunctions.net/helloWorld")
                .setTag("test")
                .addQueryParameter("url", url)
                .setPriority(Priority.MEDIUM)
                .build()

                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        deleteApiTemporaryData(teamId);


                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.v("data","sss=>"+anError.getErrorBody());

                    }
                });
    }

}
