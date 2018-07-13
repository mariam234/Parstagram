package me.mariamdiallo.instagram.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import me.mariamdiallo.instagram.R;

public class SignUpActivity extends AppCompatActivity {

    EditText etUsername;
    EditText etPassword;
    EditText etEmail;
    Button btSignUp;
    TextView tvReturn;
    private ProgressBar pbProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // get references to views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        btSignUp = findViewById(R.id.btSignUp);
        tvReturn = findViewById(R.id.tvReturn);
        pbProgressBar = findViewById(R.id.pbProgressBar);

        // on click listener for "sign up" button
        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get what user inputted
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String email = etEmail.getText().toString();

                // create new parse user
                ParseUser user = new ParseUser();
                user.setUsername(username);
                user.setPassword(password);
                user.setEmail(email);

                signUp(user);
            }
        });

        tvReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // sign up user in background
    private void signUp(ParseUser user) {
        showProgressBar();

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                hideProgressBar();
                if (e == null) {
                    setResult(RESULT_OK);
                    finish();
                }
                else {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    void showProgressBar() {
        pbProgressBar.setVisibility(View.VISIBLE);
    }

    void hideProgressBar () {
        pbProgressBar.setVisibility(View.GONE);
    }

}
