package me.mariamdiallo.instagram.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import me.mariamdiallo.instagram.fragment.HomeFragment;
import me.mariamdiallo.instagram.fragment.ProfileFragment;
import me.mariamdiallo.instagram.R;
import me.mariamdiallo.instagram.fragment.UploadFragment;

public class MainActivity extends AppCompatActivity implements
        UploadFragment.PostListener,
        UploadFragment.RetakeListener,
        ProfileFragment.EditProfileListener
{

    HomeFragment homeFragment;
    UploadFragment uploadFragment;
    ProfileFragment profileFragment;

    Toolbar tbToolbar;
    ImageView ivLogo;
    ImageView ivCamera;

    private Dialog dialog;

    // The list of fragments used in the view pager. They live in the activity and we pass them down
    // to the adapter upon creation.
    private final List<Fragment> fragments = new ArrayList<>();

    // A reference to our view pager.
    private ViewPager viewPager;

    // The adapter used to display information for our bottom navigation view.
    private Adapter adapter;

    // A reference to our bottom navigation view.
    private BottomNavigationView bottomNavigation;

    static final int REQUEST_EDIT_PROFILE = 20;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_SELECT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivLogo = findViewById(R.id.ivLogo);
        ivCamera = findViewById(R.id.ivCamera);
        tbToolbar = findViewById(R.id.tbToolbar);

        // hide title at start
        setSupportActionBar(tbToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // when camera icon is clicked, user can take photo
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set upload fragment
                setCurrentItem(1);
                // take photo intent
                uploadFragment.launchImageCapture();
            }
        });

        // instantiate fragments
        homeFragment = new HomeFragment();
        uploadFragment = new UploadFragment();
        profileFragment = new ProfileFragment();

        // Create the fragments to be passed to the ViewPager
        fragments.add(homeFragment);
        fragments.add(uploadFragment);
        fragments.add(profileFragment);

        // Grab a reference to our view pager.
        viewPager = findViewById(R.id.pager);

        // Instantiate our Adapter which we will use in our ViewPager
        adapter = new Adapter(getSupportFragmentManager(), fragments);

        // Attach our adapter to our view pager.
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigation.setSelectedItemId(R.id.action_home);
                        break;
                    case 1:
                        // calling setSelectedItemId causes dialog to show up twice
                        break;
                    case 2:
                        bottomNavigation.setSelectedItemId(R.id.action_profile);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // Grab a reference to our bottom navigation view
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Handle the click for each item on the bottom navigation view.
        // we then delegate this out to the view pager adapter such that it can switch the
        // page which we are currently displaying
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                 switch (item.getItemId()) {
                    case R.id.action_home:
                        // Set the item to the first item in our list (home)
                        setCurrentItem(0);
                        return true;
                    case R.id.action_upload:
                        // create dialog to ask user what type of upload then go to upload fragment
                        createPostDialog();
                        return true;
                    case R.id.action_profile:
                        // Set the current item to the third item in our list (profile)
                        setCurrentItem(2);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    // The example view pager which we use in combination with the bottom navigation view to make
    // a smooth horizontal sliding transition.
    static class Adapter extends FragmentStatePagerAdapter {

        // The list of fragments which we are going to be displaying in the view pager.
        private final List<Fragment> fragments;

        public Adapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);

            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    // ask user whether they want to select or take image and start upload fragment accordingly
    public void createPostDialog() {
        CharSequence options[] = new CharSequence[] {"Select from pictures", "Capture picture"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create new post");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int option) {
                // set upload fragment
                setCurrentItem(1);

                // when "select from pictures" button is pressed, select picture
                if (option == 0) {
                    uploadFragment.launchImageSelect();
                }
                // when "capture picture" option is pressed, take picture
                else {
                    uploadFragment.launchImageCapture();
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

    // method for going back to home when user creates new post
    @Override
    public void launchHome() {
        bottomNavigation.setSelectedItemId(R.id.action_home);
        setCurrentItem(0);
        homeFragment.loadTopPosts(true);
    }

    void setCurrentItem(int i) {
        viewPager.setCurrentItem(i);
        // handle how title bar switches
        switch (i) {
            // home
            case 0:
                setTitle(getResources().getString(R.string.app_name));
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                ivLogo.setVisibility(View.VISIBLE);
                ivCamera.setVisibility(View.VISIBLE);
                break;

            // upload
            case 1:
                setTitle("Create new post");
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                ivLogo.setVisibility(View.GONE);
                ivCamera.setVisibility(View.GONE);
                break;

            // profile
            case 2:
                setTitle(ParseUser.getCurrentUser().getUsername());
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                ivLogo.setVisibility(View.GONE);
                ivCamera.setVisibility(View.GONE);
                break;
        }
    }

    // method for starting edit profile activity
    @Override
    public void launchEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivityForResult(intent, REQUEST_EDIT_PROFILE);
    }

    // go back to profile with updated info after user edits profile
    // or if this is result from editing profile picture, save new profile picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_EDIT_PROFILE) {
            setCurrentItem(3);
            setTitle(ParseUser.getCurrentUser().getUsername());
            profileFragment.loadProfile();
        }
        // try fragments' on activity results
        else {
            profileFragment.onActivityResult(requestCode, resultCode, data);
            uploadFragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
