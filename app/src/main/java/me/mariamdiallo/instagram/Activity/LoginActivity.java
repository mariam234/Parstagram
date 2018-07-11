package me.mariamdiallo.instagram.Activity;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import me.mariamdiallo.instagram.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btLogin;
    private Button btSignUp;
    private ProgressBar pbProgressBar;

    final int REQUEST_SIGN_UP = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            launchMainActivity();
        }

        setContentView(R.layout.activity_login);

        // get references to views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btLogin = findViewById(R.id.btLogin);
        btSignUp = findViewById(R.id.btSignUp);
        pbProgressBar = findViewById(R.id.pbProgressBar);

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();

                login(username, password);
            }
        });

        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchSignUpActivity();
            }
        });
    }

    private void login(String username, String password) {

        showProgressBar();

        // always make network requests in background, otherwise it locks up UI
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                hideProgressBar();

                if (e == null) {
                    Log.d("LoginActivity", "Login Successful");
                    launchMainActivity();
                }
                else {
                    Log.e("LoginActivity", "Login Failure");
                    Toast.makeText(getBaseContext(), "Incorrect username and/or password.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    void launchMainActivity() {
        final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //todo - add back button to menu
    void launchSignUpActivity() {
        final Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        // start activity for result so that this activity can finish after sign up is done
        startActivityForResult(intent, REQUEST_SIGN_UP);
    }

    // finish this activity if user successfully signs up (so they don't return to login when back button is pressed)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_SIGN_UP && resultCode == RESULT_OK) {
            launchMainActivity();
            finish();
        }
    }

    void showProgressBar() {
        pbProgressBar.setVisibility(View.VISIBLE);
    }

    void hideProgressBar () {
        pbProgressBar.setVisibility(View.GONE);
    }
}
