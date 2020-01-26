package com.mimik.smarthome;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mimik.edgeappauth.EdgeAppAuth;
import com.mimik.edgeappauth.authobject.AuthConfig;
import com.mimik.edgeappauth.authobject.AuthResponse;
import com.mimik.edgeappcommon.EdgeRequestStatus;
import com.mimik.edgeappops.EdgeAppOps;
import com.mimik.edgeappops.edgeservice.EdgeConfig;
import com.mimik.edgeappops.microserviceobjects.MicroserviceDeploymentConfig;
import com.mimik.edgeappops.microserviceobjects.MicroserviceDeploymentStatus;
import com.mimik.smarthome.edgeSDK.IdToken;
import com.mimik.smarthome.edgeSDK.MdsProvider;
import com.mimik.smarthome.userinterface.homePanel.HPIdle;
import com.mimik.smarthome.userinterface.visitorPanel.VPIdle;
import com.mimik.smarthome.userinterface.visitorPanel.VSplashScreen;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.TokenRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class StartupActivity extends AppCompatActivity {
    private static final String TAG = "StartupActivity";

    private final String MIMIK_LOGIN_ACTION = "com.mimik.smarthome.HANDLE_AUTHORIZATION_FINISHED";
    private final String MIMIK_BACKENDTOKEN_ACTION = "com.mimik.smarthome.HANDLE_BACKENDTOKEN_FINISHED";
    private final String MIMIK_UNASSOCIATE_ACTION = "com.mimik.smarthome.HANDLE_UNASSOCIATION_FINISHED";
    // Callback extras
    public static final String INTENT_EXTRA_ACCESSTOKEN = "accessToken";
    public static final String INTENT_EXTRA_ACCOUNTID = "accountId";

    private final String REDIRECT_URI = "com.mimik.smarthome://oauth2callback";

    // OAuth endpoints
    public static final String TOKEN_ENDPOINT =   BuildConfig.MID_ROOT_URI + "/token";
    public static final String AUTH_ENDPOINT = BuildConfig.MID_ROOT_URI + "/auth";
    public static final String GRANT_TYPE = "exchange_edge_token";

    // User access tokens
    private String mEdgeAccessToken;
    private String mUserAccessToken;

    private List<AsyncTask> mTaskList;

    EdgeAppOps mAppOps;
    ProgressBar startupProgress;
    ImageView loggedIn;
    ImageView superDriveMS;
    ImageView msgMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            Log.d(TAG, "onCreate");
            setContentView(R.layout.activity_startup);

            startupProgress = findViewById(R.id.startupProgress);
            loggedIn = findViewById(R.id.logged_in_state);
            superDriveMS = findViewById(R.id.superdrive_state);
            msgMS = findViewById(R.id.message_state);


            EdgeConfig config = new EdgeConfig();
            mAppOps = new EdgeAppOps(this, config);
            mTaskList = new ArrayList<>();

            new LocalDeviceIdTask().execute();
            if (hasToken()) {
                loggedIn.setImageResource(R.drawable.ic_check_box_selected_24dp);
            } else {
                loggedIn.setImageResource(R.drawable.ic_check_box_empty_24dp);
                doAuthLogin();
            }

        }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.d(TAG, "onResume");
//
//        Intent intent = getIntent();
//        logIntent(TAG, intent);
//        if (intent != null) {
//            if (intent.getExtras() == null) {
//                if (hasToken()) {
//                    loggedIn.setImageResource(R.drawable.ic_check_box_selected_24dp);
//                } else {
//                    loggedIn.setImageResource(R.drawable.ic_check_box_empty_24dp);
//                    doAuthLogin();
//                }
//            }
//        }
//    }

    // Handles incoming intents
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        logIntent(TAG, intent);
        if (intent != null) {
            if (intent.getAction() != null) {
                String action = intent.getAction();
                if (action.equals(MIMIK_LOGIN_ACTION)) {
                    // Login intent
                    handleIntentLogin(intent);
                } else if (action.equals((MIMIK_BACKENDTOKEN_ACTION))) {
                    // token exchange intent
                    handleIntentTokenExchange(intent);
                } else if (action.equals(MIMIK_UNASSOCIATE_ACTION)) {
                    // Edge unassociation intent
                    handleIntentUnassociateAction(intent);
                }
            }
        }
    }

    private void handleIntentLogin(Intent intent) {
        // Login intent
        EdgeRequestStatus<AuthResponse> requestStatus = EdgeRequestStatus.fromIntent(intent, AuthResponse.class);
        if (requestStatus.response != null
                && requestStatus.response.getAccessToken() != null
                && !requestStatus.response.getAccessToken().isEmpty()) {
            // Successful, store access tokens
            mEdgeAccessToken = requestStatus.response.getAccessToken();
            Log.d(TAG, "handleIntentLogin: " + mEdgeAccessToken);
            toast(getResources().getString(R.string.toast_login));

            Log.d(TAG, "need to get backend access token now");
            Intent postAuthorizationIntent = new Intent(this, this.getClass());
            postAuthorizationIntent.setAction(MIMIK_BACKENDTOKEN_ACTION);
            postAuthorizationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent postIntent = PendingIntent.getActivity(this, 1, postAuthorizationIntent, 0);
            getBackendToken(requestStatus.response, postIntent);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dockContainer();
                }
            }, 1000);
        } else {
            // Failed
            Log.d(TAG, "handleIntentLogin Error: " + requestStatus.error.getErrorMessage() );
            toast(getResources().getString(R.string.toast_failed_login));
        }
    }

    private void handleIntentTokenExchange(Intent intent) {
        // Token Exchange intent
        if (intent.getExtras() != null) {
            mUserAccessToken = intent.getStringExtra(INTENT_EXTRA_ACCESSTOKEN);
            Log.d(TAG, "handleIntentTokenExchange: " + mUserAccessToken);
        } else {
            Log.d(TAG, "handleIntentTokenExchange Error " );
            toast(getResources().getString(R.string.toast_failed_login));
        }
    }

    public void getBackendToken(AuthResponse authResponse, PendingIntent postIntent) {
        Log.d(TAG, "getBackendToken()");
        AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse(AUTH_ENDPOINT) /* auth endpoint */,
                Uri.parse(TOKEN_ENDPOINT) /* token endpoint */
        );
        TokenRequest.Builder builder = new TokenRequest.Builder(serviceConfiguration, BuildConfig.CLIENT_ID);
        builder.setGrantType(GRANT_TYPE);
        Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put("token", authResponse.getAccessToken());
