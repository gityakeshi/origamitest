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
import android.support.annotation.DrawableRes;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.Scopes;
import com.google.firebase.auth.FirebaseAuth;
import origamify.com.origamitest.MainActivity;
import origamify.com.origamitest.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

//import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatDelegate;
//import android.widget.CompoundButton;
//import android.widget.CompoundButton.OnCheckedChangeListener;
//import com.google.firebase.auth.FirebaseUser;
//import butterknife.ButterKnife;

/*
    *   Diese Activity (bzw. Klasse) dient der Koordination des Einlogmechanismuses, d.h. ihre Funktion ist die Festlegung
    *   der für die mit Google, spezifischer unserem Firebase-Projekt, benötigten Parametern, sowie der
    *   Weiterleitung auf andere Activites.
    */

public class AuthUiActivity extends AppCompatActivity {
    private static final String GOOGLE_TOS_URL = "https://www.google.com/policies/terms/";
    private static final String FIREBASE_TOS_URL = "https://firebase.google.com/terms/";
    private static final String GOOGLE_PRIVACY_POLICY_URL = "https://www.google.com/policies/privacy/";
    private static final String FIREBASE_PRIVACY_POLICY_URL = "https://firebase.google.com/terms/analytics/#7_privacy";

    private static final int RC_SIGN_IN = 100;

    boolean true1 = true;
    boolean true2 = true;

    @BindView(R.id.email_provider)
    CheckBox mUseEmailProvider;

    @BindView(R.id.phone_provider)
    CheckBox mUsePhoneProvider;

    @BindView(R.id.google_provider)
    CheckBox mUseGoogleProvider;

    @BindView(R.id.google_tos)
    RadioButton mUseGoogleTos;

    @BindView(R.id.firebase_tos)
    RadioButton mUseFirebaseTos;

    @BindView(R.id.google_privacy)
    RadioButton mUseGooglePrivacyPolicy;

    @BindView(R.id.firebase_privacy)
    RadioButton mUseFirebasePrivacyPolicy;

    @BindView(R.id.sign_in)
    Button mSignIn;

    @BindView(R.id.root)
    View mRootView;

    @BindView(R.id.firebase_logo)
    RadioButton mFirebaseLogo;

    @BindView(R.id.google_logo)
    RadioButton mGoogleLogo;

    @BindView(R.id.no_logo)
    RadioButton mNoLogo;

    @BindView(R.id.credential_selector_enabled)
    CheckBox mEnableCredentialSelector;

    @BindView(R.id.hint_selector_enabled)
    CheckBox mEnableHintSelector;

    @BindView(R.id.allow_new_email_accounts)
    CheckBox mAllowNewEmailAccounts;

    @BindView(R.id.require_name)
    CheckBox mRequireName;

    @BindView(R.id.google_scopes_label)
    TextView mGoogleScopesLabel;

    @BindView(R.id.google_scope_drive_file)
    CheckBox mGoogleScopeDriveFile;

    @BindView(R.id.google_scope_youtube_data)
    CheckBox mGoogleScopeYoutubeData;


    public static Intent createIntent(Context context) {
        return new Intent(context, AuthUiActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isGoogleMisconfigured()) {
            showSnackbar(R.string.configuration_required);
        }
        signIn2();
    }

