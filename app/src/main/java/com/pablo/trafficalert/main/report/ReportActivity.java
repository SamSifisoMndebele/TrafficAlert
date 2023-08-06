package com.pablo.trafficalert.main.report;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.pablo.trafficalert.R;
import com.pablo.trafficalert.databinding.ActivityReportBinding;
import com.pablo.trafficalert.models.ReportData;
import com.pablo.trafficalert.models.UserInfo;
import com.pablo.trafficalert.utils.LoadingDialog;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ReportActivity extends AppCompatActivity {
    public static final String ARG_USER = "arg_user";
    public static final String ARG_REPORT_ID = "arg_report_id";

    private ActivityReportBinding binding;
    private LoadingDialog loadingDialog;
    private String reportId;
    private UserInfo userInfo;
    private ListenerRegistration snapshotRegistration = null;

    private final FirebaseFirestore database = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingDialog = new LoadingDialog(this);

        binding.backButton.setOnClickListener(view -> onBackPressed());
        reportId = getIntent().getExtras().getString(ARG_REPORT_ID);
        userInfo = getIntent().getExtras().getParcelable(ARG_USER);

        snapshotRegistration = database.collection("reports")
                .document(reportId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        loadingDialog.showError(error.getMessage(), null, dialog -> finish());
                        return;
                    }
                    ReportData reportData = value != null ? value.toObject(ReportData.class) : null;
                    if (reportData == null) loadingDialog.showError("Report is not available.", null, dialog -> finish());
                    else showView(reportData);
                });

        database.collection("reports")
                .document(reportId)
                .update("viewsCount", FieldValue.increment(1));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        snapshotRegistration.remove();
        snapshotRegistration = null;
    }

    @SuppressLint("SetTextI18n")
    private void showView(@NotNull ReportData report){
        UserInfo reportUserInfo = report.getUserInfo();
        if (reportUserInfo != null) {
            Glide.with(binding.userImage)
                    .load(reportUserInfo.getImageUrl())
                    .placeholder(R.drawable.ic_person)
                    .into(binding.userImage);

            String username = "";
            if (reportUserInfo.getNames() != null){
                username += Arrays.stream(reportUserInfo.getNames().split(" ")).map(s -> s.toCharArray()[0]+"")
                        .collect(Collectors.joining("")) + " ";
            }
            if (reportUserInfo.getLastName() != null) {
                username += reportUserInfo.getLastName();
            }
            binding.username.setText("By "+username);
        } else {
            binding.username.setText(getString(R.string.anonymous));
        }
        DateFormat timeInstance = DateFormat.getTimeInstance(DateFormat.SHORT);
        binding.time.setText(timeInstance.format(report.getTimestamp().toDate()));
        DateFormat dateInstance = DateFormat.getDateInstance(DateFormat.MEDIUM);
        binding.date.setText(dateInstance.format(report.getTimestamp().toDate()));
        binding.viewsCount.setText(report.getViewsCount() + " views");

        binding.title.setText(report.getTitle());
        binding.description.setText(report.getDescription());

        String imageUrl = report.getImageUrl();
        if (imageUrl != null){
            Glide.with(binding.image)
                    .load(imageUrl)
                    .into(binding.image);
            binding.image.setVisibility(View.VISIBLE);
        } else {
            binding.image.setImageURI(null);
            binding.image.setVisibility(View.GONE);
        }
        
        if (userInfo.getUid().equals(reportUserInfo != null ? reportUserInfo.getUid() : null)) {
            binding.deleteButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setOnClickListener(v-> new MaterialAlertDialogBuilder(this)
                    .setMessage( "This report will be permanently deleted.")
                    .setNegativeButton("Cancel", (dialog,i)->dialog.dismiss())
                    .setPositiveButton("Delete", (dialog,i)->{
                        snapshotRegistration.remove();
                        loadingDialog.showProgress("Deleting the report ...", null);
                        database.collection("reports")
                                .document(reportId)
                                .delete()
                                .addOnFailureListener(e->loadingDialog.showError(e.getMessage(), null,null))
                                .addOnSuccessListener(unused -> {
                                    FirebaseStorage.getInstance().getReference()
                                            .child("reports/"+reportId+".jpg")
                                            .delete();
                                    loadingDialog.showSuccess("The report is permanently deleted.", d -> onBackPressed());
                                });
                    })
                    .show());

            binding.editButton.setVisibility(View.VISIBLE);
            binding.editButton.setOnClickListener(v-> {
                Intent intent = new Intent(this, EditReportActivity.class);
                intent.putExtra(EditReportActivity.ARG_REPORT, report);
                startActivity(intent);
            });
        }

        binding.commentsButton.setOnClickListener(view -> Snackbar.make(view, "User construction feature.", Snackbar.LENGTH_SHORT)
                .setAnchorView(view).show());

    }
}