package com.sachavs.alartest.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.sachavs.alartest.OnFragmentListener;
import com.sachavs.alartest.R;
import com.sachavs.alartest.components.MainViewModel;
import com.sachavs.alartest.fragments.adapter.ListAdapter;

public class ListFragment extends Fragment {
    private String TAG = "ListFragment";

    private static final String ARG_CODE = "authCode";
    private String code;
    private MainViewModel viewModel;
    private OnFragmentListener listener;

    private View view;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ListAdapter adapter;
    private int numPage = 0;

    public ListFragment() { }

    public static ListFragment newInstance(String code) {
        ListFragment fragment = new ListFragment();

        Bundle args = new Bundle();
        args.putString(ARG_CODE, code);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            code = savedInstanceState.getString("code");
            numPage = savedInstanceState.getInt("numPage");
        }
        if (code == null && getArguments() != null) {
            code = getArguments().getString(ARG_CODE);
        }
        viewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        if(numPage == 0) {
            viewModel.getListAsync(code, String.valueOf(numPage+1));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(view == null) {
            view = inflater.inflate(R.layout.fragment_list, container, false);

            recyclerView = view.findViewById(R.id.list);
            progressBar = view.findViewById(R.id.progress);

            adapter = new ListAdapter(viewModel.getItems(), listener);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if(!recyclerView.canScrollVertically(1)) {
                        viewModel.getListAsync(code, String.valueOf(numPage+1));
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }
            });

            if(viewModel.isLoading()) {
                progressBar.setVisibility(View.VISIBLE);
            }

        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel.totalPages.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String page) {
                if(page != null) {
                    if(page.equals("-1")) {
                        listener.showSnack(getString(R.string.wrong_data));
                        numPage++;
                    } else {
                        try {
                            int newNumPage = Integer.valueOf(page);
                            if(numPage != newNumPage) {
                                numPage = newNumPage;
                                adapter.notifyDataSetChanged();
                            }
                        } catch (NumberFormatException e) {
                            numPage = 0;
                        }
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("code", code);
        outState.putInt("numPage", numPage);
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
