package com.example.faceattendance;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Map;

public class face_recognition {
    private Interpreter interpreter;
    private int INPUT_SIZE;
    private int height=0;
    Context ctx;
    ArrayList<String> ar=new ArrayList<>();
public String faceName;
    private int width=0;
    private GpuDelegate gpuDelegate=null;
    private CascadeClassifier cascadeClassifier;
    face_recognition(AssetManager assets, Context context,String model_path,int input_size) throws IOException{
INPUT_SIZE=input_size;
this.ctx=context;
        Interpreter.Options options;
        CompatibilityList compatList = new CompatibilityList();


        try {
             options =new Interpreter.Options();
            NnApiDelegate nnApiDelegate = null;
// Initialize interpreter with NNAPI delegate for Android Pie or above
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                nnApiDelegate = new NnApiDelegate();
                options.addDelegate(nnApiDelegate);
            }
            interpreter=new Interpreter(loadModel(assets,model_path),options);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                options = new Interpreter.Options();
                options.addDelegate(new GpuDelegate());
                interpreter = new Interpreter(loadModel(assets, model_path), options);
            } catch (Exception i) {
                options = new Interpreter.Options();
                try {
                    interpreter = new Interpreter(loadModel(assets, model_path), options);
                } catch (Exception j) {
                    j.printStackTrace();

                }
            }
        }




        Log.d("face_recognition","Model Is Successfully Loaded");



        try{
            InputStream inputStream=context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            File cascadeDir=context.getDir("cascade",Context.MODE_PRIVATE);
            File mCascadeFile=new File(cascadeDir,"haarcascade_frontalface_alt");
            FileOutputStream fileOutputStream=new FileOutputStream(mCascadeFile);
            byte[] buffer=new byte[4096];
            int byteRead;
            while((byteRead=inputStream.read(buffer))!=-1){
                fileOutputStream.write(buffer,0,byteRead);
            }
            inputStream.close();
            fileOutputStream.close();
            cascadeClassifier =new CascadeClassifier(mCascadeFile.getAbsolutePath());
           if(cascadeClassifier.empty()){
            Log.d("face_recognition","Classifier Failed to Load");
           cascadeClassifier=null;
           }
           else
               Log.d("face_recognition","Classifier is Loaded");

           cascadeDir.delete();

        }
        catch (IOException e){


            e.printStackTrace();
            Log.d("face_recognition", "Failed to load cascade. Exception thrown: " + e);
        }


    }

    public Mat recognizeImage(Mat matImage){
     Core.flip(matImage.t(),matImage,1);

    Mat greyScaleImage=new Mat();
        Imgproc.cvtColor(matImage,greyScaleImage,Imgproc.COLOR_RGBA2GRAY);
        height=greyScaleImage.height();
        width=greyScaleImage.width();
        int absoluteFaceSize=(int) (height*0.1);
        MatOfRect faces=new MatOfRect();
        if(cascadeClassifier!=null){
            cascadeClassifier.detectMultiScale(greyScaleImage,faces,1.1,2,2,
           new Size(absoluteFaceSize,absoluteFaceSize),new Size());
        }
        Rect[] faceArray=faces.toArray();
        for(int i=0;i<faceArray.length;i++){
            Imgproc.rectangle(matImage,faceArray[i].tl(),faceArray[i].br(),new Scalar(0,255,0,255),2);
            Rect roi=new Rect((int)faceArray[i].tl().x,(int)faceArray[i].tl().y,
                    ((int)faceArray[i].br().x)-((int)faceArray[i].tl().x),
                    ((int)faceArray[i].br().y)-((int)faceArray[i].tl().y));
            Mat croppedRgb=new Mat(matImage,roi);
            Bitmap bitmap=null;
            bitmap=Bitmap.createBitmap(croppedRgb.cols(),croppedRgb.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(croppedRgb,bitmap);
            Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap,INPUT_SIZE,INPUT_SIZE,false);
            ByteBuffer byteBuffer=createBitmapToByteBuffer(scaledBitmap);

float[][] faceValue=new float[1][1];
interpreter.run(byteBuffer,faceValue);
Log.d("face_Recognition","out: "+ Array.get(Array.get(faceValue,0),0));

        float readFace=(float)Array.get(Array.get(faceValue,0),0);
        faceName=readFaceName(readFace);




        Imgproc.putText(matImage,faceName,
                new Point((int)faceArray[i].tl().x+10,(int)faceArray[i].tl().y+20),
                1,1.5,new Scalar(255,255,255,150),2);

        }




        Core.flip(matImage.t(),matImage,0);
        return matImage;
    }

    private String readFaceName(float readFace) {
        String val="";
        if(readFace>0 & readFace<0.5)
            val="Courteny Cox";
        else if(readFace>=0.5 & readFace<1.5)
            val="Arnold Schwarzenegger";
        else if(readFace>=1.5 & readFace<2.5)
            val="Bhuvan bam";
        else if(readFace>=2.5 & readFace<3.5)
            val="Hardik Pandya";
        else if(readFace>=3.5 & readFace<4.5)
            val="David Schwimmer";
        else if(readFace>=4.5 & readFace<5.5)
            val="Matt LeBlanc";
        else if(readFace>=5.5 & readFace<6.5)
            val="Simon Helberg";
        else if(readFace>=6.5 & readFace<7.5)
            val="Scarlett johansson";
        else if(readFace>=7.5 & readFace<8.5)
            val="Pankaj Tripathi";
        else if(readFace>=8.5 & readFace<9.5)
            val="Matthew Perry";
        else if(readFace>=9.5 & readFace<10.5)
            val="Sylvester Stallone";
        else if(readFace>=10.5 & readFace<11.5)
            val="Lional Messi";
        else if(readFace>=11.5 & readFace<12.5)
            val="Jim Parsons";
        else if(readFace>=12.5 & readFace<13.5)
            val="Random Person";
        else if(readFace>=13.5 & readFace<14.5)
            val="Lisa Kudrow";
        else if(readFace>=14.5 & readFace<15.5)
            val="Mohammed Ali";
        else if(readFace>=15.5 & readFace<16.5)
            val="Brad Pitt";
        else if(readFace>=16.5 & readFace<17.5)
            val="Ronaldo";
        else if(readFace>=17.5 & readFace<18.5)
            val="Virat Kohli";
        else if(readFace>=18.5 & readFace<19.5)
            val="Angelina Jolie";
        else if(readFace>=19.5 & readFace<20.5)
            val="Kunal Nayya";
        else if(readFace>=20.5 & readFace<21.5)
            val="Manoj Bajpayee";
        else if(readFace>=21.5 & readFace<22.5)
            val="Sachin Tendulkar";
        else if(readFace>=22.5 & readFace<23.5)
            val="Jennifer Aniston";
        else if(readFace>=23.5 & readFace<24.5)
            val="Dhoni";
        else if(readFace>=24.5 & readFace<25.5)
            val="Pewdiepie";
        else if(readFace>=25.5 & readFace<26.5)
            val="Aishwarya Rai";
        else if(readFace>=26.5 & readFace<27.5)
            val="Johnny Galeck";
        else if(readFace>=27.5 & readFace<28.5)
            val="Rohit Sharma";
        else if(readFace>=28.5 & readFace<29.5)
            val="Suresh Raina";

        String val1=val+" is Present";
        if(ar.contains(val1) ){
            }
        else if(val!=null && val!="")
        ar.add(val1);
        return val;

    }
    public ArrayList<String> values(){
        return ar;
    }


    private ByteBuffer createBitmapToByteBuffer(Bitmap scaledBitmap) {
        ByteBuffer byteBuffer;
        int inputSize=INPUT_SIZE;
        byteBuffer=ByteBuffer.allocateDirect(4*1*inputSize*inputSize*3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] values=new int[inputSize*inputSize];
        scaledBitmap.getPixels(values,0,scaledBitmap.getWidth(),0,0,scaledBitmap.getWidth(),scaledBitmap.getHeight());
        int pixels=0;
        for(int i=0;i<inputSize;++i){
            for(int j=0;j<inputSize;++j){
final int val=values[pixels++];
byteBuffer.putFloat((((val>>16)&0xFF))/255.0f);
                byteBuffer.putFloat((((val>>8)&0xFF))/255.0f);
                byteBuffer.putFloat(((val & 0xFF))/255.0f);



            }

        }
        return byteBuffer;
    }

    private MappedByteBuffer loadModel(AssetManager assets, String model_path) throws  IOException{
        AssetFileDescriptor assetFileDescriptor=assets.openFd(model_path);
        FileInputStream fileInputStream=new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel=fileInputStream.getChannel();
        long startOffset=assetFileDescriptor.getStartOffset();
        long declaredLength =assetFileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }


}
