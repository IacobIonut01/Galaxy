package com.dragons.aurora;

import android.content.Context;

import java.util.Iterator;
import java.util.List;

import com.dragons.aurora.model.Review;

abstract public class ReviewIterator implements Iterator<List<Review>> {

    protected String packageName;
    protected Context context;

    protected int page = -1;

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
