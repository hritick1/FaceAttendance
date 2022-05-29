package com.example.faceattendance;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class AttendanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        ArrayList<String> ar=getIntent().getExtras().getStringArrayList(MainActivity.TEXT);
        ListView listView=findViewById(R.id.list_item);
        ArrayAdapter<String > adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,ar);
        listView.setAdapter(adapter);
    }
}