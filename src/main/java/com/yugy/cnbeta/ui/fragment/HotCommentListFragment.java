package com.yugy.cnbeta.ui.fragment;

import android.app.ListFragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.yugy.cnbeta.model.HotCommentModel;
import com.yugy.cnbeta.sdk.Cnbeta;
import com.yugy.cnbeta.ui.activity.NewsActivity;
import com.yugy.cnbeta.ui.adapter.HotCommentListAdapter;
import com.yugy.cnbeta.ui.listener.ListViewScrollObserver;
import com.yugy.cnbeta.ui.view.AppMsg;
import com.yugy.cnbeta.utils.ScreenUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by yugy on 14-1-9.
 */
public class HotCommentListFragment extends ListFragment{

    private String mFromCommentId = "0";

    private PullToRefreshLayout mPullToRefreshLayout;
    private ListViewScrollObserver mListViewScrollObserver;
    private HotCommentListAdapter mAdapter;

    public HotCommentListFragment(){
        mListViewScrollObserver = new ListViewScrollObserver();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewGroup viewGroup = (ViewGroup) view;
        mPullToRefreshLayout = new PullToRefreshLayout(getActivity());
        ActionBarPullToRefresh.from(getActivity())
                .insertLayoutInto(viewGroup)
                .setup(mPullToRefreshLayout);

        getListView().setOnScrollListener(mListViewScrollObserver);
        getListView().setBackgroundColor(Color.WHITE);
        getListView().setOverScrollMode(View.OVER_SCROLL_NEVER);
        getListView().setPadding(0, ScreenUtils.dp(getActivity(), 48), 0, 0);
        getListView().setClipToPadding(false);
        getListView().setDividerHeight(1);

        mAdapter = new HotCommentListAdapter(this);

        getData();
    }

    public void setOnScrollUpAndDownListener(ListViewScrollObserver.OnListViewScrollListener listener){
        mListViewScrollObserver.setOnScrollUpAndDownListener(listener);
    }

    public void getData(){
        mPullToRefreshLayout.setRefreshing(true);
        setEmptyText("正在加载...");
        Cnbeta.getHotComment(getActivity(), mFromCommentId,
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray jsonArray) {
                    try {
                        ArrayList<HotCommentModel> models = getModels(jsonArray);
                        if(mAdapter.getModels().size() == 0){
                            mAdapter.getModels().addAll(models);
                            setListAdapter(mAdapter);
                        }else{
                            mAdapter.getModels().addAll(models);
                            mAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        AppMsg.makeText(getActivity(), "数据解析失败", AppMsg.STYLE_ALERT).show();
                        e.printStackTrace();
                    }
                    mPullToRefreshLayout.setRefreshComplete();
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    AppMsg.makeText(getActivity(), "刷新失败", AppMsg.STYLE_ALERT).show();
                    setEmptyText("刷新失败，请尝试重新打开cnβ");
                    volleyError.printStackTrace();
                    mPullToRefreshLayout.setRefreshComplete();
                }
            }
        );
    }

    private ArrayList<HotCommentModel> getModels(JSONArray jsonArray) throws JSONException{
        ArrayList<HotCommentModel> models = new ArrayList<HotCommentModel>();
        for(int i = 0; i < jsonArray.length(); i++){
            HotCommentModel model = new HotCommentModel();
            model.parse(jsonArray.getJSONObject(i));
            models.add(model);
            if(i == jsonArray.length() - 1){
                mFromCommentId = model.id;
            }
        }
        return models;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(getActivity(), NewsActivity.class);
        intent.putExtra("data", mAdapter.getModels().get(position));
        intent.putExtra("hotComment", true);
        startActivity(intent);
    }
}
