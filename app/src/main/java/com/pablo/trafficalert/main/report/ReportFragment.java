package com.pablo.trafficalert.main.report;

import static android.app.Activity.RESULT_OK;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pablo.trafficalert.R;
import com.pablo.trafficalert.databinding.FragmentReportBinding;
import com.pablo.trafficalert.main.MainActivity;
import com.pablo.trafficalert.models.ReportData;
import com.pablo.trafficalert.models.UserInfo;
import com.pablo.trafficalert.utils.LoadingDialog;
import com.pablo.trafficalert.utils.OpenPicturesContract;
import com.pablo.trafficalert.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReportFragment extends Fragment {

    private FragmentReportBinding binding;
    private LoadingDialog loadingDialog;

    private final FirebaseFirestore database = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private Uri imageUri = null;
    private final ActivityResultLauncher<String[]> selectImageResultLauncher = registerForActivityResult(new OpenPicturesContract(), imageUri -> {
        if (imageUri != null) {
            this.imageUri = imageUri;
            binding.reportImage.setImageURI(imageUri);
            binding.reportImageLayout.setVisibility(View.VISIBLE);
            binding.imageAddButton.setVisibility(View.GONE);
        }
    });
    private Uri tempImageUri = null;
    private final ActivityResultLauncher<Intent> startCameraResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            imageUri = tempImageUri;
            binding.reportImage.setImageURI(tempImageUri);
            binding.reportImageLayout.setVisibility(View.VISIBLE);
            binding.imageAddButton.setVisibility(View.GONE);
        }
    });
    private void launchCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        tempImageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);

        startCameraResultLauncher.launch(cameraIntent);
    }
    private void imageChooser() {
        if (imageUri != null) {
            CharSequence[] choice = {"Change image","Remove image"};
            new MaterialAlertDialogBuilder(requireContext())
                    .setItems(choice, ((dialog, item) -> {
                        switch (item) {
                            case 0: {
                                imageLauncher();
                                break;
                            }
                            case 1: {
                                binding.reportImage.setImageURI(null);
                                binding.reportImageLayout.setVisibility(View.GONE);
                                binding.imageAddButton.setVisibility(View.VISIBLE);
                                imageUri = null;
                            }
                        }
                    }))
                    .show();


        } else {
            imageLauncher();
        }
    }
    private void imageLauncher() {
        CharSequence[] choice = {"Choose from gallery","Take new image"};
        new MaterialAlertDialogBuilder(requireContext())
                .setItems(choice, ((dialog, item) -> {
                    switch (item) {
                        case 0: {
                            String[] launchArray = {"image/*"};
                            selectImageResultLauncher.launch(launchArray);
                            break;
                        }
                        case 1: {
                            launchCamera();
                        }
                    }
                }))
                .show();
    }

    private UserInfo userInfo = null;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("imageUri", imageUri);
        outState.putParcelable("userInfo", userInfo);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReportBinding.inflate(inflater, container, false);
        loadingDialog = new LoadingDialog(requireActivity());

        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable("imageUri");
            if (imageUri != null) {
                binding.reportImage.setImageURI(imageUri);
                binding.reportImageLayout.setVisibility(View.VISIBLE);
                binding.imageAddButton.setVisibility(View.GONE);
            }
            userInfo = savedInstanceState.getParcelable("userInfo");
            updateReporter(userInfo);
        } else {
            MainActivity.userInfo.observe(getViewLifecycleOwner(), userInfo -> {
                this.userInfo = userInfo;
                updateReporter(userInfo);
            });
        }

        return binding.getRoot();
    }

    private void updateReporter(@Nullable UserInfo userInfo) {
        if (userInfo == null) {
            binding.profilePicture.setImageResource(R.drawable.ic_person);
            binding.username.setText(R.string.anonymous);
            return;
        }

        Glide.with(binding.profilePicture)
                .load(userInfo.getImageUrl())
                .placeholder(R.drawable.ic_person)
                .into(binding.profilePicture);

        String username = "";
        if (userInfo.getNames() != null){
            username += Arrays.stream(userInfo.getNames().split(" "))
                    .map(s -> s.toCharArray()[0]+"").collect(Collectors.joining("")) + " ";
        }
        if (userInfo.getLastName() != null) {
            username += userInfo.getLastName();
        }
        binding.username.setText(username);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.postReportButton.setOnClickListener (this::addReport);
        binding.imageAddButton.setOnClickListener (v-> imageChooser());
        binding.reportImage.setOnClickListener (v-> imageChooser());
        binding.imageRemoveButton.setOnClickListener (v->{
            binding.reportImage.setImageURI(null);
            binding.reportImageLayout.setVisibility(View.GONE);
            binding.imageAddButton.setVisibility(View.VISIBLE);
            imageUri = null;
        });
        binding.reportUserLayout.setOnClickListener(v->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Report as anonymous?")
                        .setPositiveButton("Yes", (d, i)->{
                            userInfo = null;
                            updateReporter(null);
                        })
                        .setNegativeButton("No", (d, i)->{
                            userInfo = MainActivity.userInfo.getValue();
                            updateReporter(userInfo);
                        })
                        .setNeutralButton("Cancel", (d, i)-> d.dismiss())
                        .show());


        ArrayAdapter<CharSequence> reportTitleAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.report_title,
                android.R.layout.simple_spinner_dropdown_item);
        binding.titleSpinner.setAdapter(reportTitleAdapter);
        binding.titleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String text = adapterView.getItemAtPosition(position).toString();
                if (text.equals("Other")){
                    binding.titleCardView.setVisibility(View.VISIBLE);
                } else {
                    binding.titleCardView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (binding.titleSpinner.getSelectedItem() == "Other"){
            binding.titleCardView.setVisibility(View.VISIBLE);
        } else {
            binding.titleCardView.setVisibility(View.GONE);
        }
    }

    private void addReport(View view) {
        String spinnerTitle = binding.titleSpinner.getSelectedItem().toString();
        String fieldTitle = Objects.requireNonNull(binding.titleEditText.getText()).toString().trim();
        if (spinnerTitle.equals("Select report title") ||
                spinnerTitle.equals("Other") && fieldTitle.isEmpty()){
            Snackbar.make(view, "Report title is required.", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#EF5350"))
                    .setAnchorView(view).show();
            return;
        }

        String title = spinnerTitle.equals("Other") ? fieldTitle : spinnerTitle;
        String location = Objects.requireNonNull(binding.locationEditText.getText()).toString().trim();
        String description = Objects.requireNonNull(binding.descriptionEditText.getText()).toString().trim();

        if (location.isEmpty()) {
            Snackbar.make(view, "Report location is required.", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#EF5350"))
                    .setAnchorView(view).show();
            return;
        }
        if (imageUri == null && description.isEmpty()) {
            Snackbar.make(view, "Report image or description is required.", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#EF5350"))
                    .setAnchorView(view).show();
            return;
        }

        loadingDialog.showProgress(null, null);

        DocumentReference document = database.collection("reports").document();

        if (imageUri != null) {
            StorageReference reportsStorage = storage.getReference("reports")
                    .child(document.getId()+".jpg");
            UploadTask uploadTask = reportsStorage.putFile(imageUri);
            uploadTask
                    .continueWithTask(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            throw task.getException();
                        }
                        return reportsStorage.getDownloadUrl();
                    })
                    .addOnFailureListener(e -> loadingDialog.showError(e.getMessage(), null,null))
                    .addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        ReportData report = new ReportData(
                                document.getId(),
                                title,
                                location,
                                description,
                                userInfo,
                                imageUrl
                        );
                        addReport(report, document);
                    });
        } else {
            ReportData report = new ReportData(
                    document.getId(),
                    title,
                    location,
                    description,
                    userInfo
            );
            addReport(report, document);
        }
    }

    private void addReport(@NotNull ReportData report, @NotNull DocumentReference document) {
        document.set(report, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    loadingDialog.showSuccess("Report posted.", null);
                    Utils.runThisAfter(()->{
                        binding.locationEditText.setText(null);
                        binding.descriptionEditText.setText(null);
                        binding.titleEditText.setText(null);
                        binding.reportImage.setImageURI(null);
                        binding.reportImageLayout.setVisibility(View.GONE);
                        binding.imageAddButton.setVisibility(View.VISIBLE);
                        imageUri = null;
                        binding.titleSpinner.setSelection(0);
                    }, 1);
                })
                .addOnFailureListener(e -> loadingDialog.showError(e.getMessage(), null,null));
    }
}