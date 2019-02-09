package dzo.com.emailverificationdemo;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private EditText etEmail,etEmailPwd;
    FirebaseUser user;
    String TAG="vsking:>>";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        Button bnVerify = findViewById(R.id.bnVerify);
        etEmail=findViewById(R.id.etEmail);
        etEmailPwd=findViewById(R.id.etEmailPwd);
        bnVerify.setOnClickListener(this);

    }
    @Override
    protected void onStart() {
        super.onStart();
       user = mAuth.getCurrentUser();
        updateUI(user);
    }

    private void updateUI(FirebaseUser currentUser) {
        if(currentUser!=null && currentUser.isEmailVerified()){
            Toast.makeText(this, "E-mail verified successfully !", Toast.LENGTH_LONG).show();
        }
    }
    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bnVerify:{
                String email,password;
                email=etEmail.getText().toString();
                password=etEmailPwd.getText().toString();
                if(!email.isEmpty() && !password.isEmpty()) {
                    if(isValidEmail(email)) {
                        if(mAuth.getCurrentUser()==null) {
                            verifyEmail(email, password);
                        }else{
                            Toast.makeText(this, "You have already authorized this e-mail.", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(this, "Invalid e-mail !", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this, "Fill the fields !", Toast.LENGTH_SHORT).show();

                }
                break;
            }

        }
    }

    private void verifyEmail(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            user = mAuth.getCurrentUser();
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                               if (task.isSuccessful()){
                                   ((TextView)findViewById(R.id.tvStatus)).setText("Eamil is authenticated...(Click to send verification link to e-mail.)");
                                   ((TextView)findViewById(R.id.tvStatus)).setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           if(user.isEmailVerified()){
                                               Toast.makeText(MainActivity.this, "You have already verified this email. ", Toast.LENGTH_SHORT).show();
                                               ((TextView)findViewById(R.id.tvStatus)).setText("E-mail verified successfully !");
                                           }else{
                                               sendLink();
                                           }
                                       }
                                   });
                               }
                                }
                            });
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });



    }

    private void sendLink() {
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    ((TextView)findViewById(R.id.tvStatus)).setText("Eamil sent to "+user.getEmail());
                    runnable.run();
                }
            }

        });
    }
    public void isEmailVerified(){
        user.reload();
        if (user.isEmailVerified()){
            ((TextView)findViewById(R.id.tvStatus)).setText("E-mail verified successfully.");
        }
    }
   Handler handler=new Handler();
   Runnable runnable=new Runnable() {
       @Override
       public void run() {
        isEmailVerified();
        handler.postDelayed(runnable,1000);
       }
   };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.getInstance().signOut();
    }
}
