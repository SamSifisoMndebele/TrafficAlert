package com.pablo.trafficalert.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.pablo.trafficalert.R;
import com.pablo.trafficalert.auth.AuthActivity;
import com.pablo.trafficalert.databinding.ActivityProfileBinding;
import com.pablo.trafficalert.main.MainActivity;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        MainActivity.userInfo.observe(this, userInfo -> {
            if (userInfo.isAdmin()){
                binding.userDescription.setVisibility(View.VISIBLE);
                binding.userDescription.setText("Admin");
            }

            String username = "";
            if (userInfo.getNames() != null){
                username += Arrays.stream(userInfo.getNames().split(" ")).map(s -> s.toCharArray()[0]+"").collect(Collectors.joining(""));
                username += " ";
            }
            if (userInfo.getLastName() != null) {
                username += userInfo.getLastName();
            }
            binding.userName.setText(username);

            Glide.with(binding.userImage)
                    .load(userInfo.getImageUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(binding.userImage);

            binding.firstNames.setText(userInfo.getNames());
            binding.lastName.setText(userInfo.getLastName());
            binding.phoneNumber.setText(userInfo.getPhoneNumber());
            binding.emailAddress.setText(userInfo.getEmail());
        });

        binding.buildingLayout.setVisibility(View.GONE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_logout) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Logout?")
                    .setPositiveButton(R.string.logout, (dialog, i)-> {
                        dialog.dismiss();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this, AuthActivity.class));
                        finishAffinity();
                    })
                    .show();
            return true;
        }
        return false;
    }
}