package me.mariamdiallo.instagram;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import me.mariamdiallo.instagram.models.Post;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_SELECT = 2;

    private EditText etDescription;
    private Button btSelectImage;
    private Button btTakeImage;
    private Button btCreate;
    private Button btRefresh;
    private ImageView ivImage;
    private Bitmap bitmap;
    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // get references to views
        etDescription = findViewById(R.id.etDescription);
        btCreate = findViewById(R.id.btCreate);
        btRefresh = findViewById(R.id.btRefresh);
        btSelectImage = findViewById(R.id.btSelectImage);
        btTakeImage = findViewById(R.id.btTakeImage);
        ivImage = findViewById(R.id.ivImage);

        // when "create" button is clicked, upload image to Parse post database
        btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String description = etDescription.getText().toString();
                final ParseUser user = ParseUser.getCurrentUser();
                final ParseFile parseFile = new ParseFile(imageFile);

                createPost(description, parseFile, user);
            }
        });

        // when "take image" button is pressed, start intent to take image with camera
        btTakeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        // when "select image" button is pressed, start intent to select image from phone
        btSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_IMAGE_SELECT);
            }
        });

        // when "refresh" button is pressed, reload posts
        btRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadTopPosts();
            }
        });

        // load posts on create
        loadTopPosts();

    }

    // adding new post to Parse database
    public void createPost(String description, ParseFile imageFile, ParseUser user) {
        final Post newPost = new Post();
        newPost.setDescription(description);
        newPost.setImage(imageFile);
        newPost.setUser(user);

        newPost.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("HomeActivity", "Create post success!");
                }
                else {
                    e.printStackTrace();
                }
            }
        });
    }

    // loading posts
    private void loadTopPosts() {
        final Post.Query postQuery = new Post.Query();
        postQuery.getTop().withUser();

        postQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); i++) {
                        Log.d("HomeActivity",
                                String.format("Post[%d]=%s\nusername=%s",
                                        i, objects.get(i).getDescription(),
                                        objects.get(i).getUser().getUsername()));
                    }
                }
                else {
                    e.printStackTrace();
                }
            }
        });
    }

    // shows image in view after selecting/capturing and stores it in imageFile for upload to Parse
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // if user captured image
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                Bitmap bitmap = (Bitmap)data.getExtras().get("data");
                imageFile = bitmapToFile(bitmap);
                ivImage.setImageBitmap(bitmap);
            }

            else if (requestCode == REQUEST_IMAGE_SELECT && resultCode == Activity.RESULT_OK) {
                // recycle unused bitmaps
                if (bitmap != null) {
                    bitmap.recycle();
                }

                InputStream stream = getContentResolver().openInputStream(
                        data.getData());
                bitmap = BitmapFactory.decodeStream(stream);

                // set imageFile to user's selected image and show image in view
                imageFile = bitmapToFile(bitmap);
                stream.close();
                ivImage.setImageBitmap(bitmap);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // converts bitmap to file
    private File bitmapToFile (Bitmap bitmap) throws IOException {
        //create a file to write bitmap data
        File file = new File(this.getCacheDir(), "new_file");
        file.createNewFile();

        //Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();

        return file;
    }

}
