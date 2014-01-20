package com.yugy.cnbeta.ui.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuItem;

import com.umeng.analytics.MobclickAgent;
import com.yugy.cnbeta.R;
import com.yugy.cnbeta.network.RequestManager;
import com.yugy.cnbeta.ui.activity.swipeback.SwipeBackActivity;
import com.yugy.cnbeta.ui.fragment.CommentFragment;
import com.yugy.cnbeta.utils.ScreenUtils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by yugy on 14-1-8.
 */
public class CommentActivity extends SwipeBackActivity{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityBase.onCreate(this);
        setContentView(R.layout.activity_comment);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        CommentFragment commentFragment = new CommentFragment();
        Bundle args = getIntent().getBundleExtra("bundle");
        commentFragment.setArguments(args);
        getFragmentManager().beginTransaction().add(R.id.comment_container, commentFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityBase.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ActivityBase.onPause(this);
    }

    @Override
    protected void onDestroy() {
        ActivityBase.onDestroy(this);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.activity_out);
    }
}
