package me.mariamdiallo.instagram.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.mariamdiallo.instagram.Fragment.HomeFragment;
import me.mariamdiallo.instagram.Fragment.ProfileFragment;
import me.mariamdiallo.instagram.R;
import me.mariamdiallo.instagram.Fragment.UploadFragment;

public class MainActivity extends AppCompatActivity implements UploadFragment.PostListener {

    HomeFragment homeFragment;
    UploadFragment uploadFragment;
    ProfileFragment profileFragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        bottomNavigation.setSelectedItemId(R.id.action_upload);
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
                        viewPager.setCurrentItem(0);
                        return true;
                    case R.id.action_upload:
                        // create dialog to ask user what type of upload then go to upload fragment
                        createPostDialog();
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.action_profile:
                        // Set the current item to the third item in our list (profile)
                        viewPager.setCurrentItem(2);
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
    void createPostDialog() {
        CharSequence options[] = new CharSequence[] {"Select from pictures", "Capture picture"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Post new image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int option) {
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
        viewPager.setCurrentItem(0);
        homeFragment.loadTopPosts(true);
        homeFragment.rvPosts.smoothScrollToPosition(0);
    }
}
