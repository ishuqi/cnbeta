package com.yugy.cnbeta.network;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Environment;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.yugy.cnbeta.Application;
import com.yugy.cnbeta.R;

/**
 * Created by yugy on 14-1-6.
 */
public class RequestManager {

    private final int MEM_CACHE_SIZE;

    private static RequestManager sInstance;
    private RequestQueue sRequestQueue;
    private ImageLoader sImageLoader;
    private DiskBasedCache sDiskBasedCache;

    private RequestManager(){
        sDiskBasedCache = new DiskBasedCache(Application.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        sRequestQueue = new RequestQueue(sDiskBasedCache, new BasicNetwork(new HurlStack()));
        sRequestQueue.start();
        MEM_CACHE_SIZE = 1024 * 1024 *
                ((ActivityManager) Application.getContext().getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() / 3;
        sImageLoader = new ImageLoader(sRequestQueue, new BitmapLruCache(MEM_CACHE_SIZE));
    }

    public static ImageLoader getImageLoader() {
        if(sInstance == null){
            sInstance = new RequestManager();
        }
        return sInstance.sImageLoader;
    }

    public static RequestManager getInstance(){
        if(sInstance == null){
            sInstance = new RequestManager();
        }
        return sInstance;
    }

    public void displayImage(String url, final ImageView imageView){
        imageView.setImageResource(R.drawable.ic_image_loading);
        sImageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean isImmediate) {
                if (!isImmediate){
                    TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{
                            Application.getContext().getResources().getDrawable(R.drawable.ic_image_loading),
                            new BitmapDrawable(Application.getContext().getResources(), imageContainer.getBitmap())
                    });
                    transitionDrawable.setCrossFadeEnabled(true);
                    imageView.setImageDrawable(transitionDrawable);
                    transitionDrawable.startTransition(100);
                }else{
                    imageView.setImageBitmap(imageContainer.getBitmap());
                }
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                imageView.setImageResource(R.drawable.ic_image_fail);
            }
        });
    }

    public void addRequest(Context context, Request request){
        request.setTag(context);
        sRequestQueue.add(request);
    }

    public void cancelRequests(Context tag){
        sRequestQueue.cancelAll(tag);
    }
}
