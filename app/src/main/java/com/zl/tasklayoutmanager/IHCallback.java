package com.zl.tasklayoutmanager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ItemTouchHelper 辅助类，主要用于滑动删除和拖拽换位置的
 * 滑动删除
 * Time: 2021/2/1 0001
 * Author: zoulong
 */
public class IHCallback extends ItemTouchHelper.Callback {
    private RemoveListener mRemoveListener;
    public IHCallback(RemoveListener mRemoveListener) {
        this.mRemoveListener = mRemoveListener;
    }

    /**
     * 根据 recycleview 和 viewHolder返回滑动方向好移动方向
     * @param recyclerView
     * @param viewHolder
     * @return
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        boolean isHorizontal = recyclerView.getLayoutManager().canScrollHorizontally();
        //滑动方向
        int dragFlags = isHorizontal ? ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT  : ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        //移动方向
        int swipeFlags = !isHorizontal ? ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT  : ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mRemoveListener.onItemRemove(viewHolder.getLayoutPosition());
    }

    /**
     * 滑动临界值百分比， 默认值0.5f
     * @param viewHolder
     * @return
     */
    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return .1f;
    }
}
