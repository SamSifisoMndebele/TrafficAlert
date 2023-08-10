package com.pablo.trafficalert.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pablo.trafficalert.databinding.FragmentSignUpBinding;
import com.pablo.trafficalert.main.MainActivity;
import com.pablo.trafficalert.models.UserInfo;
import com.pablo.trafficalert.utils.Constants;
import com.pablo.trafficalert.utils.LoadingDialog;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SignUpFragment extends Fragment {
    public final static String ARG_EMAIL = "arg_email";
    private FragmentSignUpBinding binding;
    private LoadingDialog loadingDialog;

    private final FirebaseFirestore database = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        loadingDialog = new LoadingDialog(requireActivity());
        String email = getArguments() != null ? getArguments().getString(ARG_EMAIL) : null;
        if (email != null) {
            Objects.requireNonNull(binding.emailTextInput.getEditText()).setText(email);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.signInButton.setOnClickListener(view1 -> NavHostFragment
                .findNavController(SignUpFragment.this)
                .navigateUp());

        binding.isAdminSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                binding.verificationCodeTextInput.setVisibility(View.VISIBLE);
            } else {
                binding.verificationCodeTextInput.setVisibility(View.GONE);
            }
        });

        binding.signUpButton.setOnClickListener(view1 -> validateAndSignUp());
    }

    private void validateAndSignUp() {
        EditText namesEditText = Objects.requireNonNull(binding.namesTextInput.getEditText());
        if (namesEditText.getText().toString().isEmpty()){
            namesEditText.setError("Name is required");
            return;
        }
        EditText lastNameEditText = Objects.requireNonNull(binding.lastNameTextInput.getEditText());
        if (lastNameEditText.getText().toString().isEmpty()){
            lastNameEditText.setError("Lastname is required");
            return;
        }
        EditText numberEditText = Objects.requireNonNull(binding.numberTextInput.getEditText());
        if (numberEditText.getText().toString().isEmpty()){
            numberEditText.setError("Phone number is required");
            return;
        } else if (!numberEditText.getText().toString().matches(Constants.PHONE_PATTERN)) {
            numberEditText.setError("Phone number is not valid");
            return;
        }
        EditText emailEditText = Objects.requireNonNull(binding.emailTextInput.getEditText());
        if (emailEditText.getText().toString().isEmpty()){
            emailEditText.setError("Email is required");
            return;
        } else if (!emailEditText.getText().toString().matches(Constants.EMAIL_PATTERN)) {
            emailEditText.setError("Email is not valid");
            return;
        }

        EditText passwordEditText = Objects.requireNonNull(binding.passwordTextInput.getEditText());
        if (passwordEditText.getText().toString().isEmpty()){
            passwordEditText.setError("Password is required");
            return;
        }
        EditText passwordConfirmEditText = Objects.requireNonNull(binding.passwordConfirmTextInput.getEditText());
        if (!passwordEditText.getText().toString().equals(passwordConfirmEditText.getText().toString())){
            passwordConfirmEditText.setError("Passwords do not match");
            return;
        }

        if (binding.isAdminSwitch.isChecked()) {
            EditText verificationCodeEditText = Objects.requireNonNull(binding.verificationCodeTextInput.getEditText());
            if (verificationCodeEditText.getText().toString().isEmpty()){
                verificationCodeEditText.setError("Verification code is required");
                return;
            }
        }

        signUp(namesEditText.getText().toString().trim(),
                lastNameEditText.getText().toString().trim(),
                emailEditText.getText().toString().trim(),
                numberEditText.getText().toString().trim(),
                passwordEditText.getText().toString());
    }

    private void signUp(@NotNull String names,@NotNull String lastName,@NotNull String email,@NotNull String number,@NotNull String password) {
        loadingDialog.showProgress("Creating Account...", null);

        if (binding.isAdminSwitch.isChecked()) {
            database.collection("App")
                    .document("AppInfo")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String verificationCode = documentSnapshot.getString("adminVerificationCode");
                        if (Objects.requireNonNull(binding.verificationCodeTextInput.getEditText())
                                .getText().toString().equals(verificationCode)) {
                            auth.createUserWithEmailAndPassword(email,password)
                                    .addOnSuccessListener(authResult -> {
                                        loadingDialog.setText("Saving user data...");
                                        UserInfo userInfo = new UserInfo(Objects.requireNonNull(authResult.getUser()).getUid(),
                                                names,
                                                lastName,
                                                email,
                                                number, true);

                                        storeUserInfo(userInfo);
                                    })
                                    .addOnFailureListener(e -> loadingDialog.showError(e.getMessage(), null, null));
                        } else {
                            loadingDialog.showError("Incorrect admin verification code", null, null);
                        }
                    });
            }
        else {
            auth.createUserWithEmailAndPassword(email,password)
                    .addOnSuccessListener(authResult -> {
                        loadingDialog.setText("Saving user data...");
                        UserInfo userInfo = new UserInfo(Objects.requireNonNull(authResult.getUser()).getUid(),
                                names,
                                lastName,
                                email,
                                number);

                        storeUserInfo(userInfo);
                    })
                    .addOnFailureListener(e -> loadingDialog.showError(e.getMessage(), null, null));
        }
    }

    private int trials = 0;
    private void storeUserInfo(@NotNull UserInfo userInfo) {
        database.collection("users")
                .document(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .set(userInfo)
                .addOnSuccessListener(unused -> loadingDialog.showSuccess(null, dialog -> {
                    startActivity(new Intent(requireActivity(), MainActivity.class));
                    requireActivity().finishAffinity();
                }))
                .addOnFailureListener(e -> {
                    trials++;
                    if (trials <= 3)
                        storeUserInfo(userInfo);
                    else {
                        loadingDialog.showError("Signing up failed.\n${it.message}", null, null);
                        auth.getCurrentUser().delete();
                    }
                });
    }

}