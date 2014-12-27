package com.cellasoft.univrapp.gcm;

import android.content.Context;
import com.cellasoft.univrapp.Config;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.utils.StreamUtils;
import com.google.android.gcm.GCMRegistrar;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import static com.cellasoft.univrapp.utils.LogUtils.*;

public final class ServerUtilities {
    private static final String TAG = makeLogTag("GCM");
    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    /**
     * Register this account/device pair within the server.
     */
    public static boolean register(final Context context, String dest,
                                   final String regId) {
        LOGI(TAG, "registering device (regId = " + regId + ")");
        String serverUrl = Config.Links.GCM + "/register.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        params.put("dest", dest);

        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
        // Once GCM returns a registration id, we need to register on our server
        // As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            LOGD(TAG, "Attempt #" + i + " to register");
            try {
                post(serverUrl, params);
                GCMRegistrar.setRegisteredOnServer(context, true);
                Settings.setRegistrationId(GCMRegistrar
                        .getRegistrationId(context));
                return true;
            } catch (IOException e) {
                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                LOGE(TAG, "Failed to register on attempt " + i, e);
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    LOGD(TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    LOGD(TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return false;
                }
                // increase backoff exponentially
                backoff *= 2;
            }
        }
        return false;
    }

    /**
     * Unregister this account/device pair within the server.
     */
    public static void unregister(final Context context, final String regId) {
        LOGI(TAG, "unregistering device (regId = " + regId + ")");
        String serverUrl = Config.Links.GCM + "/unregister.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        try {
            post(serverUrl, params); // Unregister for UnivrApp server
            GCMRegistrar.setRegisteredOnServer(context, false);
            Settings.setRegistrationId("Not Registered");
        } catch (IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
            LOGD(TAG, "Unable to unregister from application server", e);
            String message = context.getString(
                    R.string.server_unregister_error, e.getMessage());
            Config.displayMessage(context, message);
        }
    }

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params   request parameters.
     * @throws IOException propagated from POST.
     */
    private static void post(String endpoint, Map<String, String> params)
            throws IOException {
        StreamUtils.disableConnectionReuseIfNecessary();

        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        LOGV(TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            LOGE("URL", "> " + url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);

            StreamUtils.closeQuietly(out);
            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }
            // String response = StreamUtils.readAllText(conn.getInputStream(),
            // "UTF-8");
            // LOGI(TAG, response);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            params.clear();
            params = null;
        }
    }
}
