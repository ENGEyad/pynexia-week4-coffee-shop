package com.pynexia.week4.common;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONObject;

public final class FirebaseBridge {
    private FirebaseBridge() { }

    public static FirebaseApp initialize(Context context) {
        if (!FirebaseApp.getApps(context).isEmpty()) return FirebaseApp.getInstance();
        try (InputStream input = context.getAssets().open("google-services.json")) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            if (output.size() == 0) return null;
            JSONObject root = new JSONObject(output.toString("UTF-8"));
            JSONObject project = root.getJSONObject("project_info");
            JSONArray clients = root.getJSONArray("client");
            JSONObject selected = null;
            String packageName = context.getPackageName();
            for (int i = 0; i < clients.length(); i++) {
                JSONObject client = clients.getJSONObject(i);
                String clientPackage = client.getJSONObject("client_info").getJSONObject("android_client_info").optString("package_name");
                if (packageName.equals(clientPackage)) { selected = client; break; }
            }
            if (selected == null) return null;
            String appId = selected.getJSONObject("client_info").getString("mobilesdk_app_id");
            String apiKey = selected.getJSONArray("api_key").getJSONObject(0).getString("current_key");
            FirebaseOptions.Builder options = new FirebaseOptions.Builder()
                .setApplicationId(appId)
                .setApiKey(apiKey)
                .setProjectId(project.getString("project_id"))
                .setGcmSenderId(project.getString("project_number"));
            String databaseUrl = project.optString("firebase_url", "");
            if (!databaseUrl.isEmpty()) options.setDatabaseUrl(databaseUrl);
            return FirebaseApp.initializeApp(context, options.build());
        } catch (Exception ignored) {
            return null;
        }
    }
}
