package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private ArrayList<Map<String, Object>> courseList;

    public CourseAdapter(ArrayList<Map<String, Object>> courseList) {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> course = courseList.get(position);
        holder.txtCourseCode.setText(String.valueOf(course.get("courseCode")));
        holder.txtStatus.setText("Status: " + String.valueOf(course.get("status")));
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCourseCode, txtStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCourseCode = itemView.findViewById(R.id.txtCourseCode);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}
