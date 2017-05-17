package com.example.v_zakudong.customcamerafunc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.v_zakudong.customcamerafunc.fragment.CalcFragment;
import com.example.v_zakudong.customcamerafunc.fragment.OtherFragment;
import com.example.v_zakudong.customcamerafunc.fragment.TakePhotoFragment;
import com.example.v_zakudong.customcamerafunc.fragment.VideoRecorderFragment;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;
    private String[] perssions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};
    private RadioGroup rg_group;
    private RadioButton[] buttons;
    public ArrayList<Fragment>fragments = new ArrayList<>();
    private int current = 0;
    private Spinner spinner;
    private SharedPreferences sp_resoltion;
    private String[] stringArray;
    private ImageView settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPerssion(this);
        }
        setContentView(R.layout.activity_main);
        initView();
        initFragment();
//        initSpinner();
    }

    private void initFragment() {
        fragments.add(new TakePhotoFragment());
        fragments.add(new VideoRecorderFragment());
        fragments.add(new CalcFragment());
        fragments.add(new OtherFragment());
        getSupportFragmentManager().beginTransaction().add(R.id.linear_layout,fragments.get(0)).commit();
    }

    private void initView() {
        rg_group = (RadioGroup)findViewById(R.id.rb_group);
        settings = (ImageView)findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });
        final int count = rg_group.getChildCount();
        buttons = new RadioButton[count];
        for (int i = 0; i < count; i++) {
            buttons[i] = (RadioButton) rg_group.getChildAt(i);
        }
        buttons[0].setTextColor(Color.parseColor("#00c0be"));
        rg_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                //切换碎片
                for (int j = 0; j < count; j++) {
                    buttons[j].setTextColor(Color.parseColor("#989898"));
                    if (buttons[j].getId()==i){
                        buttons[j].setTextColor(Color.parseColor("#00c0be"));
                        switchFragment(j);
                    }
                }
            }
        });
    }

    private void switchFragment(int position) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        /*if (fragments.get(position).isAdded()){
            transaction.show(fragments.get(position)).hide(fragments.get(current)).commit();
        }else {
            transaction.add(R.id.linear_layout,fragments.get(position)).hide(fragments.get(current)).commit();
        }*/
        transaction.replace(R.id.linear_layout,fragments.get(position)).commit();
        current = position;
    }

    private void initSpinner() {
        spinner = (Spinner) findViewById(R.id.spinner);
        sp_resoltion = getSharedPreferences("sp_resolution", Context.MODE_PRIVATE);
        //Spinner下拉框
        spinner.setDropDownVerticalOffset(15);
        spinner.setGravity(Gravity.CENTER);
        spinner.setPrompt("请选择分辨率");
        stringArray = getResources().getStringArray(R.array.resolution);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stringArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (sp_resoltion.getString("resolution", "480×640").equals("480×640")) {
            spinner.setSelection(0);
            setResolution(0);
        } else {
            spinner.setSelection(1);
            setResolution(1);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "分辨率:" + stringArray[position], Toast.LENGTH_SHORT).show();
                setResolution(position);
                //使用接口回调，回调数据到碎片中
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    //动态添加权限
    private void checkPerssion(Activity activity) {
        int perssion_code = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        if (perssion_code != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, perssions, REQUEST_CODE);
        }
    }

    //设置分辨率
    public void setResolution(int position) {
        sp_resoltion.edit().putString("resolution", stringArray[position]).commit();
    }


}
