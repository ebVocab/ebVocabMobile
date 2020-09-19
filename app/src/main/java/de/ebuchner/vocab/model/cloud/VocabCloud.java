package de.ebuchner.vocab.model.cloud;

import android.app.Activity;
import android.content.SharedPreferences;
import de.ebuchner.vocab.mobile.platform.MobileUIPlatform;

public class VocabCloud {

    private static final String SHARED_PREFERENCES_NAME = "de.ebuchner.vocab.model.cloud";
    private static final String CLOUD_USER = "user";
    private static final String CLOUD_SECRET = "secret";
    private static final String CLOUD_SERVER = "server";

    private VocabCloud() {

    }

    public static void saveAuthenticationInput(String server, String user, String secret) {
        Activity currentActivity = MobileUIPlatform.instance().getMobileNuiDirector().getCurrentActivity();
        SharedPreferences.Editor editor = currentActivity.getSharedPreferences(
                SHARED_PREFERENCES_NAME,
                Activity.MODE_PRIVATE
        ).edit();


        if (server == null || server.trim().length() == 0)
            editor.remove(CLOUD_SERVER);
        else
            editor.putString(
                    CLOUD_SERVER,
                    server
            );

        if (user == null || user.trim().length() == 0)
            editor.remove(CLOUD_USER);
        else
            editor.putString(
                    CLOUD_USER,
                    user
            );

        if (secret == null || secret.trim().length() == 0)
            editor.remove(CLOUD_SECRET);
        else
            editor.putString(
                    CLOUD_SECRET,
                    secret
            );

        editor.apply();
    }

    public static AuthenticationInput restoreAuthenticationInput() {
        Activity currentActivity = MobileUIPlatform.instance().getMobileNuiDirector().getCurrentActivity();
        SharedPreferences cloudPreferences =
                currentActivity.getSharedPreferences(
                        SHARED_PREFERENCES_NAME,
                        Activity.MODE_PRIVATE
                );
        String user = cloudPreferences.getString(CLOUD_USER, "");
        String secret = cloudPreferences.getString(CLOUD_SECRET, "");
        String server = cloudPreferences.getString(CLOUD_SERVER, "");

        return new AuthenticationInput(server, user, secret);
    }

    public static class AuthenticationInput {
        private final String user;
        private final String secret;
        private final String server;

        public AuthenticationInput(String server, String user, String secret) {
            this.server = server;
            this.user = user;
            this.secret = secret;
        }

        public String getUser() {
            return user;
        }

        public String getSecret() {
            return secret;
        }

        public String getServer() {
            return server;
        }
    }
}
