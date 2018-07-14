package me.mariamdiallo.instagram.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseFile;

import org.ocpsoft.prettytime.PrettyTime;
import org.parceler.Parcels;

import java.util.Date;
import java.util.Locale;

import me.mariamdiallo.instagram.R;
import me.mariamdiallo.instagram.models.Post;

public class PostDetailsActivity extends AppCompatActivity {

    // declare views
    ImageView ivProfileImage;
    ImageView ivImage;
    TextView tvUsername;
    TextView tvTime;
    TextView tvDescription;
    TextView tvLocation;
    TextView tvUsernameBottom;
    ImageView ivHeart;
    ImageView ivComment;
    ImageView ivSave;
    ImageView ivDirect;

    Toolbar tbToolbar;
    ImageView ivReturn;
    TextView tvTitle;

    // declare info fields
    String username;
    String description;
    String time;
    String profileImageUrl;
    String imageUrl;
    String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        // set actionbar to be toolbar and set title
        tbToolbar = findViewById(R.id.tbToolbar);
        setSupportActionBar(tbToolbar);

        // perform findViewById lookups
        ivProfileImage = findViewById(R.id.ivProfileImage);
        ivImage = findViewById(R.id.ivImage);
        tvUsername = findViewById(R.id.tvUsername);
        tvTime = findViewById(R.id.tvTime);
        tvDescription = findViewById(R.id.tvDescription);
        tvLocation = findViewById(R.id.tvLocation);
        tvUsernameBottom = findViewById(R.id.tvUsernameBottom);
        ivHeart = findViewById(R.id.ivHeart);
        ivComment = findViewById(R.id.ivComment);
        ivSave = findViewById(R.id.ivSave);
        ivDirect = findViewById(R.id.ivDirect);
        ivReturn = findViewById(R.id.ivReturn);
        tvTitle = findViewById(R.id.tvTitle);

        Intent intent = getIntent();

        // get info from intent
        username = intent.getStringExtra("username");
        description = intent.getStringExtra("description");
        time = intent.getStringExtra("time");
        location = intent.getStringExtra("location");
        imageUrl = intent.getStringExtra("imageUrl");
        profileImageUrl = intent.getStringExtra("profileImageUrl");

        // set activity title to "[username]'s post"
        tvTitle.setText(username + "'s post");

        // populate the views according to this data
        tvUsername.setText(username);
        tvUsernameBottom.setText(username);
        tvDescription.setText(description);
        tvLocation.setText(location);
        tvTime.setText(time);

        // load post image using glide
        Glide.with(this)
                .load(imageUrl)
                .into(ivImage);

        // load profile image on post if user has one
        if (!profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfileImage);
        }

        // set on click listeners
        ivReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}
