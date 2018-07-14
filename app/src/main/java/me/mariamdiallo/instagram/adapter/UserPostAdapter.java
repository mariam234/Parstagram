package me.mariamdiallo.instagram.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.ocpsoft.prettytime.PrettyTime;
import org.parceler.Parcels;

import java.io.File;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.mariamdiallo.instagram.R;
import me.mariamdiallo.instagram.activity.PostDetailsActivity;
import me.mariamdiallo.instagram.models.Post;

// adapter for posts on profile
public class UserPostAdapter extends BaseAdapter {
    // list of posts and context
    List<Post> userPosts;
    Context context;

    // Clean all elements of the recycler
    public void clear() {
        userPosts.clear();
        notifyDataSetChanged();
    }

    // pass in the user posts array in the constructor
    public UserPostAdapter(Context context, List<Post> userPosts) {
        this.context = context;
        this.userPosts = userPosts;
    }

    @Override
    public int getCount() {
        return userPosts.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Post userPost = userPosts.get(position);
        if (view == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.item_user_post, null);
            final ViewHolder viewHolder = new ViewHolder(view);
            view.setTag(R.id.VIEW_HOLDER_KEY, viewHolder);
            view.setTag(R.id.POSITION_KEY, position);
        }

        final ViewHolder viewHolder = (ViewHolder)view.getTag(R.id.VIEW_HOLDER_KEY);
        Glide.with(context)
                .load(userPost.getImage().getUrl())
                .into(viewHolder.ivImage);

        return view;
    }

    // create the ViewHolder class
    public class ViewHolder implements View.OnClickListener {
        // declare views
        ImageView ivImage;

        public ViewHolder(View itemView) {
            // perform findViewById lookups
            ivImage = itemView.findViewById(R.id.ivImage);
            // set this as items' onclicklistener
            itemView.setOnClickListener(this);
        }

        // open details page for post on click
        public void onClick(View view) {
            // get item position
            int position = (int) view.getTag(R.id.POSITION_KEY);
            // get the post at the position from posts array
            Post post;
            ParseUser user;
            try {
                post = userPosts.get(position).fetchIfNeeded();
                user = post.getUser().fetchIfNeeded();
                // create intent go to post details activity
                Intent intent = new Intent (context, PostDetailsActivity.class);
                // put post details
                intent.putExtra("username", user.getUsername());
                intent.putExtra("description", post.getString("description"));
                intent.putExtra("imageUrl", post.getParseFile("image").getUrl());

                // format date
                Date timeRaw = post.getCreatedAt();
                Format formatter = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm aa");
                String time = formatter.format(timeRaw);
/*                PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
                String time = prettyTime.format(timeRaw);*/
                intent.putExtra("time", time);

                // check for null location and profile image
                String locationRaw = post.getString("location");
                String location = locationRaw == null ? "" : locationRaw;
                intent.putExtra("location", location);

                String profileImageUrlRaw = user.getParseFile("profileImage").getUrl();
                String profileImageUrl = locationRaw == null ? "" : profileImageUrlRaw;
                intent.putExtra("profileImageUrl", profileImageUrl);

                // start details activity
                context.startActivity(intent);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}