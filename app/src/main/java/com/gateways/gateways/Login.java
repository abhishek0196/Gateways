package com.gateways.gateways;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    String newVariable3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailText = email.getText().toString();
                passwordText = password.getText().toString();
                mAuth.signInWithEmailAndPassword(emailText,passwordText)
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(Login.this, "Successful", Toast.LENGTH_SHORT).show();
                                    //Log.d("", "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();

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
                                            Toast.makeText(Login.this, ""+role, Toast.LENGTH_SHORT).show();
                                            if(role.equals("Registrar")) {
                                                scanner = new Intent(Login.this, Scanner.class);
                                            }
                                            else if(role.equals("Coordinator"))
                                            {
                                                scanner = new Intent(Login.this, ScannerEvent.class);
                                            }
                                            scanner.putExtra("role", role);
                                            scanner.putExtra("event", event);
                                            scanner.putExtra("email", mAuth.getCurrentUser().getEmail());
                                            startActivity(scanner);


                                        }
                                    });
                                } else {

                                    Toast.makeText(Login.this, "signInWithEmail:failure" +task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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
}
