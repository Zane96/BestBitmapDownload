package com.example.zane.bitmapdownload;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Zane on 15/11/25.
 */
public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.MyViewHolder>{

    //图片缓存LruCache
    private LruCache<String, BitmapDrawable> mLrucache;
    private LayoutInflater layoutInflater;
    private Context context;

    public RecycleViewAdapter(Context context){

        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        int maxSize = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxSize / 8;
        mLrucache = new LruCache<String, BitmapDrawable>(cacheSize){
            @Override
            protected int sizeOf(String key, BitmapDrawable value) {
                return value.getBitmap().getByteCount();
            }
        };

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_recycleview, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String url = Images.imageUrls[position];

        BitmapDrawable drawable = getBitmapFromMemoryCache(url);
        if(drawable != null){
            holder.imageView.setImageDrawable(drawable);
        }else {
            loadBitmap(url, holder.imageView);
        }

    }

    @Override
    public int getItemCount() {
        return Images.imageUrls.length;
    }

    //将drawable与imageview绑定
    public void loadBitmap(String url, ImageView imageView){
        Bitmap mLoadingBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
        if (cancelBeforeTask(url, imageView)){
            BitmapDownloadTask task = new BitmapDownloadTask(imageView);
            AsyncDrawable drawable = new AsyncDrawable(context.getResources(), mLoadingBitmap, task);
            imageView.setImageDrawable(drawable);
            task.execute(url);
        }
    }

    //用来进行第一步的判断，如果imageview对应的task的url跟传进去的url不同，那么取消上一次task
    public boolean cancelBeforeTask(String url, ImageView imageView){
        BitmapDownloadTask task = getBitmapWorkerTask(imageView);

        if(task != null){
            String imgUrl = task.url;
            if (imgUrl != url || imgUrl == ""){
                task.cancel(true);
            } else {
                return false;
            }
        }

        return true;
    }

    //用来根据传进来的imageview获得对应的draeable,再获得对应的task
    private static BitmapDownloadTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapTask();
            }
        }
        return null;
    }

    //添加task的弱引用
    static class AsyncDrawable extends BitmapDrawable{

        private WeakReference<BitmapDownloadTask> taskWeakReference;

        public AsyncDrawable(Resources res, Bitmap bitmap
                                    , BitmapDownloadTask task) {
            super(res, bitmap);
            taskWeakReference = new WeakReference<BitmapDownloadTask>(task);
        }

        public BitmapDownloadTask getBitmapTask(){
            return taskWeakReference.get();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageview);
        }
    }

    public BitmapDrawable getBitmapFromMemoryCache(String key) {
        return mLrucache.get(key);
    }

    public void addBitmapToMemoryCache(String key, BitmapDrawable drawable) {
        if (getBitmapFromMemoryCache(key) == null) {
            mLrucache.put(key, drawable);
        }
    }

    //添加imageview的弱引用
    class BitmapDownloadTask extends AsyncTask<String, Void, BitmapDrawable>{

        private ImageView imageView;
        private WeakReference<ImageView> imageViewWeakReference;
        public String url;

        public BitmapDownloadTask(ImageView imageView) {
            this.imageView = imageView;
            imageViewWeakReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected BitmapDrawable doInBackground(String... params) {

            url = params[0];
            Bitmap bitmap = downloadBitmap(url);
            BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
            addBitmapToMemoryCache(url, bitmapDrawable);

            return bitmapDrawable;
        }

        @Override
        protected void onPostExecute(BitmapDrawable bitmapDrawable) {

            if(isCancelled()){
                bitmapDrawable = null;
            }
            if(imageViewWeakReference != null && bitmapDrawable != null){
                ImageView imageView = imageViewWeakReference.get();
                BitmapDownloadTask task = getBitmapWorkerTask(imageView);
                if(this == task && imageView != null){
                    imageView.setImageDrawable(bitmapDrawable);
                }
            }
            //imageView.setImageDrawable(bitmapDrawable);
        }

        private Bitmap downloadBitmap(String imageUrl) {
            Bitmap bitmap = null;
            HttpURLConnection con = null;
            try {
                URL url = new URL(imageUrl);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5 * 1000);
                con.setReadTimeout(10 * 1000);
                bitmap = BitmapFactory.decodeStream(con.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
            return bitmap;
        }
    }
}
