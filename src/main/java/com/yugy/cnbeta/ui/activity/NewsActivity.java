package com.yugy.cnbeta.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.yugy.cnbeta.R;
import com.yugy.cnbeta.model.HotCommentModel;
import com.yugy.cnbeta.model.NewsListModel;
import com.yugy.cnbeta.model.TopTenNewsModel;
import com.yugy.cnbeta.network.RequestManager;
import com.yugy.cnbeta.sdk.Cnbeta;
import com.yugy.cnbeta.ui.activity.swipeback.SwipeBackActivity;
import com.yugy.cnbeta.ui.view.RefreshActionItem;
import com.yugy.cnbeta.ui.view.SelectorImageView;
import com.yugy.cnbeta.utils.MessageUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.view.View.inflate;
import static com.yugy.cnbeta.ui.view.RefreshActionItem.RefreshActionListener;

/**
 * Created by yugy on 14-1-7.
 */
public class NewsActivity extends SwipeBackActivity implements RefreshActionListener{

    private String mId;
    private String mTitleString;
    private String mCommentCountString;

    private LinearLayout mContainer;
    private ImageView mHeaderImage;
    private TextView mTitle;
    private TextView mCommentCount;
    private TextView mTime;
    private View mCommentFooter;
    private Button mFooterButton;
    private RefreshActionItem mRefreshActionItem;
    private ShareActionProvider mShareActionProvider;

    private int mImageMarginBottom;
    private LayoutParams mImageLayoutParams;
    private ArrayList<String> mImgUrls = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onError(this);
        FadingActionBarHelper helper = new FadingActionBarHelper()
                .actionBarBackground(R.drawable.ab_solid_bg)
                .headerLayout(R.layout.view_news_header)
                .contentLayout(R.layout.activity_news);
        setContentView(helper.createView(this));
        helper.initActionBar(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);

        mImageMarginBottom = (int) getResources().getDimension(R.dimen.news_image_margin_bottom);
        mImageLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mImageLayoutParams.setMargins(0, 0, 0, mImageMarginBottom);

        mContainer = (LinearLayout) findViewById(R.id.news_container);
        mHeaderImage = (ImageView) findViewById(R.id.news_header_image);
        mTitle = (TextView) findViewById(R.id.news_header_title);
        mCommentCount = (TextView) findViewById(R.id.news_header_comment_count);
        mTime = (TextView) findViewById(R.id.news_header_time);
        mCommentFooter = inflate(this, R.layout.view_comment_footer, null);
        mFooterButton = (Button) mCommentFooter.findViewById(R.id.comment_footer_button);

        if(getIntent().hasExtra("top10")){
            TopTenNewsModel data = getIntent().getParcelableExtra("data");
            mId = data.id;
            mTitleString = data.title;
            mCommentCountString = data.commentCount;
            mTime.setText(data.readCount + "次阅读");
        }else if(getIntent().hasExtra("hotComment")){
            HotCommentModel data = getIntent().getParcelableExtra("data");
            mId = data.articleId;
            mTitleString = data.articleTitle;
            mCommentCountString = data.commentCount;
            mTime.setText("“" + data.comment + "”");
        }else{
            NewsListModel data = getIntent().getParcelableExtra("data");
            mId = data.id;
            mTitleString = data.title;
            mCommentCountString = data.commentCount;

            mTime.setText(data.time);
            TextView summary = getNewTextView();
            summary.setText(data.summary);
            mContainer.addView(summary);
        }

        mTitle.setText(mTitleString);
        mCommentCount.setText(mCommentCountString);
        mFooterButton.setText("查看评论(" + mCommentCountString + ")");
        mFooterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewsActivity.this, CommentActivity.class);
                intent.putExtra("id", mId);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_in, 0);
            }
        });

        getArticleData();
    }

    private void getArticleData(){
        Cnbeta.getNewsContent(this, mId,
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray jsonArray) {
                    mContainer.removeAllViews();
                    mImgUrls.clear();
                    try {
                        for (int i = 0; i < jsonArray.length(); i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if(jsonObject.getString("type").equals("text")){
                                TextView textView = getNewTextView();
                                textView.setText(Html.fromHtml(jsonObject.getString("value")));
                                mContainer.addView(textView);
                            }else if(jsonObject.getString("type").equals("img")){
                                SelectorImageView imageView = getNewImageView();
                                final String imgUrl = jsonObject.getString("value");
                                if(mImgUrls.size() == 0){
                                    ImageLoader.getInstance().displayImage(imgUrl, mHeaderImage);
                                }
                                mImgUrls.add(imgUrl);
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(NewsActivity.this, PicActivity.class);
                                        intent.putExtra("list", mImgUrls);
                                        intent.putExtra("current", mImgUrls.indexOf(imgUrl));
                                        intent.putExtra("title", mTitleString);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.activity_in, 0);
                                    }
                                });
                                ImageLoader.getInstance().displayImage(imgUrl, imageView);
                                mContainer.addView(imageView);
                            }
                        }
                        mContainer.addView(mCommentFooter);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    mRefreshActionItem.setRefreshing(false);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    volleyError.printStackTrace();
                    MessageUtils.toast(NewsActivity.this, "获取数据失败");
                    if(mRefreshActionItem != null){
                        mRefreshActionItem.setRefreshing(false);
                    }
                }
            }
        );
    }

    private void setShareIntent(Intent shareIntent){
        if(mShareActionProvider != null){
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private TextView getNewTextView(){
        TextView textView = new TextView(this);
        textView.setAutoLinkMask(Linkify.ALL);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setLineSpacing(0, 1.4f);
        return textView;
    }

    private SelectorImageView getNewImageView(){
        SelectorImageView imageView = new SelectorImageView(this);
        imageView.setAdjustViewBounds(true);
        imageView.setLayoutParams(mImageLayoutParams);
        return imageView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news, menu);

        MenuItem shareItem = menu.findItem(R.id.news_action_share);
        mShareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
        StringBuilder shareText = new StringBuilder("《");
        shareText.append(mTitleString);
        shareText.append("》 ");
        shareText.append("http://www.cnbeta.com/articles/");
        shareText.append(mId);
        shareText.append(".htm");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        shareIntent.setType("text/plain");
        setShareIntent(shareIntent);

        MenuItem refreshItem = menu.findItem(R.id.news_action_refresh);
        mRefreshActionItem = (RefreshActionItem) refreshItem.getActionView();
        mRefreshActionItem.setMenuItem(refreshItem);
        mRefreshActionItem.setRefreshActionListener(this);
        mRefreshActionItem.setRefreshing(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if(NavUtils.shouldUpRecreateTask(this, upIntent)){
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                }else{
                    upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            case R.id.news_action_website:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://m.cnbeta.com/view_" + mId + ".htm"));
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefreshButtonClick(RefreshActionItem sender) {
        mRefreshActionItem.setRefreshing(true);
        getArticleData();
    }

    @Override
    protected void onDestroy() {
        RequestManager.getInstance().cancelRequests(this);
        super.onDestroy();
    }

    @Override
     protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.activity_out);
    }
}
