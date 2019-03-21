package me.wapy.utils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Antonio Zaitoun on 23/02/2019.
 */
public final class Firebase {
    public static void init() throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream("");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("")
                .build();

        FirebaseApp.initializeApp(options);
    }

    private Firebase(){}
}
