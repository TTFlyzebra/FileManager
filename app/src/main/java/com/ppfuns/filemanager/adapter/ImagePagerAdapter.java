package com.ppfuns.filemanager.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ppfuns.filemanager.MyApp;
import com.ppfuns.filemanager.R;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.wtflogger.WtfLog;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by 李冰锋 on 2016/12/23 11:22.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.adapter
 */
public class ImagePagerAdapter extends PagerAdapter {
    public final static String TAG = ImagePagerAdapter.class.getSimpleName();

    private List<AbstractMediaItem> mMediaItemList;
    private WeakReference<View> mViewWeakReference;


    public ImagePagerAdapter(List<AbstractMediaItem> pMediaItemList) {
        mMediaItemList = pMediaItemList;
    }

    @Override
    public int getCount() {
        if (mMediaItemList != null) {
            return mMediaItemList.size();
        }
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Context context = container.getContext();

        View view = LayoutInflater.from(context).inflate(R.layout.item_viewpager_image, container, false);
        container.addView(view);

        final ImageView imageView = (ImageView) view.findViewById(R.id.iv_image);
        final View loadingView = view.findViewById(R.id.ll_loading);
        final String path = mMediaItemList.get(position).mPath;
        mOnLoadingListener.onLoadingStart(loadingView, path);
        Glide.with(context)
                .load(path)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(MyApp.getScreenWidth(), MyApp.getScreenHeight())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        loadingView.setVisibility(View.INVISIBLE);
                        imageView.setImageResource(R.drawable.erro_rpic);
                        mOnLoadingListener.onLoadingError(e);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        loadingView.setVisibility(View.INVISIBLE);
                        WtfLog.d.tmpTag(TAG)
                                .title("drawable_bounds")
                                .key("w").intVal(resource.getIntrinsicWidth())
                                .key("h").intVal(resource.getIntrinsicHeight())
                                .print();
                        mOnLoadingListener.onLoadingFinnish(loadingView, path);
                        return false;
                    }
                })
                .into(imageView);
        return view;
    }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                if (object instanceof View) {
                    ImageView imageView = (ImageView) ((View) object).findViewById(R.id.iv_image);
                    Glide.clear(imageView);
                    container.removeView((View) object);
                }
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return super.getPageTitle(position);
            }


            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                if (object instanceof View) {
                    mViewWeakReference = new WeakReference<>((View) object);
                }
            }

            public View getItemView() {
                return mViewWeakReference.get();
            }


            private OnLoadingListener mOnLoadingListener = new DefaultOnLoadingListener();


            public static class DefaultOnLoadingListener implements OnLoadingListener {

                @Override
                public void onLoadingStart(View loadingView, String loadingPath) {

                }

                @Override
                public void onLoadingFinnish(View loadingView, String loadingPath) {

                }

                @Override
                public void onLoadingError(Throwable pThrowable) {

                }
            }

            public interface OnLoadingListener {
                void onLoadingStart(View loadingView, String loadingPath);

                void onLoadingFinnish(View loadingView, String loadingPath);

                void onLoadingError(Throwable pThrowable);
            }

            public void setOnLoadingListener(OnLoadingListener pOnLoadingListener) {
                mOnLoadingListener = pOnLoadingListener;
            }
        };
