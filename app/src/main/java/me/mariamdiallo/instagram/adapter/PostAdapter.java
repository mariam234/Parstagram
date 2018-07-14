package me.mariamdiallo.instagram.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import me.mariamdiallo.instagram.R;
import me.mariamdiallo.instagram.models.Post;

// adapter for posts on home
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    // list of posts and context
    List<Post> posts;
    Context context;

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // pass in the posts array in the constructor
    public PostAdapter(List<Post> postsArg) {
        posts = postsArg;
    }

    // for each row, inflate the layout and cache references into ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // get context and create inflater
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View postView = inflater.inflate(R.layout.item_post, parent, false);
        ViewHolder viewHolder = new ViewHolder(postView);
        return viewHolder;
    }

    // bind the values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get the data according to position
        Post post = posts.get(position);

        // populate the holder's views according to this data
        holder.tvUsername.setText(post.getUser().getUsername());
        holder.tvUsernameBottom.setText(post.getUser().getUsername());
        holder.tvDescription.setText(post.getDescription());
        String location = post.getString("location");
        if (location != null) {
            holder.tvLocation.setText(location);
        }

        // enter time post was created at
        Date createdAt = post.getCreatedAt();
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        String relativeCreatedAt = prettyTime.format(createdAt);
        holder.tvTime.setText(relativeCreatedAt.toUpperCase());

        // load post image using glide
        Glide.with(context)
                .load(post.getImage().getUrl())
                .into(holder.ivImage);

        // load profile image on post if user has one
        ParseFile profileImageFile = post.getUser().getParseFile("profileImage");
        if (profileImageFile != null) {
            Glide.with(context)
                    .load(profileImageFile.getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.ivProfileImage);
        }
        else {
            holder.ivProfileImage.setImageDrawable(context.getResources().getDrawable(R.drawable.default_profile));
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    // create the ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

        public ViewHolder(View itemView) {
            super(itemView);

            // perform findViewById lookups
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvUsernameBottom = itemView.findViewById(R.id.tvUsernameBottom);
            ivHeart = itemView.findViewById(R.id.ivHeart);
            ivComment = itemView.findViewById(R.id.ivComment);
            ivSave = itemView.findViewById(R.id.ivSave);
            ivDirect = itemView.findViewById(R.id.ivDirect);

            // set this as items' onclicklistener
            itemView.setOnClickListener(this);

        }

        // call appropriate method from activity timeline for each click
        @Override
        public void onClick(View view) {

            // get item position
            int position = getAdapterPosition();
            // get the post at the position from posts array
            Post post = posts.get(position);

            switch (view.getId()) {
                case R.id.ivHeart: {
                    Toast.makeText(context, "Heart clicked!", Toast.LENGTH_SHORT).show();
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }
}