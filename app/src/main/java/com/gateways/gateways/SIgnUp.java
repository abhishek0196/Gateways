package com.gateways.gateways;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class SIgnUp extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private String email,password,role,event = "";
    FirebaseFunctions mFunctions;
    EditText emails,passwords;
    Spinner roles,events;
    Button signUp,login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_sign_u);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mFunctions = FirebaseFunctions.getInstance();

        emails = findViewById(R.id.email);
        passwords = findViewById(R.id.password);
        roles = findViewById(R.id.role);
        events = findViewById(R.id.event);

        signUp = findViewById(R.id.Register);
        login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(SIgnUp.this, Login.class);
                startActivity(login);
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emails.getText().toString();
                password = passwords.getText().toString();
                Toast.makeText(SIgnUp.this, email+"   "+password, Toast.LENGTH_SHORT).show();
                role = roles.getSelectedItem().toString();
                if(role.equals("Coordinator"))
                {
                    event = events.getSelectedItem().toString();
                }
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SIgnUp.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("RegSuccess", "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    addMessage()
                                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                                @Override
                                                public void onComplete(@NonNull Task<String> task) {
                                                    if (!task.isSuccessful()) {
                                                        Toast.makeText(SIgnUp.this, ""+task.getResult(), Toast.LENGTH_SHORT).show();
                                                        Exception e = task.getException();

                                                        if (e instanceof FirebaseFunctionsException) {
                                                            FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                                            FirebaseFunctionsException.Code code = ffe.getCode();
                                                            Object details = ffe.getDetails();
                                                        }

                                                        // ...
                                                    }
                                                    else {
                                                        Toast.makeText(SIgnUp.this, ""+task.getResult(), Toast.LENGTH_SHORT).show();

                                                    }

                                                    // ...
                                                }
                                            });

                                    //updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("RegSuccess", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(SIgnUp.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    //updateUI(null);
                                }

                                // ...
                            }
                        });
            }
        });

    }

    private Task<String> addMessage() {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("push", true);
        data.put("role",role);
        data.put("event",event);

        return mFunctions
                .getHttpsCallable("addRole")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        //Toast.makeText(SIgnUp.this, ""+task.getResult().getData().toString(), Toast.LENGTH_SHORT).show();
                        String result ;//(String) task.getResult().getData();
                        HashMap<String,String> res =(HashMap<String, String>) task.getResult().getData();
                        result = res.get("message");

//                        Toast.makeText(SIgnUp.this, ""+result, Toast.LENGTH_SHORT).show();
                        return result;
                    }
                });

    }
}
