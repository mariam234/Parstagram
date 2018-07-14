package me.mariamdiallo.instagram.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import me.mariamdiallo.instagram.R;
import me.mariamdiallo.instagram.models.Post;

public class UploadFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 10;
    private static final int REQUEST_IMAGE_SELECT = 11;

    EditText etDescription;
    EditText etLocation;
    Button btCreate;
    ImageView ivImage;
    ProgressBar pbProgressBar;

    Bitmap bitmap;
    File imageFile;

    Activity activity;

    private PostListener postListener;
    private RetakeListener retakeListener;

    // Listens for post successfully created and goes back to home
    public interface PostListener {
        void launchHome();
    }

    // Listens for user requesting to retake photo and shows dialog for capture type
    public interface RetakeListener {
        void createPostDialog();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        activity = getActivity();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the PostListener interface. If not, it throws an exception
        try {
            postListener = (PostListener) context;
            retakeListener = (RetakeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // get references to views
        etDescription = view.findViewById(R.id.etDescription);
        btCreate = view.findViewById(R.id.btCreate);
        ivImage = view.findViewById(R.id.ivImage);
        pbProgressBar = view.findViewById(R.id.pbProgressBar);
        etLocation = view.findViewById(R.id.etLocation);

        // when "create" button is clicked, upload image to Parse post database
        btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String description = etDescription.getText().toString();
                final String location = etLocation.getText().toString();
                final ParseUser user = ParseUser.getCurrentUser();
                final ParseFile parseFile = new ParseFile(imageFile);

                createPost(description, parseFile, user, location);
            }
        });

        // when image is pressed, give user option to retake photo
        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (retakeListener != null) {
                    retakeListener.createPostDialog();
                }
            }
        });
    }

    public void launchImageSelect() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        getActivity().startActivityForResult(intent, REQUEST_IMAGE_SELECT);
    }

    public void launchImageCapture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            getActivity().startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // adding new post to Parse database
    public void createPost(String description, ParseFile imageFile, ParseUser user, String location) {
        showProgressBar();

        final Post newPost = new Post();
        newPost.setDescription(description);
        newPost.setImage(imageFile);
        newPost.setUser(user);
        newPost.setLocation(location);

        newPost.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("MainActivity", "Create post success!");
                    hideProgressBar();
                    if (postListener != null) {
                        postListener.launchHome();
                        clearFragment();
                    }

                }
                else {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDetach() {
        postListener = null;
        retakeListener = null;
        super.onDetach();

    }

    // shows image in view after selecting/capturing and stores it in imageFile for upload to Parse
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // if user captured image
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                bitmap = (Bitmap)data.getExtras().get("data");
                imageFile = bitmapToFile(bitmap, activity);
                ivImage.setImageBitmap(bitmap);
            }

            else if (requestCode == REQUEST_IMAGE_SELECT && resultCode == Activity.RESULT_OK) {
                // recycle unused bitmaps
                if (bitmap != null) {
                    bitmap.recycle();
                }

                InputStream stream = activity.getContentResolver().openInputStream(
                        data.getData());
                bitmap = BitmapFactory.decodeStream(stream);

                // set imageFile to user's selected image and show image in view
                imageFile = bitmapToFile(bitmap, activity);
                ivImage.setImageBitmap(bitmap);
                stream.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    void clearFragment() {
        etLocation.setText("");
        etDescription.setText("");
        ivImage.setImageDrawable(getResources().getDrawable(R.color.transparent));
    }

    void showProgressBar() {
        pbProgressBar.setVisibility(View.VISIBLE);
    }

    void hideProgressBar () {
        pbProgressBar.setVisibility(View.GONE);
    }

    // converts bitmap to file
    public static File bitmapToFile (Bitmap bitmap, Activity activity) throws IOException {
        // create a file to write bitmap data
        File file = new File(activity.getCacheDir(), "new_file");
        file.createNewFile();

        // Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        // write the bytes in file
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();

        return file;
    }

}
