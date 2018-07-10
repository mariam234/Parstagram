package me.mariamdiallo.instagram;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

import me.mariamdiallo.instagram.models.Post;

public class ParseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // register subclass/model
        ParseObject.registerSubclass(Post.class);

        // configure Parse settings/info
        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("parstagram")
                .clientKey(getResources().getString(R.string.master_key))
                .server("http://mariam234-fbu-instagram.herokuapp.com/parse")
                .build();

                // initialize Parse server using specified configurations
                Parse.initialize(configuration);
    }
}
