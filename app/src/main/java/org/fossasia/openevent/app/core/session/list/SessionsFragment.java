package org.fossasia.openevent.app.core.session.list;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.openevent.app.R;
import org.fossasia.openevent.app.common.mvp.view.BaseFragment;
import org.fossasia.openevent.app.core.main.MainActivity;
import org.fossasia.openevent.app.data.ContextUtils;
import org.fossasia.openevent.app.data.session.Session;
import org.fossasia.openevent.app.databinding.SessionsFragmentBinding;
import org.fossasia.openevent.app.ui.ViewUtils;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;

public class SessionsFragment extends BaseFragment<SessionsPresenter> implements SessionsView {

    private Context context;
    private long trackId;

    @Inject
    ContextUtils utilModel;

    @Inject
    Lazy<SessionsPresenter> sessionsPresenter;

    private SessionsAdapter sessionsAdapter;
    private SessionsFragmentBinding binding;
    private SwipeRefreshLayout refreshLayout;

    private boolean initialized;

    public static SessionsFragment newInstance(long trackId) {
        SessionsFragment fragment = new SessionsFragment();
        Bundle args = new Bundle();
        args.putLong(MainActivity.EVENT_KEY, trackId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getContext();

        if (getArguments() != null)
            trackId = getArguments().getLong(MainActivity.EVENT_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.sessions_fragment, container, false);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        setupRecyclerView();
        setupRefreshListener();
        getPresenter().attach(trackId, this);
        getPresenter().start();

        initialized = true;
    }

    @Override
    protected int getTitle() {
        return R.string.session;
    }

    @Override
    public void onStop() {
        super.onStop();
        refreshLayout.setOnRefreshListener(null);
    }

    private void setupRecyclerView() {
        if (!initialized) {
            sessionsAdapter = new SessionsAdapter(getPresenter());

            RecyclerView recyclerView = binding.sessionsRecyclerView;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(sessionsAdapter);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
    }

    private void setupRefreshListener() {
        refreshLayout = binding.swipeContainer;
        refreshLayout.setColorSchemeColors(utilModel.getResourceColor(R.color.color_accent));
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            getPresenter().loadSessions(true);
        });
    }

    @Override
    public Lazy<SessionsPresenter> getPresenterProvider() {
        return sessionsPresenter;
    }

    @Override
    public void showError(String error) {
        ViewUtils.showSnackbar(binding.getRoot(), error);
    }

    @Override
    public void showProgress(boolean show) {
        ViewUtils.showView(binding.progressBar, show);
    }

    @Override
    public void onRefreshComplete(boolean success) {
        if (success)
            ViewUtils.showSnackbar(binding.sessionsRecyclerView, R.string.refresh_complete);
    }

    @Override
    public void showResults(List<Session> items) {
        sessionsAdapter.notifyDataSetChanged();
    }

    @Override
    public void showEmptyView(boolean show) {
        ViewUtils.showView(binding.emptyView, show);
    }
}
