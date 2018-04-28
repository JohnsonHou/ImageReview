package com.jchou.imagereview.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.jchou.imagereview.R;
import com.jchou.imagereview.glide.ProgressTarget;
import com.jchou.imagereview.util.ScreenUtils;

import java.io.File;

import static com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.ZOOM_FOCUS_CENTER_IMMEDIATE;


/**
 * 单张图片显示Fragment
 */
public class ImageDetailFragment extends Fragment {
    private String mImageUrl;

    private SubsamplingScaleImageView mImageView;


    private boolean isNewCreate = false, isVisible = false;//是否第一次加载完成，是否可见。

    public static ImageDetailFragment newInstance(String imageUrl) {
        final ImageDetailFragment f = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putString("url", imageUrl);
        f.setArguments(args);

        return f;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;
        if (isVisibleToUser) {
            initData();
        } else {
            isNewCreate = false;
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(getActivity() instanceof OnLoadListener) {
            onLoadListener = (OnLoadListener)getActivity();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onLoadListener=null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mImageUrl = bundle != null ? bundle.getString("url", "") : "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.image_detail_fragment,
                container, false);
        mImageView = (SubsamplingScaleImageView) v.findViewById(R.id.image);
        mImageView.setMaxScale(15);
        mImageView.setZoomEnabled(true);
        mImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isNewCreate = true;//布局新创建
        initData();
    }


    private void initData() {
        if (!isVisible || !isNewCreate) {
            return;
        }
        if (onImageListener != null) {
            onImageListener.onInit();
        }
        Glide.with(getActivity())
                .load(mImageUrl)
                .downloadOnly(new ProgressTarget<String, File>(mImageUrl, null) {

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        if (onLoadListener!=null){
                            onLoadListener.onLoadStart();
                        }
                    }

                    @Override
                    public void onProgress(long bytesRead, long expectedLength) {
                        int p = 0;
                        if (expectedLength >= 0) {
                            p = (int) (100 * bytesRead / expectedLength);
                        }
                    }

                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> animation) {
                        // 将保存的图片地址给SubsamplingScaleImageView,这里注意设置ImageViewState设置初始显示比例
                        ImageSource imageSource = ImageSource.uri(Uri.fromFile(resource));
                        int sWidth = BitmapFactory.decodeFile(resource.getAbsolutePath()).getWidth();
                        int sHeight = BitmapFactory.decodeFile(resource.getAbsolutePath()).getHeight();
                        WindowManager wm = (WindowManager) getActivity()
                                .getSystemService(Context.WINDOW_SERVICE);
                        int width = ScreenUtils.getScreenWidth(getActivity());
                        int height = ScreenUtils.getScreenHeight(getActivity());
                        float scaleW = width / (float) sWidth;
                        float scaleH = height / (float) sHeight;
                        if (sHeight >= height
                                && sHeight / sWidth >= height / width) {
                            mImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
                            mImageView.setImage(ImageSource.uri(Uri.fromFile(resource)), new ImageViewState(scaleW, new PointF(0, 0), 0));
                        } else {
                            mImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
                            mImageView.setImage(ImageSource.uri(Uri.fromFile(resource)));
                            mImageView.setDoubleTapZoomStyle(ZOOM_FOCUS_CENTER_IMMEDIATE);
                        }
                        ActivityCompat.startPostponedEnterTransition(getActivity());
                        if (onLoadListener!=null){
                            onLoadListener.onLoadSuccess();
                        }
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        ActivityCompat.startPostponedEnterTransition(getActivity());
                        if (onLoadListener!=null){
                            onLoadListener.onLoadFailed();
                        }
                    }

                    @Override
                    public void getSize(SizeReadyCallback cb) {
                        cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                    }
                });

    }


    public interface OnImageListener {
        void onInit();
    }

    public void setOnImageListener(OnImageListener onImageListener) {
        this.onImageListener = onImageListener;
    }

    private OnImageListener onImageListener;


    public interface OnLoadListener{
        void onLoadStart();
        void onLoadSuccess();
        void onLoadFailed();
    }

    private OnLoadListener onLoadListener;

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageView.recycle();
    }


}