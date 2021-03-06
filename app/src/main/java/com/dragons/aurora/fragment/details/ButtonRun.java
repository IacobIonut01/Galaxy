package com.dragons.aurora.fragment.details;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.dragons.aurora.AuroraApplication;
import com.dragons.aurora.R;
import com.dragons.aurora.activities.AuroraActivity;
import com.dragons.aurora.model.App;

public class ButtonRun extends Button {

    ButtonRun(AuroraActivity activity, App app) {
        super(activity, app);
    }

    @Override
    protected android.widget.Button getButton() {
        if (activity.findViewById(R.id.download).getVisibility() == View.VISIBLE)
            return null;
        else
            return (android.widget.Button) activity.findViewById(R.id.run);
    }

    @Override
    protected boolean shouldBeVisible() {
        return isInstalled() && null != getLaunchIntent();
    }

    @Override
    protected void onButtonClick(View v) {
        Intent i = getLaunchIntent();
        if (null != i) {
            try {
                activity.startActivity(i);
            } catch (ActivityNotFoundException e) {
                Log.e(getClass().getName(), "getLaunchIntentForPackage returned an intent, but starting the activity failed for " + app.getPackageName());
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Intent getLaunchIntent() {
        Intent i = activity.getPackageManager().getLaunchIntentForPackage(app.getPackageName());
        boolean isTv = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && ((AuroraApplication) activity.getApplication()).isTv();
        if (isTv) {
            Intent l = activity.getPackageManager().getLeanbackLaunchIntentForPackage(app.getPackageName());
            if (null != l) {
                i = l;
            }
        }
        if (i == null) {
            return null;
        }
        i.addCategory(isTv ? Intent.CATEGORY_LEANBACK_LAUNCHER : Intent.CATEGORY_LAUNCHER);
        return i;
    }
}