    @OnClick(R.id.sign_in)
    public void signIn(View view) {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setTheme(getSelectedTheme())
                        .setLogo(getSelectedLogo())
                        .setAvailableProviders(getSelectedProviders())
                        .setTosUrl(getSelectedTosUrl())
                        .setPrivacyPolicyUrl(getSelectedPrivacyPolicyUrl())
                        .setIsSmartLockEnabled(mEnableCredentialSelector.isChecked(),
                                mEnableHintSelector.isChecked())
                        .build(),
                RC_SIGN_IN);
    }

    public void signIn2() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setTheme(R.style.AppTheme)
                        .setLogo(R.drawable.ic_live_tv_black_24dp)
                        .setAvailableProviders(getSelectedProviders2())
                        .setTosUrl(FIREBASE_TOS_URL)
                        .setPrivacyPolicyUrl(FIREBASE_PRIVACY_POLICY_URL)
                        .setIsSmartLockEnabled(true,
                                true)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            return;
        }

        showSnackbar(R.string.unknown_response);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startSignedInActivity(null);
            finish();
        }
    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == RESULT_OK) {
            startSignedInActivity(response);
            finish();
            return;
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
//                showSnackbar(R.string.sign_in_cancelled);
                this.startActivity(new Intent(this, MainActivity.class));

                return;
            }

            if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;
            }

            if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showSnackbar(R.string.unknown_error);
                return;
            }
        }

        showSnackbar(R.string.unknown_sign_in_response);
    }

    private void startSignedInActivity(IdpResponse response) {
        startActivity(
                SignedInActivity.createIntent(
                        this,
                        response,
                        new SignedInActivity.SignedInConfig(
                                getSelectedLogo(),
                                getSelectedTheme(),
                                getSelectedProviders2(),
                                getSelectedTosUrl2(),
                                true1,
                                true2)));
    }


    @MainThread
    @StyleRes
    private int getSelectedTheme() {
        return R.style.AppTheme;
    }

    @MainThread
    @DrawableRes
    private int getSelectedLogo() {
        return R.drawable.ic_live_tv_black_24dp;
    }

    @MainThread
    private List<IdpConfig> getSelectedProviders() {
        List<IdpConfig> selectedProviders = new ArrayList<>();

        if (mUseGoogleProvider.isChecked()) {
            selectedProviders.add(
                    new IdpConfig.GoogleBuilder().setScopes(getGoogleScopes()).build());
        }


        if (mUseEmailProvider.isChecked()) {
            selectedProviders.add(new IdpConfig.EmailBuilder()
                    .setRequireName(mRequireName.isChecked())
                    .setAllowNewAccounts(mAllowNewEmailAccounts.isChecked())
                    .build());
        }

        if (mUsePhoneProvider.isChecked()) {
            selectedProviders.add(new IdpConfig.PhoneBuilder().build());
        }

        return selectedProviders;
    }

    @MainThread
    private List<IdpConfig> getSelectedProviders2() {
        List<IdpConfig> selectedProviders = new ArrayList<>();

        selectedProviders.add(
                new IdpConfig.GoogleBuilder().setScopes(getGoogleScopes2()).build());


        selectedProviders.add(new IdpConfig.EmailBuilder()
                .setRequireName(true)
                .setAllowNewAccounts(true)
                .build());

        selectedProviders.add(new IdpConfig.PhoneBuilder().build());

        return selectedProviders;
    }

    @MainThread
    private String getSelectedTosUrl() {
        if (mUseGoogleTos.isChecked()) {
            return GOOGLE_TOS_URL;
        }

        return FIREBASE_TOS_URL;
    }

    @MainThread
    private String getSelectedTosUrl2() {
            return GOOGLE_TOS_URL;
    }

    @MainThread
    private String getSelectedPrivacyPolicyUrl() {
        if (mUseGooglePrivacyPolicy.isChecked()) {
            return GOOGLE_PRIVACY_POLICY_URL;
        }

        return FIREBASE_PRIVACY_POLICY_URL;
    }

    @MainThread
    private boolean isGoogleMisconfigured() {
        return AuthUI.UNCONFIGURED_CONFIG_VALUE.equals(getString(R.string.default_web_client_id));
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    @MainThread
    private List<String> getGoogleScopes() {
        List<String> result = new ArrayList<>();
        if (mGoogleScopeYoutubeData.isChecked()) {
            result.add("https://www.googleapis.com/auth/youtube.readonly");
        }
        if (mGoogleScopeDriveFile.isChecked()) {
            result.add(Scopes.DRIVE_FILE);
        }
        return result;
    }

    @MainThread
    private List<String> getGoogleScopes2() {
        List<String> result = new ArrayList<>();
        result.add("https://www.googleapis.com/auth/youtube.readonly");
        result.add(Scopes.DRIVE_FILE);
        return result;
    }
}
