package me.mariamdiallo.instagram.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import me.mariamdiallo.instagram.Fragment.UploadFragment;
import me.mariamdiallo.instagram.R;

public class EditProfileActivity extends AppCompatActivity {

    EditText etName;
    EditText etUsername;
    EditText etBio;
    EditText etEmail;
    Button btSubmit;
    ImageView ivProfileImage;
    TextView tvEditProfileImage;
    ParseUser user;

    ParseFile profileImageParseFile;

    Dialog dialog;

    final int REQUEST_IMAGE_SELECT = 1;
    final int REQUEST_IMAGE_CAPTURE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        user = ParseUser.getCurrentUser();

        // get references to view
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etBio = findViewById(R.id.etBio);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        etEmail = findViewById(R.id.etEmail);
        btSubmit = findViewById(R.id.btSubmit);
        tvEditProfileImage = findViewById(R.id.tvEditProfileImage);

        // load user's existing data
        etName.setText(user.getString("name"));
        etUsername.setText((user.getUsername()));
        etBio.setText(user.getString("bio"));
        etEmail.setText(user.getEmail());
        profileImageParseFile = user.getParseFile("profileImage");
        if (profileImageParseFile != null) {
            Glide.with(this)
                    .load(profileImageParseFile.getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfileImage);
        }

        // show dialog for type of image capture when user requests to edit profile image
        tvEditProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });


        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString();
                String username = etUsername.getText().toString();
                String bio = etBio.getText().toString();
                String email = etEmail.getText().toString();
                saveProfile(name, username, bio, email, profileImageParseFile);
            }
        });

    }

    // puts new profile image in view after selecting/capturing
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Activity activity = getParent();
        Bitmap bitmap;
        File profileImageFile;
        try {
            // if user captured image
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                bitmap = (Bitmap) data.getExtras().get("data");
                // set imageFile to user's selected image and show image in view
                profileImageFile = UploadFragment.bitmapToFile(bitmap, getParent());
                profileImageParseFile = new ParseFile(profileImageFile);
            } else if (requestCode == REQUEST_IMAGE_SELECT && resultCode == Activity.RESULT_OK) {
                InputStream stream = activity.getContentResolver().openInputStream(
                        data.getData());
                bitmap = BitmapFactory.decodeStream(stream);

                // set imageFile to user's selected image and show image in view
                profileImageFile = UploadFragment.bitmapToFile(bitmap, activity);
                profileImageParseFile = new ParseFile(profileImageFile);
                stream.close();
            }
        }
        catch (FileNotFoundException e) {
        e.printStackTrace();
        }
        catch (IOException e) {
        e.printStackTrace();
        }
    }

    void saveProfile(String name, String username, String bio, String email, ParseFile profileImage) {
        // set new user values
        user.put("name", name);
        user.put("username", username);
        user.put("bio", bio);
        user.put("email", email);
        user.put("profileImage", profileImage);

        // save user's info and go back to profile
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
                else {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // ask user whether they want to select or take image and start upload fragment accordingly
    public void showDialog() {
        CharSequence options[] = new CharSequence[] {"Select from pictures", "Capture picture"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit profile picture");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int option) {
                // when "select from pictures" button is pressed, select picture
                if (option == 0) {
                    launchImageSelect();
                }
                // when "capture picture" option is pressed, take picture
                else {
                    launchImageCapture();
                }

            }
        });

        // dismiss old dialogs
        if (dialog != null) {
            dialog.dismiss();
        }

        // show new dialog
        dialog = builder.show();
    }

    public void launchImageSelect() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_IMAGE_SELECT);
    }

    public void launchImageCapture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }
}
