package com.example.v_zakudong.customcamerafunc.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.v_zakudong.customcamerafunc.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CalcFragment extends Fragment {
    private static final String TAG = "CalcFragment";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "执行了--------onAttach: ");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "执行了--------onCreate: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "执行了--------onCreateView: ");
        return inflater.inflate(R.layout.fragment_calc, container, false);
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "执行了--------onActivityCreated: ");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "执行了--------onStart: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "执行了--------onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "执行了--------onPause: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "执行了--------onStop: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "执行了--------onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "执行了--------onDestroy: ");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "执行了--------onDetach: ");
    }

}
