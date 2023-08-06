package com.pablo.trafficalert.auth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.pablo.trafficalert.databinding.FragmentSignUpBinding;
import java.util.Objects;

public class SignUpFragment extends Fragment {
    public final static String ARG_EMAIL = "arg_email";
    private FragmentSignUpBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
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
    }



}