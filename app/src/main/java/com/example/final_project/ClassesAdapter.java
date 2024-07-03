package com.example.final_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClassesAdapter extends RecyclerView.Adapter<ClassesAdapter.ClassViewHolder> {

    private List<ClassModelR> classList;
    private OnAttendanceButtonClickListener attendanceButtonClickListener;

    public ClassesAdapter(List<ClassModelR> classList, OnAttendanceButtonClickListener listener) {
        this.classList = classList;
        this.attendanceButtonClickListener = listener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassModelR classModel = classList.get(position);
        holder.textViewClassName.setText(classModel.getClassName());

        if (classModel.isAttendanceDone()) {
            holder.buttonAttendance.setText("Attendance Done");
            holder.buttonAttendance.setEnabled(false);
        } else {
            holder.buttonAttendance.setText("Give Attendance");
            holder.buttonAttendance.setEnabled(true);
        }

        holder.buttonAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (attendanceButtonClickListener != null) {
                    attendanceButtonClickListener.onAttendanceButtonClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView textViewClassName;
        Button buttonAttendance;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewClassName = itemView.findViewById(R.id.textViewClassName);
            buttonAttendance = itemView.findViewById(R.id.buttonAttendance);
        }
    }

    // Interface for handling attendance button clicks
    public interface OnAttendanceButtonClickListener {
        void onAttendanceButtonClick(int position);
    }
}
