package com.dragons.aurora.fragment.preference;

import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;

import java.util.HashMap;
import java.util.Map;

import com.dragons.aurora.MultiSelectListPreference;
import com.dragons.aurora.fragment.PreferenceFragment;
import com.dragons.aurora.R;
import com.dragons.aurora.Util;
import com.dragons.aurora.model.App;
import com.dragons.aurora.task.InstalledAppsTask;

public class Blacklist extends Abstract {

    private ListPreference blackOrWhite;
    private MultiSelectListPreference appList;
    private CheckBoxPreference autoWhitelist;

    public Blacklist(PreferenceFragment activity) {
        super(activity);
    }

    public void setBlackOrWhite(ListPreference blackOrWhite) {
        this.blackOrWhite = blackOrWhite;
    }

    public void setAppList(MultiSelectListPreference appList) {
        this.appList = appList;
    }

    public void setAutoWhitelist(CheckBoxPreference autoWhitelist) {
        this.autoWhitelist = autoWhitelist;
    }

    @Override
    public void draw() {
        AppListTask task = new AppListTask(appList);
        task.setIncludeSystemApps(true);
        task.execute();

        Preference.OnPreferenceChangeListener listener = new BlackListOnPreferenceChangeListener(appList, autoWhitelist);
        blackOrWhite.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(blackOrWhite, blackOrWhite.getValue());
    }

    static class BlackListOnPreferenceChangeListener implements Preference.OnPreferenceChangeListener {

        private MultiSelectListPreference appList;
        private CheckBoxPreference autoWhitelist;

        public BlackListOnPreferenceChangeListener(MultiSelectListPreference appList, CheckBoxPreference autoWhitelist) {
            this.appList = appList;
            this.autoWhitelist = autoWhitelist;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String value = (String) newValue;
            boolean isBlackList = null != value && value.equals(PreferenceFragment.LIST_BLACK);
            appList.setTitle(appList.getContext().getString(
                    isBlackList
                            ? R.string.pref_update_list_black
                            : R.string.pref_update_list_white
            ));
            appList.setDialogTitle(appList.getTitle());
            preference.setSummary(appList.getContext().getString(
                    isBlackList
                            ? R.string.pref_update_list_white_or_black_black
                            : R.string.pref_update_list_white_or_black_white
            ));
            autoWhitelist.setEnabled(!isBlackList);
            return true;
        }
    }

    static class AppListTask extends InstalledAppsTask {

        private MultiSelectListPreference appList;
        private Map<String, String> appNames;

        public AppListTask(MultiSelectListPreference appList) {
            this.appList = appList;
            setContext(appList.getContext());
        }

        @Override
        protected void onPreExecute() {
            appList.setEntries(new String[0]);
            appList.setEntryValues(new String[0]);
        }

        @Override
        protected void onPostExecute(Map<String, App> installedApps) {
            int count = appNames.size();
            appList.setEntries(appNames.values().toArray(new String[count]));
            appList.setEntryValues(appNames.keySet().toArray(new String[count]));
        }

        @Override
        protected Map<String, App> doInBackground(String... strings) {
            Map<String, App> installedApps = super.doInBackground(strings);
            Map<String, String> appNames = new HashMap<>();
            for (String packageName : installedApps.keySet()) {
                appNames.put(packageName, installedApps.get(packageName).getDisplayName());
            }
            this.appNames = Util.sort(appNames);
            return installedApps;
        }
    }
}
