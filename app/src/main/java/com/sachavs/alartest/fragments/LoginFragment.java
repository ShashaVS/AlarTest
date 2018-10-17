package com.sachavs.alartest.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.sachavs.alartest.OnFragmentListener;
import com.sachavs.alartest.R;
import com.sachavs.alartest.components.MainViewModel;

public class LoginFragment extends Fragment {
    private String TAG = "LoginFragment";

    private MainViewModel viewModel;
    private OnFragmentListener listener;

    private LinearLayout loginForm;
    private EditText userNameTextView, passwordTextView;
    private Button signInButton;
    private ProgressBar progressBar;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        loginForm = view.findViewById(R.id.login_form);
        userNameTextView = view.findViewById(R.id.username);
        passwordTextView = view.findViewById(R.id.password);
        signInButton = view.findViewById(R.id.sign_in_button);
        progressBar = view.findViewById(R.id.progress);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userNameTextView.getText().toString();
                String password = passwordTextView.getText().toString();

                if(username.isEmpty()) {
                    userNameTextView.requestFocus();
                    userNameTextView.setError(getString(R.string.error_field_required));

                } else if(password.isEmpty()) {
                    passwordTextView.requestFocus();
                    passwordTextView.setError(getString(R.string.error_field_required));

                } else {
                    listener.hideKeyboard();
                    loginForm.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    viewModel.authAsync(username, password);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel.authCode.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String code) {
                if(code != null && !code.isEmpty()) {
                    listener.openList(code);
                } else {
                    loginForm.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    listener.showSnack(getString(R.string.error_status));
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentListener) {
            listener = (OnFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

}
