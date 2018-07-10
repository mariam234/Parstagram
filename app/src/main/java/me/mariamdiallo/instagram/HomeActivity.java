package me.mariamdiallo.instagram;

import android.app.Activity;
import android.content.Intent;
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

    private EditText etDescription;
    private Button btCreate;
    private Button btRefresh;
    private ImageView ivImage;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
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
        ivImage = findViewById(R.id.ivImage);

        // set on click listeners for buttons
        btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String description = etDescription.getText().toString();
                final ParseUser user = ParseUser.getCurrentUser();
                final ParseFile parseFile = new ParseFile(imageFile);

                createPost(description, parseFile, user);
            }
        });

        btRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadTopPosts();
            }
        });

        // prompt user to select image to post
        pickImage(ivImage);

        loadTopPosts();

    }

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

    // start intent to select image from memory
    public void pickImage(View View) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    // start intent to take image with camera
    public void takeImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // shows selected image in view
        // from http://blog.vogella.com/2011/09/13/android-how-to-get-an-image-via-an-intent/
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK)
            try {
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

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // creates File out of Bitmap
    // from https://stackoverflow.com/questions/7769806/convert-bitmap-to-file
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
