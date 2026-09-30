package com.atlas.assistant;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.window.OnBackInvokedDispatcher;
import android.provider.CalendarContract;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.ComponentActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.core.app.ActivityCompat;
import androidx.webkit.WebViewAssetLoader;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends ComponentActivity {
    private static final int REQ_MEDIA = 1001;
    private static final int REQ_FILE_CHOOSER = 1003;

    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private CredentialManager credentialManager;
    private FirebaseAuth firebaseAuth;

    private final ActivityResultLauncher<String[]> permissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                // Permissions are requested again only when a feature explicitly needs them.
            }
    );

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNativeFirebase();
        credentialManager = CredentialManager.create(this);

        webView = new WebView(this);
        configureWebView();
        setContentView(webView);
        requestRuntimePermissions();
        createNotificationChannel();
        notifyWebAuthState();
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::handleBackPress);
        }
        webView.loadUrl("https://appassets.androidplatform.net/assets/public/index.html");
    }

    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        if (Build.VERSION.SDK_INT >= 21) s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();
        webView.setWebViewClient(new WebViewClient() {
            @Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                String scheme = u.getScheme() == null ? "" : u.getScheme();
                String host = u.getHost() == null ? "" : u.getHost();
                if ("https".equalsIgnoreCase(scheme)
                        && "appassets.androidplatform.net".equalsIgnoreCase(host)) {
                    return false;
                }
                try {
                    MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, u));
                } catch (Exception ignored) {}
                return true;
            }
        });
        webView.addJavascriptInterface(new AtlasBridge(this), "ATLASNative");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    boolean ok = true;
                    for (String r : request.getResources()) {
                        if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r)
                                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ok = false;
                        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r)
                                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) ok = false;
                    }
                    if (ok) request.grant(request.getResources()); else request.deny();
                });
            }

            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                try {
                    Intent intent = params.createIntent();
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE);
                    startActivityForResult(intent, REQ_FILE_CHOOSER);
                    return true;
                } catch (ActivityNotFoundException e) {
                    filePathCallback = null;
                    return false;
                }
            }
        });
    }

    private void initNativeFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                String apiKey = BuildConfig.ATLAS_FIREBASE_API_KEY;
                String appId = BuildConfig.ATLAS_FIREBASE_APP_ID;
                String projectId = BuildConfig.ATLAS_FIREBASE_PROJECT_ID;
                String storageBucket = BuildConfig.ATLAS_FIREBASE_STORAGE_BUCKET;
                String senderId = BuildConfig.ATLAS_FIREBASE_MESSAGING_SENDER_ID;
                if (isBlank(apiKey) || isBlank(appId) || isBlank(projectId)) return;
                FirebaseOptions.Builder b = new FirebaseOptions.Builder()
                        .setApiKey(apiKey)
                        .setApplicationId(appId)
                        .setProjectId(projectId);
                if (!isBlank(storageBucket)) b.setStorageBucket(storageBucket);
                if (!isBlank(senderId)) b.setGcmSenderId(senderId);
                FirebaseApp.initializeApp(this, b.build());
            }
            firebaseAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            firebaseAuth = null;
        }
    }

    private boolean hasNativeGoogleConfig() {
        return firebaseAuth != null && !isBlank(BuildConfig.ATLAS_GOOGLE_SERVER_CLIENT_ID);
    }

    @JavascriptInterface
    private void sendJs(final String function, final String payload) {
        runOnUiThread(() -> {
            if (webView == null) return;
            String safe = JSONObject.quote(payload == null ? "" : payload);
            webView.evaluateJavascript("window[" + JSONObject.quote(function) + "]&&window[" + JSONObject.quote(function) + "](" + safe + ");", null);
        });
    }

    private void notifyWebAuthState() {
        if (webView == null) return;
        runOnUiThread(() -> {
            try {
                JSONObject root = new JSONObject();
                if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
                    com.google.firebase.auth.FirebaseUser u = firebaseAuth.getCurrentUser();
                    JSONObject user = new JSONObject();
                    user.put("uid", u.getUid());
                    user.put("displayName", u.getDisplayName());
                    user.put("email", u.getEmail());
                    user.put("photoURL", u.getPhotoUrl() == null ? "" : u.getPhotoUrl().toString());
                    root.put("user", user);
                } else root.put("user", JSONObject.NULL);
                webView.evaluateJavascript("window.atlasNativeAuthState&&window.atlasNativeAuthState(" + JSONObject.quote(root.toString()) + ");", null);
            } catch (Exception ignored) {}
        });
    }

    private void beginGoogleCredentialFlow() {
        if (!hasNativeGoogleConfig()) {
            sendGoogleResult(false, null, "Google Login ainda não foi configurado no build Android.", "CONFIG_MISSING");
            return;
        }
        try {
            GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                    .setServerClientId(BuildConfig.ATLAS_GOOGLE_SERVER_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build();
            GetCredentialRequest request = new GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build();
            credentialManager.getCredentialAsync(
                    this,
                    request,
                    null,
                    getMainExecutor(),
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override public void onResult(GetCredentialResponse response) {
                            handleCredential(response.getCredential());
                        }
                        @Override public void onError(GetCredentialException e) {
                            sendGoogleResult(false, null, e.getMessage(), "CREDENTIAL_ERROR");
                        }
                    });
        } catch (Exception e) {
            sendGoogleResult(false, null, e.getMessage(), "CREDENTIAL_START_ERROR");
        }
    }

    private void handleCredential(Credential credential) {
        if (!(credential instanceof CustomCredential)) {
            sendGoogleResult(false, null, "Credencial Google não suportada.", "UNSUPPORTED_CREDENTIAL");
            return;
        }
        CustomCredential custom = (CustomCredential) credential;
        if (!GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(custom.getType())) {
            sendGoogleResult(false, null, "Tipo de credencial Google inesperado.", "UNEXPECTED_CREDENTIAL");
            return;
        }
        try {
            GoogleIdTokenCredential google = GoogleIdTokenCredential.createFrom(custom.getData());
            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(google.getIdToken(), null);
            firebaseAuth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener(authResult -> {
                        com.google.firebase.auth.FirebaseUser u = authResult.getUser();
                        JSONObject user = new JSONObject();
                        try {
                            user.put("uid", u.getUid());
                            user.put("displayName", u.getDisplayName());
                            user.put("email", u.getEmail());
                            user.put("photoURL", u.getPhotoUrl() == null ? "" : u.getPhotoUrl().toString());
                        } catch (Exception ignored) {}
                        sendGoogleResult(true, user.toString(), null, null);
                    })
                    .addOnFailureListener(e -> sendGoogleResult(false, null, e.getMessage(), "FIREBASE_AUTH_ERROR"));
        } catch (GoogleIdTokenParsingException e) {
            sendGoogleResult(false, null, e.getMessage(), "GOOGLE_TOKEN_ERROR");
        }
    }

    private void sendGoogleResult(boolean ok, String userJson, String message, String code) {
        try {
            JSONObject root = new JSONObject();
            root.put("ok", ok);
            if (userJson != null) root.put("user", new JSONObject(userJson));
            if (message != null) root.put("message", message);
            if (code != null) root.put("code", code);
            String json = JSONObject.quote(root.toString());
            runOnUiThread(() -> webView.evaluateJavascript("window.atlasNativeGoogleResult&&window.atlasNativeGoogleResult(" + json + ");", null));
        } catch (Exception ignored) {}
    }

    private void requestRuntimePermissions() {
        ArrayList<String> p = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) p.add(Manifest.permission.CAMERA);
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) p.add(Manifest.permission.RECORD_AUDIO);
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) p.add(Manifest.permission.POST_NOTIFICATIONS);
        if (!p.isEmpty()) permissionsLauncher.launch(p.toArray(new String[0]));
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel("atlas_reminders", "ATLAS Lembretes", NotificationManager.IMPORTANCE_HIGH);
            c.setDescription("Lembretes e compromissos do ATLAS");
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    public static class AtlasBridge {
        private final MainActivity a;
        AtlasBridge(MainActivity a){this.a=a;}
        @JavascriptInterface public void share(String text){ Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,text);a.startActivity(Intent.createChooser(i,"Compartilhar com")); }
        @JavascriptInterface public void openUrl(String url){try{a.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));}catch(Exception ignored){}}
        @JavascriptInterface public void dial(String number){try{a.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(number))));}catch(Exception ignored){}}
        @JavascriptInterface public void message(String number,String body){try{Intent i=new Intent(Intent.ACTION_SENDTO,Uri.parse("smsto:"+Uri.encode(number)));i.putExtra("sms_body",body);a.startActivity(i);}catch(Exception ignored){}}
        @JavascriptInterface public void addCalendar(String title,String dateTime,String notes,long durationMinutes){try{SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX",Locale.US);long st=f.parse(dateTime).getTime(),en=st+Math.max(1,durationMinutes)*60000L;Intent i=new Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI).putExtra(CalendarContract.Events.TITLE,title).putExtra(CalendarContract.Events.DESCRIPTION,notes).putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,st).putExtra(CalendarContract.EXTRA_EVENT_END_TIME,en);a.startActivity(i);}catch(Exception ignored){}}
        @JavascriptInterface public void scheduleReminder(String id,String title,String body,long triggerAt){try{AlarmManager am=(AlarmManager)a.getSystemService(Context.ALARM_SERVICE);Intent i=new Intent(a,ReminderReceiver.class).putExtra("title",title).putExtra("body",body);int code=Math.abs(id.hashCode());PendingIntent pi=PendingIntent.getBroadcast(a,code,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);if(Build.VERSION.SDK_INT>=23)am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,triggerAt,pi);else am.set(AlarmManager.RTC_WAKEUP,triggerAt,pi);}catch(Exception ignored){}}
        @JavascriptInterface public void cancelReminder(String id){try{AlarmManager am=(AlarmManager)a.getSystemService(Context.ALARM_SERVICE);Intent i=new Intent(a,ReminderReceiver.class);int code=Math.abs(id.hashCode());PendingIntent pi=PendingIntent.getBroadcast(a,code,i,PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE);if(pi!=null){am.cancel(pi);pi.cancel();}}catch(Exception ignored){}}
        @JavascriptInterface public void googleSignIn(){a.beginGoogleCredentialFlow();}
        @JavascriptInterface public void googleSignOut(){try{if(a.firebaseAuth!=null)a.firebaseAuth.signOut();a.credentialManager.clearCredentialStateAsync(new androidx.credentials.ClearCredentialStateRequest(), new CancellationSignal(), Runnable::run, new androidx.credentials.CredentialManagerCallback<Void, androidx.credentials.exceptions.ClearCredentialException>() { public void onResult(Void r) {} public void onError(androidx.credentials.exceptions.ClearCredentialException e) {} });}catch(Exception ignored){}a.runOnUiThread(a::notifyWebAuthState);}
        @JavascriptInterface public String getCurrentGoogleUser(){try{if(a.firebaseAuth==null||a.firebaseAuth.getCurrentUser()==null)return "";com.google.firebase.auth.FirebaseUser u=a.firebaseAuth.getCurrentUser();JSONObject root=new JSONObject(),user=new JSONObject();user.put("uid",u.getUid());user.put("displayName",u.getDisplayName());user.put("email",u.getEmail());user.put("photoURL",u.getPhotoUrl()==null?"":u.getPhotoUrl().toString());root.put("user",user);return root.toString();}catch(Exception e){return "";}}
        @JavascriptInterface public void deleteGoogleAccount(){try{if(a.firebaseAuth!=null && a.firebaseAuth.getCurrentUser()!=null)a.firebaseAuth.getCurrentUser().delete();}catch(Exception ignored){}}
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode!=REQ_FILE_CHOOSER)return;if(filePathCallback==null)return;Uri[] results=null;if(resultCode==RESULT_OK&&data!=null){if(data.getClipData()!=null){int n=data.getClipData().getItemCount();results=new Uri[n];for(int i=0;i<n;i++)results[i]=data.getClipData().getItemAt(i).getUri();}else if(data.getData()!=null)results=new Uri[]{data.getData()};}filePathCallback.onReceiveValue(results);filePathCallback=null;}

    private void handleBackPress() {
        if (webView != null) {
            webView.evaluateJavascript("window.atlasHandleBack?String(window.atlasHandleBack()):'false';", value -> {
                if (!"\"true\"".equals(value)) finish();
            });
        } else {
            finish();
        }
    }

    @Override public void onBackPressed(){
        if (Build.VERSION.SDK_INT < 33) {
            handleBackPress();
        } else {
            super.onBackPressed();
        }
    }
}
