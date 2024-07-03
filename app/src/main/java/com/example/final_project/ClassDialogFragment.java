package com.example.final_project;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Arrays;
import java.util.List;

public class ClassDialogFragment extends DialogFragment {

    private TeacherActivity teacherActivity; // Reference to TeacherActivity

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        teacherActivity = (TeacherActivity) getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_class, null);
        builder.setView(dialogView);

        Spinner spinnerSubjectName = dialogView.findViewById(R.id.spinnerSubjectName);
        Button buttonAdd = dialogView.findViewById(R.id.buttonAdd);
        Button buttonEndClass = dialogView.findViewById(R.id.buttonEndClass);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        // Set up the spinner with sample subjects
        List<String> subjects = Arrays.asList("OPERATING SYSTEMS","DATABASE MANAGEMENT SYSTEM" , "COMPUTER NETWORK" ,"SOFTWARE ENGINEERING","OOP USING JAVA");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubjectName.setAdapter(adapter);

        final AlertDialog dialog = builder.create();

        if (teacherActivity.isOngoingClass()) {
            buttonEndClass.setVisibility(View.VISIBLE);
            buttonAdd.setVisibility(View.GONE);
        }

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedSubject = spinnerSubjectName.getSelectedItem().toString();
                teacherActivity.createClass("Class Name", selectedSubject); // Replace with actual class name logic if needed
                dismiss();
            }
        });

        buttonEndClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                teacherActivity.endOngoingClass();
                dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        return dialog;
    }
}
