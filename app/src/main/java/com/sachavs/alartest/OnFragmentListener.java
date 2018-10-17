package com.sachavs.alartest;

import android.widget.ImageView;

import com.sachavs.alartest.fragments.objects.Item;

public interface OnFragmentListener {
    void showSnack(String message);
    void openList(String code);
    void openDetail(Item item);
    void hideKeyboard();
    void loadImageToView(ImageView view, String id, String url);
}
