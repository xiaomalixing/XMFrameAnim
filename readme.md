## 一、组件名
小马自研车载帧动画组件，主要特性：
- 支持Resource下图集显示；
- 支持Assets和本地图集显示；
- 支持多图集显示；
- 支持精灵图集显示；
- 依赖库小且资源占用小；

## 二、如何使用
### 1、依赖
     
### 2、应用实例
#### - 创建布局文件：

	<?xml version="1.0" encoding="utf-8"?>
	<FrameLayout 	xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <com.xiaoma.frameanim.FrameAnimSurfaceView
            android:id="@+id/anim_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="@android:color/darker_gray" />
    <!--TextureView实现-->
    <!--<com.xiaoma.frameanim.FrameAnimTextureView
        android:id="@+id/anim_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@android:color/darker_gray" />-->
	</FrameLayout>
    

#### - 代码实现：
通过ResourceId创建一个普通的帧动画，并添加到View中播放

	FrameAnimSurfaceView view;  // TODO: 动画渲染器View
	Context context;            // TODO: Context 
	int[] frameResIds;          // TODO: 资源Id数组, 帧序列将按照此顺序

	// 通过ResourceId创建一个普通的帧动画
	IFrameAnim anim = FrameAnimFactory.createWithResIds(context, frameResIds);
	// 类似的, 也可以创建精灵动画
	// IFrameAnim animSprite1 = 	FrameAnimFactory.createWithSprite("vpa/frames/1-1/03", ResourceType.ASSETS);   // Assets资源
	// IFrameAnim animSprite2 = FrameAnimFactory.createWithSprite("sdcard/frames/1-1/03", ResourceType.FILE);  // 普通磁盘文件
	anim.setOneShot(false);  // 设置是否播放一次
	view.addFrameAnim(anim); // 将当前动画添加到渲染器中
	anim.start();            // 开始当前动画
	// 帧动画播放回调
	anim.addFrameAnimListener(new FrameAnimListener() {
            @Override
            public void onStart(@NonNull IFrameAnim anim) {
                // 帧动画开始回调
            }

            @Override
            public void onStop(@NonNull IFrameAnim anim) {
                // 帧动画结束回调, 主动stop或oneshot模式下结束时执行
            }
        });

#### 3、加载效果：
