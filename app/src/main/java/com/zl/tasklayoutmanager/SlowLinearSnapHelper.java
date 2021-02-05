package com.zl.tasklayoutmanager;

import android.view.View;

import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView速度太快，此处进行抑制
 * Time: 2021/2/5 0005
 * Author: zoulong
 */
public class SlowLinearSnapHelper extends LinearSnapHelper {
    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        int targetSnapPosition = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
        final int itemCount = layoutManager.getItemCount();
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION;
        }

        final View currentView = findSnapView(layoutManager);
        if (currentView == null) {
            return RecyclerView.NO_POSITION;
        }

        int currentPosition = layoutManager.getPosition(currentView);
        int offset = 0;
        if(currentPosition < targetSnapPosition){
            offset = targetSnapPosition - currentPosition;
            if(offset > 3) targetSnapPosition = currentPosition + 3;
        }else{
            offset = currentPosition - targetSnapPosition;
            if(offset > 3) targetSnapPosition = currentPosition - 3;
        }
        return targetSnapPosition;
    }
}
