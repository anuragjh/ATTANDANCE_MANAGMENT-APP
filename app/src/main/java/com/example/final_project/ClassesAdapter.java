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
    private OnAttendanceButtonClickListener listener;
    private String currentUser;

    public ClassesAdapter(List<ClassModelR> classList, OnAttendanceButtonClickListener listener) {
        this.classList = classList;
        this.listener = listener;
        this.currentUser = currentUser;
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
        holder.textViewSubjectName.setText(classModel.getSubjectName());
        holder.textViewTeacherName.setText(classModel.getTeacherName());

        // Update button text based on attendance status
        if (classModel.isAttendanceDone()) {
            holder.buttonAttendance.setText("Already Present");
            holder.buttonAttendance.setEnabled(false);
        } else {
            holder.buttonAttendance.setText("Mark Attendance");
            holder.buttonAttendance.setEnabled(true);
        }

        holder.buttonAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAttendanceButtonClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView textViewClassName;
        TextView textViewSubjectName;
        TextView textViewTeacherName;
        Button buttonAttendance;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewClassName = itemView.findViewById(R.id.textViewClassName);
            textViewSubjectName = itemView.findViewById(R.id.textViewSubjectName);
            textViewTeacherName = itemView.findViewById(R.id.textViewTeacherName);
            buttonAttendance = itemView.findViewById(R.id.buttonAttendance);
        }
    }

    public interface OnAttendanceButtonClickListener {
        void onAttendanceButtonClick(int position);
    }

    public void updateAttendanceStatus(String classId, boolean attendanceDone) {
        for (ClassModelR classModelR : classList) {
            if (classModelR.getId().equals(classId)) {
                classModelR.setAttendanceDone(attendanceDone);
                notifyDataSetChanged();
                return;
            }
        }
    }
}
