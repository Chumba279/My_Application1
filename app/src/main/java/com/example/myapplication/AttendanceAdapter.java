package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private ArrayList<Map<String, Object>> attendanceList;

    public AttendanceAdapter(ArrayList<Map<String, Object>> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> data = attendanceList.get(position);

        holder.txtStudentEmail.setText(String.valueOf(data.get("studentEmail")));

        Object timestamp = data.get("timestamp");
        if (timestamp instanceof Long) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            holder.txtAttendanceTime.setText("Marked at: " + sdf.format(new Date((Long) timestamp)));
        } else {
            holder.txtAttendanceTime.setText("Marked at: --:--");
        }

        Object distance = data.get("distance");
        if (distance != null) {
            holder.txtDistance.setText("Distance: " + String.format("%.1fm", Double.parseDouble(String.valueOf(distance))));
        } else {
            holder.txtDistance.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtStudentEmail, txtAttendanceTime, txtDistance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtStudentEmail = itemView.findViewById(R.id.txtStudentEmail);
            txtAttendanceTime = itemView.findViewById(R.id.txtAttendanceTime);
            txtDistance = itemView.findViewById(R.id.txtDistance);
        }
    }
}
