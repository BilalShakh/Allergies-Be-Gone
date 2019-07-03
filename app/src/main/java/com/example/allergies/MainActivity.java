package com.example.allergies;

import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final String apiEndpoint = "https://eastus.api.cognitive.microsoft.com/face/v1.0";

    private final String subscriptionKey = "f776aad99025497e978d029b48cfa3b5";

    private final FaceServiceClient faceServiceClient = new FaceServiceRestClient(apiEndpoint, subscriptionKey);

    private String Result = "";

    private Bitmap face;

    private ArrayList<String> symptomFoodList = new ArrayList<>();

    private ArrayList<String> symptomActList = new ArrayList<>();

    private ArrayList<String> symptomEmotionList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText eatingEditText = findViewById(R.id.eatingEditText);
        final EditText actionsEditText = findViewById(R.id.actionsEditText);
        final TextView emotionTextView = findViewById(R.id.emotionTextView);



        //all button event handlers
        Button captureBtn = findViewById(R.id.captureBtn);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,0);
            }
        });

        Button dataBtn = findViewById(R.id.dataBtn);
        dataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectAndFrame(face);
            }
        });

        Button addBtn  = findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String food = eatingEditText.getText().toString();
                String action = actionsEditText.getText().toString();
                String emotion = emotionTextView.getText().toString();
                if(food!="" && action!="" && emotion!="Emotions here..."){
                    symptomFoodList.add(food);
                    symptomActList.add(action);
                    symptomEmotionList.add(emotion);
                    clearText();
                }
            }
        });

        Button resultBtn = findViewById(R.id.resultBtn);
        resultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(symptomFoodList.size()>0 & emotionTextView.getText().toString()!="Emotions here..."){
                    displayHypothesis(true,eatingEditText.getText().toString(),actionsEditText.getText().toString(),emotionTextView.getText().toString());
                }
                else{
                    displayHypothesis(false,"","","");
                }
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

                        TextView resultTextView = findViewById(R.id.emotionTextView);
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

    private void displayHypothesis(boolean showOrNot,String food,String action,String emotion){
        setContentView(R.layout.activity_main);
        TextView hypthesisTextView = findViewById(R.id.hypothesisTextView);
        String result = "You are unlikely to have an allergic reaction";
        int listSize = symptomActList.size();


        if(showOrNot){
            for(int i=0;i<listSize;i++){
                if(food == symptomFoodList.get(i)|action==symptomActList.get(i)|emotion==symptomEmotionList.get(i)){
                    result = "You might have an allergic reaction,be careful.";
                    break;
                }
            }

        }
        else{
            result = "Not enough Data";
        }

        hypthesisTextView.setText(result);
    }

    private void clearText() {
        setContentView(R.layout.activity_main);
        EditText eatingEditText = findViewById(R.id.eatingEditText);
        EditText actionsEditText = findViewById(R.id.actionsEditText);
        TextView emotionTextView = findViewById(R.id.emotionTextView);

        eatingEditText.setText("");
        actionsEditText.setText("");
        emotionTextView.setText("Emotions here...");
    }

}
