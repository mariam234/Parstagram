package me.mariamdiallo.instagram.Fragment;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import me.mariamdiallo.instagram.Activity.MainActivity;
import me.mariamdiallo.instagram.PostAdapter;
import me.mariamdiallo.instagram.R;
import me.mariamdiallo.instagram.models.Post;

public class HomeFragment extends Fragment {

    PostAdapter postAdapter;
    ArrayList<Post> posts;

    ProgressBar pbProgressBar;
    public RecyclerView rvPosts;
    SwipeRefreshLayout swipeContainer;

    Activity activity;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // find the RecyclerView
        rvPosts  = view.findViewById(R.id.rvPosts);
        // init the arraylist (data source)
        posts = new ArrayList<>();
        // construct the adapter from this datasource
        postAdapter = new PostAdapter(posts);
        // RecyclerView setup (layout manager, use adapter)
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        // set the adapter
        rvPosts.setAdapter(postAdapter);

        pbProgressBar = view.findViewById(R.id.pbProgressBar);

        // Setup refresh listener which triggers new data loading
        swipeContainer = view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTopPosts(false);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // load posts
        loadTopPosts(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        activity = getActivity();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // loading posts
    public void loadTopPosts(boolean withProgressBar) {
        if (withProgressBar) {
            showProgressBar();
        }

        final Post.Query postQuery = new Post.Query();
        postQuery.getTop().withUser().orderByAscending("createdAt");

        postQuery.findInBackground(new FindCallback<Post>() {

            @Override
            public void done(List<Post> postsList, ParseException e) {
                hideProgressBar();

                if (e == null) {
                    // clear out old items
                    postAdapter.clear();

                    for (int i = 0; i < postsList.size(); i++) {
                        posts.add(0, postsList.get(i));
                        postAdapter.notifyItemInserted(0);
                    }
                }

                else {
                    e.printStackTrace();
                }

                // call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }
        });
    }

    void showProgressBar() {
        pbProgressBar.setVisibility(View.VISIBLE);
            rvPosts.setVisibility(View.GONE);
    }

    void hideProgressBar () {
        pbProgressBar.setVisibility(View.GONE);
        rvPosts.setVisibility(View.VISIBLE);
    }
}
