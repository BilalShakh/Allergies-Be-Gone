package com.example.allergies;

import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final String apiEndpoint = "https://eastus.api.cognitive.microsoft.com/face/v1.0";

    // Replace `<Subscription Key>` with your subscription key.
    // For example, subscriptionKey = "0123456789abcdef0123456789ABCDEF"
    private final String subscriptionKey = "f776aad99025497e978d029b48cfa3b5";

    private final FaceServiceClient faceServiceClient = new FaceServiceRestClient(apiEndpoint, subscriptionKey);

    private String Result = "";

    private Bitmap face;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button captureBtn = findViewById(R.id.captureBtn);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,0);

                /*ImageView imageView = findViewById(R.id.imageView);
                imageView.invalidate();
                BitmapDrawable img = (BitmapDrawable) imageView.getDrawable();
                Bitmap face = img.getBitmap();*/
            }
        });

        Button dataBtn = findViewById(R.id.dataBtn);
        dataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectAndFrame(face);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==0){
            face = (Bitmap)data.getExtras().get("data");
        }
    }

    private void detectAndFrame(final Bitmap imageBitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        //Emotion emotion = new Emotion();

        final String[] resultS = {""};
        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    String exceptionMessage = "";

                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                  new FaceServiceClient.FaceAttributeType[] {
                                    FaceServiceClient.FaceAttributeType.Age,
                                    FaceServiceClient.FaceAttributeType.Gender,
                                    FaceServiceClient.FaceAttributeType.Emotion
                                  }

                            );
                            if (result == null){
                                publishProgress(
                                        "Detection Finished. Nothing detected");
                                return null;
                            }
                            publishProgress(String.format(
                                    "Detection Finished. %d face(s) detected",
                                    result.length));
                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Detection failed: %s", e.getMessage());
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog

                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                        String test = progress[0];
                    }
                    @Override
                    protected void onPostExecute(Face[] result) {
                        //TODO: update face frames

                        if(!exceptionMessage.equals("")){

                        }
                        if (result == null) return;

                        //int sum=0;

                        ArrayList<Double> emotionNums = AddResult(result);

                        /*for(int i = 0;i<emotionNums.size();i++){
                            sum+=emotionNums.get(i);
                        }*/

                        setContentView(R.layout.activity_main);

                        TextView resultTextView = findViewById(R.id.resultTextView);
                        resultTextView.setText("Suprise: "+ emotionNums.get(0)+"Anger: " + emotionNums.get(1)+"Contempt: " + emotionNums.get(2)+"Disgust: " + emotionNums.get(3)+"Fear: " + emotionNums.get(4)+"Happiness: " + emotionNums.get(5)+" Neutral: " + emotionNums.get(6)+"Sadness: " + emotionNums.get(7));

                    }
                };

        detectTask.execute(inputStream);
    }

    private ArrayList<Double> AddResult(Face[] result) {

        ArrayList<Double> emotionNums = new ArrayList<Double>();

            emotionNums.add(result[0].faceAttributes.emotion.surprise);
            emotionNums.add(result[0].faceAttributes.emotion.anger);
            emotionNums.add(result[0].faceAttributes.emotion.contempt);
            emotionNums.add(result[0].faceAttributes.emotion.disgust);
            emotionNums.add(result[0].faceAttributes.emotion.fear);
            emotionNums.add(result[0].faceAttributes.emotion.happiness);
            emotionNums.add(result[0].faceAttributes.emotion.neutral);
            emotionNums.add(result[0].faceAttributes.emotion.sadness);

            return emotionNums;
    }

}
