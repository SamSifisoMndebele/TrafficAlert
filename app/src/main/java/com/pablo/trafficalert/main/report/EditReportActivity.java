package com.pablo.trafficalert.main.report;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.os.Bundle;
import android.view.View;

import com.pablo.trafficalert.R;
import com.pablo.trafficalert.databinding.ActivityEditReportBinding;
import com.pablo.trafficalert.databinding.FragmentReportBinding;
import com.pablo.trafficalert.models.ReportData;

public class EditReportActivity extends AppCompatActivity {

    public final static String ARG_REPORT = "arg_report";

    private ActivityEditReportBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        binding = ActivityEditReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(v->onBackPressed());

        ReportData reportData = getIntent().getExtras().getParcelable(ARG_REPORT);
        if (reportData == null) {
            finish();
            return;
        }

        binding.titleEditText.setText(reportData.getTitle());
    }
}