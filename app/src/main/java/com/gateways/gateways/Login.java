package com.gateways.gateways;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.developer.kalert.KAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

public class Login extends AppCompatActivity {
    EditText email,password;
    String emailText,passwordText;
    Button login;
    FirebaseAuth mAuth;

    FirebaseUser user;
     KAlertDialog pDialog1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        email = findViewById(R.id.email);

        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();
       user = mAuth.getCurrentUser();
        pDialog1 = new KAlertDialog(Login.this, KAlertDialog.PROGRESS_TYPE);
        if(user != null)
        {
            pDialog1.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog1.setTitleText("Loading");
            pDialog1.setCancelable(false);
            pDialog1.show();
            successfulLogin();
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    emailText = email.getText().toString().trim();
                    passwordText = password.getText().toString();
                    pDialog1.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    pDialog1.setTitleText("Loading");
                    pDialog1.setCancelable(false);
                    pDialog1.show();
                    mAuth.signInWithEmailAndPassword(emailText,passwordText)
                            .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        successfulLogin();
                                    } else {
                                        pDialog1.hide();
                                        final KAlertDialog pDialog2 = new KAlertDialog(Login.this, KAlertDialog.ERROR_TYPE);
                                        pDialog2.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                                        pDialog2.setTitleText("Invalid Credentials");
                                        pDialog2.setCancelable(false);
                                        pDialog2.show();
                                    }
                                }
                            });
                }
                catch (Exception e) {
                    pDialog1.hide();
                    final KAlertDialog pDialog2 = new KAlertDialog(Login.this, KAlertDialog.ERROR_TYPE);

                    pDialog2.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    pDialog2.setTitleText("Please Provide The Details");

                    pDialog2.setCancelable(false);
                    pDialog2.show();

                    return;
                }
                }

        });
        Button signup = findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signup  = new Intent(Login.this,SIgnUp.class);
                startActivity(signup);
            }
        });
    }
    private  void successfulLogin()
    {
        user = mAuth.getCurrentUser();
        user.getIdToken(true)
                .addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                    @Override
                    public void onSuccess(GetTokenResult result) {

//                                            Map<String,Object> res =
                        String role = result.getClaims().get("roles").toString();
                        String event = result.getClaims().get("event").toString();
//                                            Toast.makeText(Login.this, value+"event:"+value1.toString(), Toast.LENGTH_SHORT).show();
                        //+"event:"+value1.toString()
                        //Log.v("avvvv",result.);
                        Intent scanner  = new Intent();
//                        Toast.makeText(Login.this, ""+role, Toast.LENGTH_SHORT).show();
                        if(role.equals("Registrar")) {
                            scanner = new Intent(Login.this, Scanner.class);
                        }
                        else if(role.equals("Coordinator"))
                        {
                            scanner = new Intent(Login.this, ScannerEvent.class);
                        }
                        pDialog1.hide();
                        scanner.putExtra("role", role);
                        scanner.putExtra("event", event);
                        scanner.putExtra("email", mAuth.getCurrentUser().getEmail());
                        startActivity(scanner);
                    }
                });

    }
}
