/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package origamify.com.origamitest.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import origamify.com.origamitest.MainActivity;
import origamify.com.origamitest.R;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/*
    *   Diese Activity (bzw. Klasse) legt den in signed_in_layout angezeigten Inhalt fest.
    */

@SuppressWarnings("FieldCanBeLocal")
public class SignedInActivity extends AppCompatActivity {

    private static final String EXTRA_IDP_RESPONSE = "extra_idp_response";
    private static final String EXTRA_SIGNED_IN_CONFIG = "extra_signed_in_config";

    @BindView(android.R.id.content)
    View mRootView;

    @BindView(R.id.user_profile_picture)
    ImageView mUserProfilePicture;

    @BindView(R.id.user_email)
    TextView mUserEmail;

    @BindView(R.id.user_display_name)
    TextView mUserDisplayName;

    @BindView(R.id.user_phone_number)
    TextView mUserPhoneNumber;

    @BindView(R.id.user_enabled_providers)
    TextView mEnabledProviders;

    private IdpResponse mIdpResponse;

    @SuppressWarnings("unused")
    private SignedInConfig mSignedInConfig;

    public static Intent createIntent(
            Context context,
            IdpResponse idpResponse,
            SignedInConfig signedInConfig) {

        Intent startIntent = new Intent();
        if (idpResponse != null) {
            startIntent.putExtra(EXTRA_IDP_RESPONSE, idpResponse);
        }

        return startIntent.setClass(context, SignedInActivity.class)
                .putExtra(EXTRA_SIGNED_IN_CONFIG, signedInConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(AuthUiActivity.createIntent(this));
            finish();
            return;
        }

        mIdpResponse = getIntent().getParcelableExtra(EXTRA_IDP_RESPONSE);
        mSignedInConfig = getIntent().getParcelableExtra(EXTRA_SIGNED_IN_CONFIG);

        setContentView(R.layout.signed_in_layout);
        ButterKnife.bind(this);
        populateProfile();
        try {
            postTokenPLS(getApplicationContext());
        } catch (JSONException e) {
            Log.e("POST","Unable to post Token: "+e.toString());
        }
    }

    public void postTokenPLS(Context context) throws JSONException{

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest sr = new StringRequest(Request.Method.POST,"https://tvdb-rest.herokuapp.com/postToken", new Response.Listener<String>() {
            @Override
            public void onResponse(String response){
                Log.d("POST (token)", response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("POST", error.toString());

            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("token", FirebaseInstanceId.getInstance().getToken());
                try {
                    params.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                } catch (NullPointerException e) {
                    Log.e("postToken failed", e.toString());
                }

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);

    }


    @OnClick(R.id.sign_out)
    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(AuthUiActivity.createIntent(SignedInActivity.this));
                            finish();
                        } else {
                            showSnackbar(R.string.sign_out_failed);
                        }
                    }
                });
    }

    @OnClick(R.id.delete_account)
    public void deleteAccountClicked() {
        try {
            postTokenPLS(getApplicationContext());
        } catch (JSONException e) {
            Log.e("POST","Unable to post Token: "+e.toString());
        }
        this.startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onBackPressed() {
        try {
            postTokenPLS(getApplicationContext());
        } catch (JSONException e) {
            Log.e("POST","Unable to post Token: "+e.toString());
        }
        this.startActivity(new Intent(this, MainActivity.class));
    }

    @MainThread
    private void populateProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            mUserEmail.setText(
                    TextUtils.isEmpty(user.getEmail()) ? "No email" : user.getEmail());
            mUserPhoneNumber.setText(
                    TextUtils.isEmpty(user.getPhoneNumber()) ? "No phone number" : user.getPhoneNumber());
            mUserDisplayName.setText(
                    TextUtils.isEmpty(user.getDisplayName()) ? "No display name" : user.getDisplayName());
        }

        StringBuilder providerList = new StringBuilder(100);

        providerList.append("Providers used: ");

        assert user != null;
        if (user.getProviders() == null || user.getProviders().isEmpty()) {
            providerList.append("none");
        } else {
            Iterator<String> providerIter = user.getProviders().iterator();
            while (providerIter.hasNext()) {
                String provider = providerIter.next();
                switch (provider) {
                    case GoogleAuthProvider.PROVIDER_ID:
                        providerList.append("Google");
                        break;
                    case EmailAuthProvider.PROVIDER_ID:
                        providerList.append("Email");
                        break;
                    case PhoneAuthProvider.PROVIDER_ID:
                        providerList.append("Phone");
                        break;
                    default:
                        throw new IllegalStateException("Unknown provider: " + provider);
                }

                if (providerIter.hasNext()) {
                    providerList.append(", ");
                }
            }
        }

        mEnabledProviders.setText(providerList);
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    static final class SignedInConfig implements Parcelable {
        int logo;
        int theme;
        List<IdpConfig> providerInfo;
        String tosUrl;
        boolean isCredentialSelectorEnabled;
        boolean isHintSelectorEnabled;

        SignedInConfig(int logo,
                       int theme,
                       List<IdpConfig> providerInfo,
                       String tosUrl,
                       boolean isCredentialSelectorEnabled,
                       boolean isHintSelectorEnabled) {
            this.logo = logo;
            this.theme = theme;
            this.providerInfo = providerInfo;
            this.tosUrl = tosUrl;
            this.isCredentialSelectorEnabled = isCredentialSelectorEnabled;
            this.isHintSelectorEnabled = isHintSelectorEnabled;
        }

        SignedInConfig(Parcel in) {
            logo = in.readInt();
            theme = in.readInt();
            providerInfo = new ArrayList<>();
            in.readList(providerInfo, IdpConfig.class.getClassLoader());
            tosUrl = in.readString();
            isCredentialSelectorEnabled = in.readInt() != 0;
            isHintSelectorEnabled = in.readInt() != 0;
        }

        public static final Creator<SignedInConfig> CREATOR = new Creator<SignedInConfig>() {
            @Override
            public SignedInConfig createFromParcel(Parcel in) {
                return new SignedInConfig(in);
            }

            @Override
            public SignedInConfig[] newArray(int size) {
                return new SignedInConfig[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(logo);
            dest.writeInt(theme);
            dest.writeList(providerInfo);
            dest.writeString(tosUrl);
            dest.writeInt(isCredentialSelectorEnabled ? 1 : 0);
            dest.writeInt(isHintSelectorEnabled ? 1 : 0);
        }
    }
}
