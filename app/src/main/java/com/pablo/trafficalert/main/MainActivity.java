package com.pablo.trafficalert.main;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pablo.trafficalert.R;
import com.pablo.trafficalert.auth.AuthActivity;
import com.pablo.trafficalert.databinding.ActivityMainBinding;
import com.pablo.trafficalert.models.UserInfo;
import com.pablo.trafficalert.profile.ProfileActivity;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    NavController navController;
    @NotNull
    public static final MutableLiveData<UserInfo> userInfo = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration
                .Builder(R.id.navigation_dashboard, R.id.navigation_report)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);


        FirebaseUser firebaseUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser());
        DocumentReference userDocRef = FirebaseFirestore.getInstance()
                .collection("users").document(firebaseUser.getUid());
        userDocRef.get()
                .addOnSuccessListener(userDocumentSnapshot -> {
                    UserInfo userInfo = userDocumentSnapshot.toObject(UserInfo.class);
                    boolean isNewProfile = userInfo == null || userInfo.getUid().isEmpty() ||
                            userInfo.getEmail().isEmpty();

                    if (isNewProfile) {
                        UserInfo newUserInfo = new UserInfo(firebaseUser.getUid(), Objects.requireNonNull(firebaseUser.getEmail()));
                        userDocRef.set(newUserInfo)
                                .addOnSuccessListener(unused -> MainActivity.userInfo.setValue(newUserInfo))
                                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());

                        new MaterialAlertDialogBuilder(this)
                                .setCancelable(false)
                                .setTitle("Your profile is not complete.")
                                .setPositiveButton("Complete Profile", (d,i)->{
                                    d.dismiss();
                                    startActivity(new Intent(this, ProfileActivity.class));
                                })
                                .show();
                    } else {
                        MainActivity.userInfo.setValue(userInfo);
                        boolean isProfileNotComplete = userInfo.getUid().isEmpty() || userInfo.getEmail().isEmpty() || userInfo.getNames() == null || userInfo.getLastName() == null || userInfo.getPhoneNumber() == null;
                        if (isProfileNotComplete) {
                            new MaterialAlertDialogBuilder(this)
                                    .setCancelable(false)
                                    .setTitle("Your profile is not complete.")
                                    .setPositiveButton("Complete Profile", (d,i)->{
                                        d.dismiss();
                                        startActivity(new Intent(this, ProfileActivity.class));
                                    })
                                    .show();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem profileItem = menu.getItem(0);

        userInfo.observe(this, info -> {
            Glide.with(this)
                    .asDrawable()
                    .load(info.getImageUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            profileItem.setIcon(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            profileItem.setIcon(placeholder);
                        }
                    });
            profileItem.setVisible(true);
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (item.getItemId() == R.id.item_logout) {
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