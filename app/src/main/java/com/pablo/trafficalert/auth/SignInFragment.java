package com.pablo.trafficalert.auth;

import android.content.Intent;
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
import com.pablo.trafficalert.databinding.FragmentSignInBinding;
import com.pablo.trafficalert.main.MainActivity;
import com.pablo.trafficalert.utils.Constants;
import com.pablo.trafficalert.utils.Utils;

import java.util.Objects;

public class SignInFragment extends Fragment {
    private FragmentSignInBinding binding;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setInputValidationListener();

        binding.signUpButton.setOnClickListener(v -> {
            Utils.hideKeyboard(requireContext(), v);
            NavHostFragment.findNavController(SignInFragment.this)
                    .navigate(R.id.action_signInFragment_to_signUpFragment);
        });
        binding.forgotPasswordButton.setOnClickListener(v -> {
            Utils.hideKeyboard(requireContext(), v);
            String email = Objects.requireNonNull(binding.emailTextInput.getEditText())
                    .getText().toString().trim();
            NavController navController = NavHostFragment.findNavController(SignInFragment.this);
            if (email.isEmpty()) {
                navController.navigate(R.id.action_signInFragment_to_passwordFragment);
            } else {
                Bundle arguments = new Bundle();
                arguments.putString(PasswordFragment.ARG_EMAIL, email);
                navController.navigate(R.id.action_signInFragment_to_passwordFragment, arguments);
            }
        });
        binding.loginButton.setOnClickListener(btn -> {
            Utils.tempDisable(btn);

            String email = Objects.requireNonNull(binding.emailTextInput.getEditText())
                    .getText().toString().trim();
            String password = Objects.requireNonNull(binding.passwordTextInput.getEditText())
                    .getText().toString().trim();

            if (email.isEmpty()){
                binding.emailTextInput.setError("Enter your email address.");
                return;
            }
            if (!email.matches(Constants.EMAIL_PATTERN)){
                binding.emailTextInput.setError("Invalid email address.");
                return;
            }
            if (password.isEmpty()){
                binding.passwordTextInput.setError("Enter your password.");
                return;
            }

            AuthActivity.loadingDialog.showProgress(getString(R.string.signing_in),  null);
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        // Sign in success, update UI with the signed-in user's information
                        AuthActivity.loadingDialog.showSuccess("Signed in successfully.", d -> {
                            startActivity(new Intent(requireActivity(), MainActivity.class));
                            requireActivity().finishAffinity();
                        });
                    })
                    .addOnFailureListener(e -> {
                        // If sign in fails, display a message to the user.
                        if (Objects.equals(e.getMessage(), "There is no user record corresponding to this identifier. The user may have been deleted.")) {
                            AuthActivity.loadingDialog.showError(e.getMessage(), null, dialog -> {
                                Bundle arguments = new Bundle();
                                arguments.putString(SignUpFragment.ARG_EMAIL, email);
                                NavHostFragment.findNavController(SignInFragment.this)
                                        .navigate(R.id.action_signInFragment_to_signUpFragment, arguments);
                            });
                        } else AuthActivity.loadingDialog.showError(e.getMessage(), null,null);
                    });
        });
    }

    private void setInputValidationListener() {
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
        emailInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !emailInput.getText().toString().trim().isEmpty() &&
                    !emailInput.getText().toString().trim().matches(Constants.EMAIL_PATTERN)) {
                binding.emailTextInput.setError("Invalid email address.");
            }
        });


        EditText passwordInput = Objects.requireNonNull(binding.passwordTextInput.getEditText());
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence password, int start, int before, int count) {
                binding.passwordTextInput.setErrorEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}