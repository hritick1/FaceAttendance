package com.example.faceattendance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  {
    public static final String TEXT ="Hello.text";
    static{
        if(OpenCVLoader.initDebug()){
            Log.d("cameraActivity","OpenCv Is loaded");

        }
        else{
            Log.i("CameraActivity","OpenCv NOT loaded");
        }}
    private Button camera_Button,textButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera_Button=findViewById(R.id.camera_button);



        camera_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        textButton=findViewById(R.id.show_text);
        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> ar=getIntent().getExtras().getStringArrayList(CameraActivity.TEXT);
//                for(int i=0;i<ar.size();i++){
//            if(ar.get(i)!=null && ar.get(i)!="")
//            Toast.makeText(MainActivity.this, "The name is: "+ar.get(i), Toast.LENGTH_SHORT).show();
//        }

                Intent intent=new Intent(MainActivity.this,AttendanceActivity.class);
                intent.putStringArrayListExtra(TEXT,ar);
                startActivity(intent);
            }
        });


    }
}