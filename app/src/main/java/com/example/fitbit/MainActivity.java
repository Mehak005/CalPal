package com.example.fitbit;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import static android.R.layout.simple_spinner_dropdown_item;

public class MainActivity extends AppCompatActivity {





    private CallbackManager mCallbackManager;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user!=null) {
                updateUI(user);
            }
            else{
                updateUI(null);
            }
    };
    private LoginButton loginButton;
    private static final String TAG = "FacebookAuthentication";
    private TextView  textViewUser;
    private ImageView mLogo;
    private AccessTokenTracker accessTokenTracker;

    private Spinner spinner;
    private EditText editText;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        spinner = findViewById(R.id.Spinner1);
        spinner.setAdapter(new ArrayAdapter<String>(simple_spinner_dropdown_item, this, CountryData.countryNames));

        editText = findViewById(R.id.Text);

        findViewById(R.id.buttonContinue).setOnClickListener(new View.OnClickListener() {
            @Override// called when clicked on the button, get cc and ph no.
            public void onClick(View v) {
                String code = CountryData.countryAreaCodes[spinner.getSelectedItemPosition()];
                //ph no entered by user.
                String number = editText.getText().toString().trim();

                if (number.isEmpty() || number.length() < 10){
                    editText.setError("Valid number is required");
                    editText.requestFocus();
                    return;
                }//if validation succeeds, start phone verify

                String phoneNumber = '+' + code + number;

                Intent intent = new  Intent(MainActivity.this,VerifyPhoneActivity.class);
                intent.putExtra("phoneNumber", phoneNumber);
                startActivity(intent);

            }
        });
    }


        @Override
        protected void onStart() {
            super.onStart();

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
            }
        });




       mFirebaseAuth = FirebaseAuth.getInstance();
       FacebookSdk.sdkInitialize(getApplicationContext());


       textViewUser = findViewById(R.id.text_user);
       mLogo = findViewById(R.id.image_logo);
       loginButton = findViewById(R.id.login_button);
       loginButton.setReadPermissions("email", "public_profile");
       mCallbackManager = CallbackManager.Factory.create();
       loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
          @Override
          public void onSuccess(LoginResult loginResult) {
              Log.d(TAG, "onSuccess" + loginResult);
              handleFacebookToken(loginResult.getAccessToken());
          }

          @Override
          public void onCancel() {
              Log.d(TAG, "onCancel");

          }

          @Override
          public void onError(FacebookException error) {
              Log.d(TAG, "onError" + error);

          }
      });

       accessTokenTracker = new AccessTokenTracker() {
           @Override
           protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
               if(currentAccessToken == null){
                   mFirebaseAuth.signOut();
               }
           }
       };



    }




    private void handleFacebookToken(AccessToken token){
        Log.d (TAG, "handleFacebookToken" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
             if(task.isSuccessful()) {
                 Log.d(TAG,"sign in with credentials: successful");
                 FirebaseUser user = mFirebaseAuth.getCurrentUser();
                 updateUI (user);
             }else{
                 Log.d(TAG,"sign in with credentials: failure"), task.getException());
                 Toast.makeText(MainActivity.this, "Authentication Failed",Toast.LENGTH_SHORT).show();
                 updateUI(null);
             }
            }
        });
    }


        @Override
        protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data){
         mCallbackManager.onActivityResult(requestCode, resultCode data);
         MainActivity.super.onActivityResult (requestCode, resultCode, data);
    }

    private void updateUI(FirebaseUser user) {
        if(user != null) {
             textViewUser.setText(user.getDisplayName());
             if(user.getPhotoUrl() != null){
                 String photoUrl = user.getPhotoUrl().toString();
                 photoUrl = photoUrl + "?type=large";
                 Picasso.get().load(photoUrl).into(mLogo);     }
        }
    }
     else

    {
        textViewUser.setText(" ");
        mLogo.setImageResource(R.drawable.logo);
    }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(authStateListener!=null){
            mFirebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}










