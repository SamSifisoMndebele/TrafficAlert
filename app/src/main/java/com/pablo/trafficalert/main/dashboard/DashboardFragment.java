package com.pablo.trafficalert.main.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pablo.trafficalert.R;
import com.pablo.trafficalert.databinding.FragmentDashboardBinding;
import com.pablo.trafficalert.main.MainActivity;
import com.pablo.trafficalert.models.UserInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private SharedPreferences preferences;
    private UserInfo userInfo;
    
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        preferences = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);

        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.userInfo.observe(getViewLifecycleOwner(), userInfo -> {
            this.userInfo = userInfo;
            startListener(null);
        });
    }

    private void startListener(ViewCodes code) {
        ViewCodes viewCode = code != null ? code :
                ViewCodes.valueOf(preferences.getString("view_code", ViewCodes.ALL_REPORTS.name())) ;

        Query query = viewCode == ViewCodes.ALL_REPORTS ?
                database.collection("reports")
                        .orderBy("timestamp", Query.Direction.DESCENDING) :
                database.collection("reports")
                        .whereEqualTo("userInfo.email", userInfo.getEmail())
                        .orderBy("timestamp", Query.Direction.DESCENDING) ;

        ReportsAdapter adapter = new ReportsAdapter((MainActivity) requireActivity(), query);
        binding.reportsList.setAdapter(adapter);
        binding.reportsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.reportsList.setHasFixedSize(true);
        adapter.startListening();
    }


    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.dashboard_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == R.id.item_all_reports) {
            preferences.edit().putString("status_code", ViewCodes.ALL_REPORTS.name()).apply();
            startListener(ViewCodes.ALL_REPORTS);
            return true;
        }
        if (item.getItemId() == R.id.item_my_reports) {
            preferences.edit().putString("status_code", ViewCodes.MY_REPORTS.name()).apply();
            startListener(ViewCodes.MY_REPORTS);
            return true;
        }
        return false;
    }
}