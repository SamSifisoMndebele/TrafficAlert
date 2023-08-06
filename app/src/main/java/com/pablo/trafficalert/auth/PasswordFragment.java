package com.pablo.trafficalert.auth;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.pablo.trafficalert.R;
import com.pablo.trafficalert.databinding.FragmentPasswordBinding;
import com.pablo.trafficalert.utils.Constants;

import java.util.Objects;

public class PasswordFragment extends Fragment {
    public final static String ARG_EMAIL = "arg_email";
    private FragmentPasswordBinding binding;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPasswordBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        String email = getArguments() != null ? getArguments().getString(ARG_EMAIL) : null;
        if (email != null) {
            Objects.requireNonNull(binding.emailTextInput.getEditText()).setText(email);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText emailInput = Objects.requireNonNull(binding.emailTextInput.getEditText());
        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.emailTextInput.setErrorEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.signUpButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(PasswordFragment.this);
            navController.navigateUp();
            navController.navigate(R.id.action_signInFragment_to_signUpFragment);
        });
        binding.signInButton.setOnClickListener(v -> NavHostFragment
                .findNavController(PasswordFragment.this)
                .navigateUp());

        binding.sendResetButton.setOnClickListener (v->{
            v.setEnabled(false);

            String emailAddress = emailInput.getText().toString().trim();
            if (emailAddress.isEmpty()) {
                binding.emailTextInput.setError("Email Required.");
                v.setEnabled(true);
            } else if (!emailAddress.matches(Constants.EMAIL_PATTERN)){
                binding.emailTextInput.setError("Email invalid.");
                v.setEnabled(true);
            } else {
                AuthActivity.loadingDialog.showProgress("Sending an email...", null);
                auth.sendPasswordResetEmail(emailAddress)
                        .addOnFailureListener(e -> {
                            AuthActivity.loadingDialog.showError(e.getMessage(), null,null);
                            v.setEnabled(true);
                        })
                        .addOnSuccessListener(unused -> AuthActivity.loadingDialog.showSuccess("Email sent successfully", dialog -> findNavController(this).navigateUp()));
            }
        });
    }
}