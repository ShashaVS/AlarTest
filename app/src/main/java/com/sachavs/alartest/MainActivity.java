package com.sachavs.alartest;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.sachavs.alartest.components.MainViewModel;
import com.sachavs.alartest.fragments.DetailFragment;
import com.sachavs.alartest.fragments.ListFragment;
import com.sachavs.alartest.fragments.LoginFragment;
import com.sachavs.alartest.fragments.objects.Item;

public class MainActivity extends AppCompatActivity implements OnFragmentListener {
    private String TAG = "MainActivity";

    private String currentFragmentTag;
    private FrameLayout layout;
    private MainViewModel viewModel;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        layout = findViewById(R.id.container);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);

        if(savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString("currentFragmentTag");
        }
        if(currentFragmentTag == null) {
            currentFragmentTag = "LoginFragment";
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new LoginFragment())
                    .commit();
        }
        updateTitle();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentFragmentTag", currentFragmentTag);
    }

    private void updateTitle() {
        switch (currentFragmentTag) {
            case "LoginFragment":
                actionBar.setTitle(R.string.log_in);
                break;
            case "ListFragment":
                actionBar.setTitle(R.string.data_list);
                actionBar.setDisplayHomeAsUpEnabled(false);
                break;
            case "DetailFragment":
                actionBar.setTitle(R.string.detail);
                actionBar.setDisplayHomeAsUpEnabled(true);
                break;
        }
    }

    @Override
    public void showSnack(String message) {
        Snackbar.make(layout, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void openList(String code) {
        currentFragmentTag = "ListFragment";
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ListFragment.newInstance(code))
                .commit();
        updateTitle();
    }

    @Override
    public void openDetail(Item item) {
        currentFragmentTag = "DetailFragment";
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, DetailFragment.newInstance(item))
                .addToBackStack(null)
                .commit();
        updateTitle();
    }

    @Override
    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if(inputManager != null && view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void loadImageToView(ImageView view, String id, String url) {
        viewModel.loadImageAsync(view, id, url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(currentFragmentTag.equals("DetailFragment")) {
            currentFragmentTag = "ListFragment";
            updateTitle();
        }
    }
}
