package com.pablo.trafficalert.main.dashboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.pablo.trafficalert.R;
import com.pablo.trafficalert.databinding.ItemReportBinding;
import com.pablo.trafficalert.main.FirestoreAdapter;
import com.pablo.trafficalert.main.MainActivity;
import com.pablo.trafficalert.main.report.ReportActivity;
import com.pablo.trafficalert.models.ReportData;
import com.pablo.trafficalert.models.UserInfo;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReportsAdapter extends FirestoreAdapter<ReportsAdapter.ReportViewHolder>{
    private final Activity activity;
    public ReportsAdapter(@NonNull Activity activity, @NonNull Query query) {
        super(query);
        this.activity = activity;
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        private final ItemReportBinding binding;
        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReportBinding.bind(itemView);
        }

        @SuppressLint("SetTextI18n")
        void bind(@NonNull DocumentSnapshot snapshot) {
            ReportData report = Objects.requireNonNull(snapshot.toObject(ReportData.class));

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
                binding.username.setText(itemView.getContext().getString(R.string.anonymous));
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


            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(activity, ReportActivity.class);
                intent.putExtra(ReportActivity.ARG_REPORT_ID, report.getId());
                intent.putExtra(ReportActivity.ARG_USER, MainActivity.userInfo.getValue());
                activity.startActivity(intent);
            });
        }

    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReportViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        DocumentSnapshot snapshot = getSnapshot(position);
        if (snapshot != null) holder.bind(snapshot);
    }

}
