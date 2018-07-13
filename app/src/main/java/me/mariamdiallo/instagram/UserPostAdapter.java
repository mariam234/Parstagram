package me.mariamdiallo.instagram;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseFile;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.mariamdiallo.instagram.models.Post;

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
            final ImageView ivImage = view.findViewById(R.id.ivImage);

            final ViewHolder viewHolder = new ViewHolder(ivImage);
            view.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder)view.getTag();
        viewHolder.ivImage.setImageDrawable(context.getResources().getDrawable(R.color.black));

        return view;
    }

    // create the ViewHolder class
    public class ViewHolder {
        // declare views
        ImageView ivImage;

        public ViewHolder(View itemView) {
            // perform findViewById lookups
            ivImage = itemView.findViewById(R.id.ivImage);
        }

        // call appropriate method from activity timeline for each click
        public void onClick(View view) {

            // get item position
            // int position = ();
            // get the post at the position from posts array
            // Post userPost = userPosts.get(position);

            Toast.makeText(context, "Post clicked!", Toast.LENGTH_SHORT).show();
        }
    }
}