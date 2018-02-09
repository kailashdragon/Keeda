package com.example.kailashpatel.keeda;

import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION_CODES.O;
import static java.lang.Boolean.FALSE;

public class SignPhone extends AppCompatActivity {

    private static final String TAG="PhoneAuth";
    private EditText PhoneNumber;
    private EditText codeText;
    private Button sendButton;
    private Button resendButton;
    private Button verifyButton;
    private Button signoutButton;
    private TextView statusText;

    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationStateChangedCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_phone);

        PhoneNumber=(EditText)findViewById(R.id.PhoneNumber);
        codeText=(EditText)findViewById(R.id.codeText);
        sendButton=(Button)findViewById(R.id.sendButton);
        resendButton=(Button)findViewById(R.id.resendButton);
        verifyButton=(Button)findViewById(R.id.verifyButton);
        signoutButton=(Button)findViewById(R.id.signoutButton);
        statusText=(TextView)findViewById(R.id.statusText);

        verifyButton.setEnabled(FALSE);
        resendButton.setEnabled(FALSE);
        signoutButton.setEnabled(FALSE);
        statusText.setText("Signed Out");

        firebaseAuth=FirebaseAuth.getInstance();
    }
    public void sendCode(View view){
        String phonenumber=PhoneNumber.getText().toString();
        setUpVerificationCallbacks();
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phonenumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                verificationStateChangedCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]
    }
    private void setUpVerificationCallbacks(){
        verificationStateChangedCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                signoutButton.setEnabled(true);
                statusText.setText("Signed in");
                resendButton.setEnabled(false);
                verifyButton.setEnabled(false);
                codeText.setText("");
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Log.d(TAG, "Invalid credential: " + e.getLocalizedMessage());
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Log.d(TAG, "SMS quota exceeded:");
                }
            }
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token){
                phoneVerificationId=verificationId;
                resendingToken=token;
                verifyButton.setEnabled(true);
                sendButton.setEnabled(false);
                resendButton.setEnabled(true);

            }
        };
    }
    public void verifyCode(View view){
        String code=codeText.getText().toString();
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(phoneVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }
    public void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    signoutButton.setEnabled(true);
                                    codeText.setText("");
                                    statusText.setText("Signed In");
                                    resendButton.setEnabled(false);
                                    verifyButton.setEnabled(false);
                                    FirebaseUser user = task.getResult().getUser();
                                } else {
                                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                                    }
                                }
                            }

        });

    }
    public void resendCode(View view){
        String phonenumber=PhoneNumber.getText().toString();
        setUpVerificationCallbacks();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phonenumber,
                60,
                TimeUnit.SECONDS,
                this,
                verificationStateChangedCallbacks,
                resendingToken);
    }
    public void signOut(View view){
        firebaseAuth.signOut();
        statusText.setText("signed out");
        signoutButton.setEnabled(true);
        sendButton.setEnabled(false);
    }
}
