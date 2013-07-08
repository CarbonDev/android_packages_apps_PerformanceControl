/*
 * Performance Control - An Android CPU Control application Copyright (C) 2012
 * James Roberts
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.brewcrewfoo.performance.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.preference.Preference.OnPreferenceChangeListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.brewcrewfoo.performance.R;
import com.brewcrewfoo.performance.activities.PCSettings;
import com.brewcrewfoo.performance.util.CMDProcessor;
import com.brewcrewfoo.performance.util.Constants;
import com.brewcrewfoo.performance.util.Helpers;

import java.io.File;


public class Tools extends PreferenceFragment implements
        OnSharedPreferenceChangeListener, OnPreferenceChangeListener, Constants {

    private SharedPreferences mPreferences;
    private EditText settingText;
    private Preference mWipe_Cache;
    private String cache_partition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  	    mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.layout.tools);

        mWipe_Cache=(Preference) findPreference(PREF_WIPE_CACHE);
        cache_partition=Helpers.getCachePartition();
        mWipe_Cache.setSummary(getString(R.string.ps_wipe_cache,cache_partition));

        setHasOptionsMenu(true);
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tools_menu, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.app_settings) {
            Intent intent = new Intent(getActivity(), PCSettings.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (key.equals(PREF_SH)) {
            shEditDialog(key,getString(R.string.sh_title));
        }
        else if(key.equals(PREF_WIPE_CACHE)) {
            //---------
            String cancel = getString(R.string.cancel);
            String ok = getString(R.string.yes);
            //-----------------
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.wipe_cache_msg))
                    .setNegativeButton(cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,int which) {
                                    //nothing
                                }
                            })
                    .setPositiveButton(ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,int which) {
                                    final StringBuilder sb = new StringBuilder();
                                    sb.append("busybox rm -rf /cache/dalvik-cache\n");
                                    sb.append("busybox rm -rf /data/dalvik-cache\n");
                                    if(Helpers.binExist("dd") && !cache_partition.equals(NOT_FOUND)){
                                        sb.append("dd if=/dev/zero of="+cache_partition+" \n");
                                    }
                                    sb.append("reboot\n");
                                    Helpers.shExec(sb);
                                }
                            }).create().show();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void shEditDialog(final String key,String title) {
        Resources res = getActivity().getResources();
        String cancel = res.getString(R.string.cancel);
        String ok = res.getString(R.string.ps_volt_save);

        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View alphaDialog = factory.inflate(R.layout.sh_dialog, null);


        settingText = (EditText) alphaDialog.findViewById(R.id.shText);
        settingText.setText(mPreferences.getString(key,""));
        settingText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return true;
            }
        });

        settingText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                   // s.toString();
                } catch (NumberFormatException ex) {
                }
            }
        });
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(alphaDialog)
                .setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        /* nothing */
                    }
                })
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putString(key, settingText.getText().toString()).commit();

                    }
                })
                .create()
                .show();
    }

}
