package com.pengl.williamchart.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.pengl.williamchart.demo.databinding.FragmentMenusBinding;

public class MenusFragment extends Fragment {

    private FragmentMenusBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMenusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnLineChartView.setOnClickListener(view1 ->
                NavHostFragment.findNavController(MenusFragment.this)
                        .navigate(R.id.action_menu_to_lineChart));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}