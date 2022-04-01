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

package ch.epfl.sdp.healthplay.auth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.auth.UserInfo;

import ch.epfl.sdp.healthplay.HomeScreenActivity;
import ch.epfl.sdp.healthplay.R;
import ch.epfl.sdp.healthplay.databinding.SignedInLayoutBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import static com.firebase.ui.auth.AuthUI.EMAIL_LINK_PROVIDER;

public class SignedInActivity extends AppCompatActivity {
    private static final String TAG = "SignedInActivity";
    public static final String IDP_RESPONSE = "extra_idp_response";
    private SignedInLayoutBinding mBinding;



    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IdpResponse response = getIntent().getParcelableExtra(IDP_RESPONSE);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(AuthUiActivity.createIntent(this));
            finish();
            return;
        }
            mBinding = SignedInLayoutBinding.inflate(getLayoutInflater());
            setContentView(mBinding.getRoot());
            populateProfile(response);
            populateIdpToken(response);

            mBinding.deleteAccount.setOnClickListener(view -> deleteAccountClicked());
            mBinding.signOut.setOnClickListener(view -> signOut());


    }

    public void onClickNight(View view) {
        setMode(AppCompatDelegate.MODE_NIGHT_YES);
        setTheme(R.style.darkTheme);
    }

    public void onClickLight(View view) {
        setMode(AppCompatDelegate.MODE_NIGHT_NO);
        setTheme(R.style.AppTheme);
    }

    public static void SetMode(Context activity) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        int mode = sharedPref.getInt(activity.getString(R.string.saved_night_mode), AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(mode);
        if(mode == AppCompatDelegate.MODE_NIGHT_YES) {
            activity.setTheme(R.style.darkTheme);
        }
        else{
            activity.setTheme(R.style.AppTheme);
        }
    }


    private void setMode(int mode) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.saved_night_mode), mode);
        editor.apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this,HomeScreenActivity.class));
                        finish();
                        return;
                    } else {
                        Log.w(TAG, "signOut:failure", task.getException());
                        showSnackbar(R.string.sign_out_failed);
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this,HomeScreenActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void deleteAccountClicked() {
        new MaterialAlertDialogBuilder(this)
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes, nuke it!", (dialogInterface, i) -> deleteAccount())
                .setNegativeButton("No", null)
                .show();
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void deleteAccount() {
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this,HomeScreenActivity.class));
                        finish();
                    } else {
                        showSnackbar(R.string.delete_account_failed);
                    }
                });
    }

    private void populateProfile(@Nullable IdpResponse response) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mBinding.userEmail.setText(
                TextUtils.isEmpty(user.getEmail()) ? "No email" : user.getEmail());
        mBinding.userPhoneNumber.setText(
                TextUtils.isEmpty(user.getPhoneNumber()) ? "No phone number" : user.getPhoneNumber());
        mBinding.userDisplayName.setText(
                TextUtils.isEmpty(user.getDisplayName()) ? "No display name" : user.getDisplayName());

        if (response == null) {
            mBinding.userIsNew.setVisibility(View.GONE);
        } else {
            mBinding.userIsNew.setVisibility(View.VISIBLE);
            mBinding.userIsNew.setText(response.isNewUser() ? "New user" : "Existing user");
        }

        List<String> providers = new ArrayList<>();
        if (user.getProviderData().isEmpty()) {
            providers.add(getString(R.string.providers_anonymous));
        } else {
            for (UserInfo info : user.getProviderData()) {
                switch (info.getProviderId()) {
                    case GoogleAuthProvider.PROVIDER_ID:
                        providers.add(getString(R.string.providers_google));
                        break;
                    case FacebookAuthProvider.PROVIDER_ID:
                        providers.add(getString(R.string.providers_facebook));
                        break;
                    case TwitterAuthProvider.PROVIDER_ID:
                        providers.add(getString(R.string.providers_twitter));
                        break;
                    case EmailAuthProvider.PROVIDER_ID:
                        providers.add(getString(R.string.providers_email));
                        break;
                    case PhoneAuthProvider.PROVIDER_ID:
                        providers.add(getString(R.string.providers_phone));
                        break;
                    case EMAIL_LINK_PROVIDER:
                        providers.add(getString(R.string.providers_email_link));
                        break;
                    case FirebaseAuthProvider.PROVIDER_ID:
                        // Ignore this provider, it's not very meaningful
                        break;
                    default:
                        providers.add(info.getProviderId());
                }
            }
        }

        mBinding.userEnabledProviders.setText(getString(R.string.used_providers, providers));
    }

    private void populateIdpToken(@Nullable IdpResponse response) {
        String token = null;
        String secret = null;
        if (response != null) {
            token = response.getIdpToken();
            secret = response.getIdpSecret();
        }

        View idpTokenLayout = findViewById(R.id.idp_token_layout);
        if (token == null) {
            idpTokenLayout.setVisibility(View.GONE);
        } else {
            idpTokenLayout.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.idp_token)).setText(token);
        }

        View idpSecretLayout = findViewById(R.id.idp_secret_layout);
        if (secret == null) {
            idpSecretLayout.setVisibility(View.GONE);
        } else {
            idpSecretLayout.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.idp_secret)).setText(secret);
        }
    }

    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mBinding.getRoot(), errorMessageRes, Snackbar.LENGTH_LONG).show();
    }
}
