package com.dragons.aurora;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;

import com.dragons.aurora.fragment.UpdatableAppsFragment;

public class UpdateAllReceiver extends BroadcastReceiver {

    static public final String ACTION_ALL_UPDATES_COMPLETE = "ACTION_ALL_UPDATES_COMPLETE";
    static public final String ACTION_APP_UPDATE_COMPLETE = "ACTION_APP_UPDATE_COMPLETE";

    static public final String EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME";
    static public final String EXTRA_UPDATE_ACTUALLY_INSTALLED = "EXTRA_UPDATE_ACTUALLY_INSTALLED";

    private UpdatableAppsFragment fragment;

    public UpdateAllReceiver(UpdatableAppsFragment fragment) {
        this.fragment = fragment;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ALL_UPDATES_COMPLETE);
        filter.addAction(ACTION_APP_UPDATE_COMPLETE);
        fragment.getActivity().registerReceiver(this, filter);
        if (!((AuroraApplication) fragment.getActivity().getApplication()).isBackgroundUpdating()) {
            enableButton();
        }
        fragment.getActivity().unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ContextUtil.isAlive(fragment.getActivity()) || TextUtils.isEmpty(intent.getAction())) {
            return;
        }
        if (intent.getAction().equals(ACTION_ALL_UPDATES_COMPLETE)) {
            ((AuroraApplication) fragment.getActivity().getApplication()).setBackgroundUpdating(false);
            enableButton();
        } else if (intent.getAction().equals(ACTION_APP_UPDATE_COMPLETE)) {
            processAppUpdate(
                    intent.getStringExtra(EXTRA_PACKAGE_NAME),
                    intent.getBooleanExtra(EXTRA_UPDATE_ACTUALLY_INSTALLED, false)
            );
        }
    }

    private void enableButton() {
        Button button = (Button) fragment.getActivity().findViewById(R.id.update_all);
        TextView textView = (TextView) fragment.getActivity().findViewById(R.id.updates_txt);
        if (null != button) {
            button.setEnabled(true);
            textView.setEnabled(true);
            button.setText(R.string.list_update_all);
            textView.setText(R.string.list_update_chk_txt);
        }
    }

    private void processAppUpdate(String packageName, boolean installedUpdate) {
        if (installedUpdate) {
            fragment.removeApp(packageName);
        }
    }
}