//        additionalParams.put("edge_id_token", getEdgeIdToken());
        builder.setAdditionalParameters(additionalParams);
        TokenRequest tr = builder.build();
        AppAuthConfiguration.Builder authBuilder = new AppAuthConfiguration.Builder();
//            builder.setConnectionBuilder(ConnectionBuilderForTesting.INSTANCE);
        AuthorizationService authService = new AuthorizationService(this, authBuilder.build());
        authService.performTokenRequest(tr, (tokenResponse, exception) -> {
            if (exception != null) {
                Log.d(TAG, "Edge Token Exchange failed", exception);
                // TODO: Handle error
            } else {
                if (tokenResponse != null) {
                    Intent completedIntent = new Intent();
                    completedIntent.putExtra(INTENT_EXTRA_ACCESSTOKEN, tokenResponse.accessToken);
                    completedIntent.putExtra(INTENT_EXTRA_ACCOUNTID, getAccountIdFromIdToken(authResponse.getIdToken()));
//                    saveTokens(context, authResponse.getAccessToken(), tokenResponse.accessToken);
                    try {
                        postIntent.send(this, 0, completedIntent);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void handleIntentUnassociateAction(Intent intent) {
        EdgeRequestStatus<AuthResponse> authStatus = EdgeRequestStatus.fromIntent(intent, AuthResponse.class);
        if (authStatus.response != null
                && authStatus.response.getAccessToken() != null
                && !authStatus.response.getAccessToken().isEmpty()) {
            // Successful
            toast(getResources().getString(R.string.toast_unassociate));
        } else {
            // Failed
            Log.d(TAG, "handleIntentUnassociateAction: " + authStatus.error.getErrorMessage() );
            toast(getResources().getString(R.string.toast_failed_unassociate));
        }
    }

    private void dockContainer() {
        if (hasToken()) {
            loggedIn.setImageResource(R.drawable.ic_check_box_selected_24dp);

            Log.d(TAG, "Install super-drive");
            McmAddSuperDriveContainerTask taskSD = new McmAddSuperDriveContainerTask();
            mTaskList.add(taskSD);
            taskSD.execute();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Install message");
                    McmAddMessageContainerTask taskMSG = new McmAddMessageContainerTask();
                    mTaskList.add(taskMSG);
                    taskMSG.execute();
                }
            }, 1000);
        } else {
            loggedIn.setImageResource(R.drawable.ic_check_box_empty_24dp);
            doAuthLogin();
        }

    }

    private void doAuthLogin() {
        Log.d(TAG, "doAuthLogin");
        AuthConfig config = new AuthConfig();
        config.setClientId(BuildConfig.CLIENT_ID);
        config.setRedirectUri(Uri.parse(REDIRECT_URI));
        Intent postAuthorizationIntent = new Intent(this, StartupActivity.class);
        postAuthorizationIntent.putExtra("END_POINT", BuildConfig.MID_ROOT_URI);
        postAuthorizationIntent.putExtra("CLIENT_ID", BuildConfig.CLIENT_ID);
        postAuthorizationIntent.setAction(MIMIK_LOGIN_ACTION);
        postAuthorizationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, postAuthorizationIntent, 0);
        Log.d(TAG, "doAuthLogin");
        EdgeAppAuth.authorize(this, config, pendingIntent);
    }

    private boolean hasToken() {
        Log.d(TAG, "hasToken");
        return mEdgeAccessToken != null && !mEdgeAccessToken.isEmpty() && mUserAccessToken != null && !mUserAccessToken.isEmpty();
    }

    // Toast message display
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    // Perform work to add the superdrive container to the mimik container manager (docker)
    private class McmAddSuperDriveContainerTask extends AsyncTask<Void, Void, MicroserviceDeploymentStatus> {
        @Override
        protected MicroserviceDeploymentStatus doInBackground(final Void... voids) {
            Log.d(TAG, "McmAddSuperDriveContainerTask");
            MicroserviceDeploymentConfig config = new MicroserviceDeploymentConfig();
            config.setName("superdrive-v1");
            config.setFilename("superdrive.tar");
            config.setResourceStream(getResources().openRawResource(R.raw.superdrive));
            config.setApiRootUri(Uri.parse("/superdrive/v1"));
            Map<String, String> env = new HashMap<>();
            env.put("MCM.WEBSOCKET_SUPPORT", "true");
            env.put("uMDS", "http://127.0.0.1:8083/mds/v1");
            config.setEnvVariables(env);
            return mAppOps.deployEdgeMicroservice(mEdgeAccessToken, config);
        }

        @Override
        protected void onPostExecute(final MicroserviceDeploymentStatus ret) {
            Log.d(TAG, "McmAddSuperDriveContainerTask - onPostExecute");
            if (ret.response != null) {
                toast(getResources().getString(R.string.toast_superdrive_mcm));
                superDriveMS.setImageResource(R.drawable.ic_check_box_selected_24dp);
            } else {
                toast(getResources().getString(R.string.toast_failed_mcm));
            }
        }
    }

    // Perform work to add the message container to the mimik container manager (docker)
    private class McmAddMessageContainerTask extends AsyncTask<Void, Void, MicroserviceDeploymentStatus> {
        @Override
        protected MicroserviceDeploymentStatus doInBackground(final Void... voids) {
            Log.d(TAG, "McmAddMessageContainerTask");
            MicroserviceDeploymentConfig config = new MicroserviceDeploymentConfig();
            config.setName("msg-v1");
            config.setFilename("msg.tar");
            config.setResourceStream(getResources().openRawResource(R.raw.msg));
            config.setApiRootUri(Uri.parse("/msg/v1"));
            Map<String, String> env = new HashMap<>();
            env.put("MCM.WEBSOCKET_SUPPORT", "true");
            config.setEnvVariables(env);
            return mAppOps.deployEdgeMicroservice(mEdgeAccessToken, config);
        }

        @Override
        protected void onPostExecute(final MicroserviceDeploymentStatus ret) {
                  Log.d(TAG, "McmAddMessageContainerTask - onPostExecute");
            if (ret.response != null) {
                toast(getResources().getString(R.string.toast_message_mcm));
                msgMS.setImageResource(R.drawable.ic_check_box_selected_24dp);

                startupProgress.setVisibility(View.INVISIBLE);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Navigating to main activity");

                        if(BuildConfig.FLAVOR.contains("THomePanel")){
                            Intent intent = new Intent(StartupActivity.this, HPIdle.class);

                            intent.putExtra("edgeToken", mEdgeAccessToken);
                            intent.putExtra("userToken", mUserAccessToken);

                            startActivity(intent);
                        }
                        else if(BuildConfig.FLAVOR.contains("TVisitorPanel")) {

                            Intent intent = new Intent(StartupActivity.this, VPIdle.class);

                            intent.putExtra("edgeToken", mEdgeAccessToken);
                            intent.putExtra("userToken", mUserAccessToken);

                            startActivity(intent);
                        }
                        else{

                            Intent intent = new Intent(StartupActivity.this, MainActivity.class);

                            intent.putExtra("edgeToken", mEdgeAccessToken);
                            intent.putExtra("userToken", mUserAccessToken);


                            startActivity(intent);

                        }
                    }
                }, 1750);
            } else {
                toast(getResources().getString(R.string.toast_failed_mcm));
            }
        }
    }

    private class LocalDeviceIdTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            MdsProvider.instance().requestDeviceId();
            return null;
        }
    }

    public void logIntent(String tag, Intent intent) {
        if (intent != null) {
            Log.d(tag, "action " + intent.getAction());
            Log.d(tag, "data " + intent.getData());
            Log.d(tag, "datastring " + intent.getDataString());
            Log.d(tag, "scheme " + intent.getScheme());
            Log.d(tag, "type " + intent.getType());
            Log.d(tag, "package " + intent.getPackage());
            if (intent.getExtras() != null) {
                for (String key : intent.getExtras().keySet()) {
                    Log.d(tag, "extra(" + key + ") "
                            + intent.getExtras().get(key));
                }
            }
        }
    }

    private static String getAccountIdFromIdToken(String idToken) {
        if (idToken == null) { return ""; }
        String[] split = idToken.split(Pattern.quote("."));
        if (split.length < 2) {
            return "";
        }
        String body = new String(Base64.decode(split[1], Base64.DEFAULT));
        Gson gson = new Gson();
        IdToken token = gson.fromJson(body, IdToken.class);
        return token.sub;
    }
}
