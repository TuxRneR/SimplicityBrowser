/**
 * Copyright 2016 Soren Stoutner <soren@stoutner.com>.
 *
 * This file is part of Privacy Browser <https://www.stoutner.com/privacy-browser>.
 *
 * Privacy Browser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Privacy Browser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Privacy Browser.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.creativetrends.simplicity.app;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

public class SettingsFragment extends PreferenceFragment {
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesListener;
    private SharedPreferences savedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final Preference homepagePreference = findPreference("homepage");

        // Initialize savedPreferences.
        savedPreferences = getPreferenceScreen().getSharedPreferences();

        // Set the homepage URL as the summary text for the Homepage preference when the preference screen is loaded.  The default is "https://www.duckduckgo.com".
        homepagePreference.setSummary(savedPreferences.getString("homepage", "https://www.duckduckgo.com"));

        // Listen for preference changes.
        preferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {



                switch (key) {
                    case "javascript_enabled":

                        SimplicityActivity.javaScriptEnabled = sharedPreferences.getBoolean("javascript_enabled", false);


                        SimplicityActivity.mainWebView.getSettings().setJavaScriptEnabled(SimplicityActivity.javaScriptEnabled);
                        SimplicityActivity.mainWebView.reload();


                        return;

                    case "first_party_cookies_enabled":
                        // Set firstPartyCookiesEnabled to the new state.  The default is false.
                        SimplicityActivity.firstPartyCookiesEnabled = sharedPreferences.getBoolean("first_party_cookies_enabled", false);


                        SimplicityActivity.cookieManager.setAcceptCookie(SimplicityActivity.firstPartyCookiesEnabled);
                        SimplicityActivity.mainWebView.reload();


                        return;

                    case "third_party_cookies_enabled":
                        // Set thirdPartyCookiesEnabled to the new state.  The default is false.
                        SimplicityActivity.thirdPartyCookiesEnabled = sharedPreferences.getBoolean("third_party_cookies_enabled", false);



                        // Update mainWebView and reload the website if API >= 21.
                        if (Build.VERSION.SDK_INT >= 21) {
                            SimplicityActivity.cookieManager.setAcceptThirdPartyCookies(SimplicityActivity.mainWebView, SimplicityActivity.thirdPartyCookiesEnabled);
                            SimplicityActivity.mainWebView.reload();
                        }
                        return;

                    case "dom_storage_enabled":
                        // Set domStorageEnabled to the new state.  The default is false.
                        SimplicityActivity.domStorageEnabled = sharedPreferences.getBoolean("dom_storage_enabled", false);

                        // Update the checkbox in the options menu.
                        MenuItem domStorageMenuItem = SimplicityActivity.mainMenu.findItem(R.id.toggleDomStorage);
                        domStorageMenuItem.setChecked(SimplicityActivity.domStorageEnabled);

                        // Update mainWebView and reload the website.
                        SimplicityActivity.mainWebView.getSettings().setDomStorageEnabled(SimplicityActivity.domStorageEnabled);
                        SimplicityActivity.mainWebView.reload();


                        return;

                    case "homepage":
                        // Set the new homepage URL as the summary text for the Homepage preference.  The default is "https://www.duckduckgo.com".
                        homepagePreference.setSummary(sharedPreferences.getString("homepage", "https://www.duckduckgo.com"));

                        // Update the homepage variable.  The default is "https://www.duckduckgo.com".
                        SimplicityActivity.homepage = sharedPreferences.getString("homepage", "https://www.duckduckgo.com");
                        return;

                    // If no match, do nothing.
                    default:
                }
            }
        };

        // Register the listener.
        savedPreferences.registerOnSharedPreferenceChangeListener(preferencesListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        savedPreferences.registerOnSharedPreferenceChangeListener(preferencesListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        savedPreferences.unregisterOnSharedPreferenceChangeListener(preferencesListener);
    }
}
