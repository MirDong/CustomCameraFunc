package com.example.v_zakudong.customcamerafunc;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {
    private Spinner spinner;
    private String[] stringArray;
    private SharedPreferences sp_resoltion;
    private TextView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        back = (TextView)findViewById(R.id.back);
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
        if (sp_resoltion.getString("resolution","480×640").equals("480×640")){
            spinner.setSelection(0);
            setResolution(0);
        }else {
            spinner.setSelection(1);
            setResolution(1);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(SettingActivity.this, "分辨率:" + stringArray[position], Toast.LENGTH_SHORT).show();
                setResolution(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        });
    }

    //设置分辨率
    public void setResolution(int position) {
        /*String[] strings = stringArray[position].split("×");
        resolution_height = strings[0];//高
        resolution_width = strings[1];//宽
        preview.setParameters(Integer.valueOf(resolution_width), Integer.valueOf(resolution_height));*/
        sp_resoltion.edit().putString("resolution",stringArray[position]).commit();
    }
}
