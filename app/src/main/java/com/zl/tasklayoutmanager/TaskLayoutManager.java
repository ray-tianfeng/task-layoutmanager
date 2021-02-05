package com.zl.tasklayoutmanager;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Time: 2021/1/29 0029
 * Author: longge
 */
public class TaskLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {
    //横竖屏常量
    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;
    //布局朝向配置
    private int orientation = HORIZONTAL;
    //childHeight/childWidth：子View高度/宽度， scrollOffset：滚动偏移量， minLimit：可以滚动最小临界点， maxLimit可以滚动最大临界点
    private int childHeight = 0, childWidth = 0, scrollOffset = 0, minLimit = 0, maxLimit = 0;;
    //在xy轴缩放的最小比例,默认0.85，一个视图的距离后的view为0.85f
    private float scaleXY = 0.85f;
    //滚动时的缩放差值
    private float scaleInterpolation;
    //未选中view的亮度，要此值生效，需要重写getBrightnessView，将亮度变化的view返回，而且返回的view必须设置了background，
    //也可以去重写setBrightness方法去实现亮度变化的需求
    //度值区间[-100, 0]值越小，越暗
    private int brightness = -30;
    public TaskLayoutManager() {
    }

    public TaskLayoutManager(int orientation) {
        this.orientation = orientation;
    }

