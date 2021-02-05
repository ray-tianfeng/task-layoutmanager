package com.zl.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.zl.tasklayoutmanager.IHCallback;
import com.zl.tasklayoutmanager.R;
import com.zl.tasklayoutmanager.RemoveListener;
import com.zl.tasklayoutmanager.TaskLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends Activity {
    ArrayList<Integer> dataArray = new ArrayList(Arrays.asList(
            R.drawable.pic1,
            R.drawable.pic2,
            R.drawable.pic3,
            R.drawable.pic4,
            R.drawable.pic5,
            R.drawable.pic1,
            R.drawable.pic2,
            R.drawable.pic3,
            R.drawable.pic4,
            R.drawable.pic5,
            R.drawable.pic6,
            R.drawable.pic7,
            R.drawable.pic8,
            R.drawable.pic9,
            R.drawable.pic10,
            R.drawable.pic6,
            R.drawable.pic7,
            R.drawable.pic1,
            R.drawable.pic2,
            R.drawable.pic3,
            R.drawable.pic4,
            R.drawable.pic5,
            R.drawable.pic6,
            R.drawable.pic7,
            R.drawable.pic8,
            R.drawable.pic9,
            R.drawable.pic10,
            R.drawable.pic8,
            R.drawable.pic9,
            R.drawable.pic10
    ));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView mRecyclerView = findViewById(R.id.recycle_view);
        mRecyclerView.setLayoutManager(new TaskLayoutManager(TaskLayoutManager.VERTICAL, 0.6f, -50){
            @Override
            public View getBrightnessView(View itemView) {
                return itemView.findViewById(R.id.iv);
            }
        });
        final TestAdapter mTestAdapter = new TestAdapter();
        mRecyclerView.setAdapter(mTestAdapter);
        new ItemTouchHelper(new IHCallback(new RemoveListener() {
            @Override
            public void onItemRemove(int position) {
                dataArray.remove(position);
                mTestAdapter.notifyItemRemoved(position);
            }
        })).attachToRecyclerView(mRecyclerView);
        new LinearSnapHelper().attachToRecyclerView(mRecyclerView);
    }
    class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder>{


        @NonNull
        @Override
        public TestAdapter.TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item, parent, false);
            return new TestViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull TestAdapter.TestViewHolder holder, int position) {
            holder.iv.setBackgroundResource(dataArray.get(position));
        }

        @Override
        public int getItemCount() {
            return dataArray.size();
        }

        class TestViewHolder extends RecyclerView.ViewHolder{
            private View iv;
            public TestViewHolder(@NonNull View itemView) {
                super(itemView);
                iv = itemView.findViewById(R.id.iv);
            }
        }
    }
}