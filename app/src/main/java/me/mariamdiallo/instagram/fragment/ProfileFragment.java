package me.mariamdiallo.instagram.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import me.mariamdiallo.instagram.activity.LoginActivity;
import me.mariamdiallo.instagram.R;
import me.mariamdiallo.instagram.adapter.UserPostAdapter;
import me.mariamdiallo.instagram.models.Post;

public class ProfileFragment extends Fragment {

    ImageView ivProfileImage;
    Button btLogout;
    Button btEditProfile;
    ProgressBar pbProgressBar;
    TextView tvName;
    TextView tvBio;

    GridView gvUserPosts;
    ArrayList<Post> userPosts;
    UserPostAdapter userPostAdapter;

    Bitmap bitmap;
    File profileImageFile;
    Activity activity;

    ParseUser user;

    Dialog dialog;

    EditProfileListener editProfileListener;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_SELECT = 2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public interface EditProfileListener {
        void launchEditProfile();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        try {
            editProfileListener = (EditProfileListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        activity = getActivity();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvName = view.findViewById(R.id.tvName);
        tvBio = view.findViewById(R.id.tvBio);
        btLogout = view.findViewById(R.id.btLogout);
        btEditProfile = view.findViewById(R.id.btEditProfile);
        pbProgressBar = view.findViewById(R.id.pbProgressBar);
        gvUserPosts = view.findViewById(R.id.gvUserPosts);

        // init the arraylist (data source)
        userPosts = new ArrayList<>();
        // construct the adapter from this datasource
        userPostAdapter = new UserPostAdapter(getContext(), userPosts);
        // set the adapter
        gvUserPosts.setAdapter(userPostAdapter);

        // when user clicks on their profile picture, allow them to change profile image
        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[] {"Select from pictures", "Capture picture"};

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Edit profile picture");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int option) {
                        if (option == 0) {
                            launchImageSelect();
                        }
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
        });

        // take user to edit profile activity when they click on button
        btEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editProfileListener != null) {
                    editProfileListener.launchEditProfile();
                }

            }
        });

        // when user clicks logout, log them out
        btLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();

                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        hideProgressBar();
                    }
                });

                // go back to login screen
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        loadProfile();
        loadPosts();
    }

    //  saves new profile image after selecting/capturing
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        user = ParseUser.getCurrentUser();
        try {
            // if user captured image
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                Bitmap bitmap = (Bitmap)data.getExtras().get("data");
                profileImageFile = UploadFragment.bitmapToFile(bitmap, activity);
                ParseFile profileImageParseFile = new ParseFile(profileImageFile);
                user.put("profileImage", profileImageParseFile);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        loadProfile();
                    }
                });

            }

            else if (requestCode == REQUEST_IMAGE_SELECT && resultCode == Activity.RESULT_OK) {
                // recycle unused bitmaps
                if (bitmap != null) {
                    bitmap.recycle();
                }

                InputStream stream = activity.getContentResolver().openInputStream(
                        data.getData());
                bitmap = BitmapFactory.decodeStream(stream);
                profileImageFile = UploadFragment.bitmapToFile(bitmap, activity);
                ParseFile profileImageParseFile = new ParseFile(profileImageFile);
                user.put("profileImage", profileImageParseFile);
                stream.close();
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        loadProfile();
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        editProfileListener = null;
        super.onDetach();
    }

    public void loadProfile () {
        ParseUser user = ParseUser.getCurrentUser();
        tvName.setText(user.getString("name"));
        tvBio.setText(user.getString("bio"));
        // set profile image to be user's profile image from parse with Glide
        ParseFile profileImage = user.getParseFile("profileImage");
        if (profileImage != null) {
            ivProfileImage.setImageDrawable(getResources().getDrawable(R.color.transparent));
            Glide.with(getContext())
                    .load(profileImage.getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfileImage);
        }
    }

    public void loadPosts() {
        showProgressBar();
        final Post.Query postQuery = new Post.Query();
        postQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        postQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> postsList, ParseException e) {
                hideProgressBar();
                if (e == null) {
                    // clear out old items
                    userPosts.clear();

                    for (int i = 0; i < postsList.size(); i++) {
                        userPosts.add(0, postsList.get(i));
                        userPostAdapter.notifyDataSetChanged();
                    }
                }

                else {
                    e.printStackTrace();
                }
            }
        });
    }

    void launchImageCapture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            getActivity().startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    void launchImageSelect() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        getActivity().startActivityForResult(intent, REQUEST_IMAGE_SELECT);
    }

    void showProgressBar() {
        pbProgressBar.setVisibility(View.VISIBLE);
    }

    void hideProgressBar () {
        pbProgressBar.setVisibility(View.GONE);
        gvUserPosts.setVisibility(View.VISIBLE);
    }
}