    /**
     * @param orientation 布局方向
     * @param scaleXY 在x和y上最小的收缩尺寸
     * @param brightness 未选中的亮度值区间[-100, 0]值越小，越暗
     */
    public TaskLayoutManager(int orientation, float scaleXY, int brightness) {
        this.orientation = orientation;
        this.scaleXY = scaleXY;
        this.brightness = brightness;

    }

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, int widthSpec, int heightSpec) {
        super.onMeasure(recycler, state, widthSpec, heightSpec);
        //初始化值
        int viewWidth = View.MeasureSpec.getSize(widthSpec);
        int viewHeight = View.MeasureSpec.getSize(heightSpec);
        //横竖屏分别初始化
        if(orientation == HORIZONTAL){
            //item宽度
            this.childWidth = viewWidth / 2;
            //item高度
            this.childHeight = (int) ((viewHeight * 1f / (viewWidth * 1f)) * childWidth);
            //item向右滚动的最小距离
            this.minLimit = -childWidth / 2;
            //item向左滚动的最大距离
            this.maxLimit = childWidth * state.getItemCount() - childWidth * 3 / 2 ;
            //当只有一项时让视图居中，将滚动偏移调整到居中位置
            if(state.getItemCount() == 1) this.scrollOffset = - childWidth / 2;
            //当偏移量为0，初始化偏移量，让第一个view显示一半，如果需要第一个view显示完整，此处修改成0就可以了
            else if(scrollOffset == 0) this.scrollOffset = childWidth / 2;
            //修正偏移量。当ItemTouchHelper移除一个item时会重新调用onMeasure方法，此处需要吧原来设置的值重新修正哈，超出部分需要减去
            //例如当滚动到最后一项。但是此时使用ItemTouchHelper移除了最后一项。滚动偏移量就超过item*item.getWidth,就会导致我们的
            else fixScrollOffset();
            //设置滚动缩放差值
            this.scaleInterpolation = (1f - scaleXY) / (viewWidth * 1f);
        }else{
            this.childHeight = viewHeight / 2;
            this.childWidth = (int) ((viewWidth * 1f / (viewHeight * 1f)) * childHeight);
            this.minLimit = -childHeight / 2;
            this.maxLimit = childHeight * state.getItemCount() - childHeight * 3 / 2 ;
            if(state.getItemCount() == 1) this.scrollOffset = -childHeight / 2;
            else if(scrollOffset == 0) this.scrollOffset = childHeight / 2;
            else fixScrollOffset();
            this.scaleInterpolation = (1f - scaleXY) / (viewHeight * 1f);
        }
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    //是否可以垂直滚动 true：支持垂直滚动， false不支持。配合scrollVerticallyBy一起使用
    @Override
    public boolean canScrollVertically() {
        return orientation == VERTICAL;
    }

    //是否可以水平滚动 true：支持水平滚动， false不支持。配合scrollVerticallyBy一起使用
    @Override
    public boolean canScrollHorizontally() {
        return orientation == HORIZONTAL;
    }

    /**
     * 布局
     * @param recycler 回收使用
     * @param state RecyclerView的状态
     */
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //step1. 将所有view离屏，离屏后缓存到mAttachedScrap【1】
        //原因：如果不detach掉，未滚到到屏幕外的view，不能统一更新位置。所以这里需要detach掉然后更新位置后重新添加view,如果开启了预布局，真实布局前onLayoutChildren已经调用过一次，所以这里需要detachAndScrapAttachedViews
        //而且已经添加了的view。addView的时候实际调用的attachViewToParent，和调用attach（view）效果一样
        detachAndScrapAttachedViews(recycler);
        //item数据未0时不用布局了
        if(state.getItemCount() == 0){
            //回收所有的view是因为如果更新后导致数据为0.需要回收view，这里回收到mCachedViews【2】，默认缓存为2
            //当这个缓存满了的时候就会将早先加入的holder取出加入RecyclerViewPool【4】
            removeAndRecycleAllViews(recycler);
            return;
        }
        //根据滚动偏移量得到在屏幕上显示的第一个item
        int startIndex = (orientation == HORIZONTAL) ? Math.abs(scrollOffset) / childWidth : Math.abs(scrollOffset) / childHeight;
        //整个屏幕只能显示3个item，得到最后一个item
        int endIndex = startIndex + 3;
        //修正最后一个item
        if(endIndex > state.getItemCount()) endIndex = state.getItemCount();
        //需要高亮的view
        View brightnessChild = null;
        //距离中心线最近的值
        int minSize = Integer.MAX_VALUE;
        //中心线
        int centerLine = (orientation == HORIZONTAL) ? getWidth() / 2 : getHeight() / 2;
        for(int index = startIndex; index < endIndex; index++){
            //从缓存中取出view，如果没后缓存记录，将会去创匠一个holder，并拿到它的view
            //这里的获取流程
            //一级缓存（mChangedScrap(优先)/mAttachedScrap）->二级缓存(mCachedViews)->四级缓存（mRecyclerPool）->Create(Holder)添加到mRecyclerPool并返回view
            //这里没有说三级缓存（mViewCacheExtension）是因为mViewCacheExtension需要自己去实现缓存逻辑。
            View childView = recycler.getViewForPosition(index);
            //测量view
            measureChildWithMargins(childView, childWidth, childHeight);
            //将view添加到recyclerview
            addView(childView);
            //view的位置和中线线
            int left , top , right , bottom , childCenterLine;
            //横竖屏分开计算
            if(orientation == HORIZONTAL){
                //item的Left = item的宽度*当item下标-滚动偏移量
                left = index * childWidth - scrollOffset;
                top = (getHeight() - childHeight) / 2;
                right = left + childWidth;
                bottom = top + childHeight;
                childCenterLine = left + childWidth / 2;
            }else{
                left = (getWidth() - childWidth) / 2;
                top = index * childHeight - scrollOffset;
                right = left + childWidth;
                bottom = top + childHeight;
                childCenterLine = top + childHeight / 2;
            }
            //child layout
            layoutDecorated(childView, left, top, right, bottom);
            //计算出中线线距离差值
            int tempInterSect = Math.abs(centerLine - childCenterLine);
            //找到距离中线最近的view
            if(tempInterSect < minSize){
                minSize = tempInterSect;
                brightnessChild = childView;
            }
            //根据距离计算出缩放差值
            float scale = (orientation == HORIZONTAL) ?
                    scaleXY + (getWidth() - tempInterSect) / 20 * 20 * scaleInterpolation :
                    scaleXY + (getHeight() - tempInterSect) / 20 * 20 * scaleInterpolation;
            //设置缩放和亮度
            decorateView(childView, scale, brightness);
        }
        //设置距离中心线最近的view高亮
        setBrightness(brightnessChild, 1);
        //回收
        //这里将所有mAttachedScrap中未取出的holder回收掉，离屏未使用holder。都是已经滚动到屏幕外的view
        //如果已经开启预布局
        List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
        for (int i = 0; i < scrapList.size(); i++) {
            RecyclerView.ViewHolder holder = scrapList.get(i);
            removeAndRecycleView(holder.itemView, recycler);
        }
    }

    /**
     * 设置view亮度和缩放
     * @param childView item view
     * @param scaleXY xy轴上的缩放值
     * @param brightness 亮度
     */
    private void decorateView(View childView,  float scaleXY, int brightness){
        setBrightness(childView, brightness);
        childView.setScaleX(scaleXY);
        childView.setScaleY(scaleXY);
    }

    /**
     * 如果canScrollHorizontally中返回true，左右滑动将回调此方法
     * @param dx 在x轴上的偏移量
     * @param recycler 回收
     * @param state 状态
     * @return 0 已经达到临界值不进行滚动，返回其他值进行滚动，这里的滚动是对所有的view进行离屏和layout操作
     */
    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        scrollOffset = scrollOffset + dx;
        fixScrollOffset();
        onLayoutChildren(recycler, state);
        return (scrollOffset == minLimit || scrollOffset == maxLimit) ? 0 : dx;
    }

    //同上
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        scrollOffset = scrollOffset + dy;
        fixScrollOffset();
        onLayoutChildren(recycler, state);
        return (scrollOffset == minLimit || scrollOffset == maxLimit) ? 0 : dy;
    }

    /**
     * 根据临界值修正偏移量
     */
    private void fixScrollOffset(){
        if(scrollOffset <= minLimit){
            scrollOffset = minLimit;
        }else if(scrollOffset >= maxLimit){
            scrollOffset = maxLimit;
        }
    }

    /**
     * 设置亮度
     * @param itemView itemview
     * @param brightness 亮度
     */
    private void setBrightness(View itemView, int brightness){
        View targetView = getBrightnessView(itemView);
        if(targetView == null) return;
        PorterDuffColorFilter porterDuffColorFilter;
        if(brightness > 0){
            int value = (int) brightness * 255 / 100;
            porterDuffColorFilter = new PorterDuffColorFilter(Color.argb(value, 255, 255, 255), PorterDuff.Mode.SRC_OVER);

        } else {
            int value = (int) (brightness * -1) * 255/100;
            porterDuffColorFilter = new PorterDuffColorFilter(Color.argb(value, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
        }
        targetView.getBackground().setColorFilter(porterDuffColorFilter);
    }

    /**
     * 返回需要修改亮度的view，此方法需要被重写。如果没有重写此方法。关于亮度的设置失效
     * @param itemView 参数为itemview
     * @return 返回需要修改亮度的view.这个view必须设置了背景值。亮度修改是通过修改背景亮度
     */
    public View getBrightnessView(View itemView){
        return null;
    }

    /**
     * 通过一个向量来确定方向，这里的方向分为布局方向和滚动方向
     * PointF(x, y),如果x不为0则布局方向为水平布局，x大于0滚动方向为正(自屏幕上向下拉)，x小于0滚动方向为负（自屏幕下向上拉）
     * @param targetPosition 这里的targetPosition为参考iteView 的下标, LinearSnapHelper中这个值为最后一个下标（因为estimateNextPositionDiffForFling计算的跳转距离已经带有符号）。RecyclerView.onAnimation中传的当前可见的第一个item下标
     * @return 方向向量
     */
    @Nullable
    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        final int firstChildPos = getPosition(getChildAt(0));
        final int direction = targetPosition < firstChildPos  ? -1 : 1;
        if (orientation == HORIZONTAL) {
            return new PointF(direction, 0);
        } else {
            return new PointF(0, direction);
        }
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext());
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }
}
