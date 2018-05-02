# ImageReview
仿微信图片详情页面，可下拉关闭页面

效果如下

![imagereview.gif](https://github.com/JohnsonHou/ImageReview/blob/HEAD/gif/imagereview.gif)

如何使用

````
compile 'com.jchou.android.imagereview:imagereview:1.0.1'
````

首先在需要使用的页面

````
//MainActivity调用的页面，urls传入的图片数据，pos传入的图片位置，view需要共享的view
ImagePagerActivity.startImagePage(MainActivity.this,
                        urls,pos,view);
````

其次设置转场动画的共享元素，因为跳转和返回时都会调用onMapSharedElements，需要判断bundle是否为空，bundle会在返回的时候在onActivityReenter获取
````
    //设置转场动画的共享元素，因为跳转和返回时都会调用onMapSharedElements，需要判断bundle是否为空
    setExitSharedElementCallback(new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (bundle!=null){
                int index = bundle.getInt(ImagePagerActivity.STATE_POSITION,0);
                sharedElements.clear();
                sharedElements.put("img", recyclerView.getLayoutManager().findViewByPosition(index));
                bundle=null;
            }
        }
    });
````

在返回的时候获取数据bundle
````
@Override
public void onActivityReenter(int resultCode, Intent data) {
    super.onActivityReenter(resultCode, data);
    bundle = data.getExtras();
    int currentPosition = bundle.getInt(ImagePagerActivity.STATE_POSITION,0);
    //做相应的滚动
    recyclerView.scrollToPosition(currentPosition);
    //暂时延迟 Transition 的使用，直到我们确定了共享元素的确切大小和位置才使用
    //postponeEnterTransition后不要忘记调用startPostponedEnterTransition
    ActivityCompat.postponeEnterTransition(this);
    recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
            // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
            recyclerView.requestLayout();
            //共享元素准备好后调用startPostponedEnterTransition来恢复过渡效果
            ActivityCompat.startPostponedEnterTransition(MainActivity.this);
            return true;
        }
    });
}
````
