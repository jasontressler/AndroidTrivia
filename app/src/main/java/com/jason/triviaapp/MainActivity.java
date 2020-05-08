package com.jason.triviaapp;

import androidx.appcompat.app.AppCompatActivity;
import android.animation.*;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.*;
import java.net.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MainActivity extends AppCompatActivity {

    private AnimatorSet setOut;
    private AnimatorSet setIn;
    private View cardFront;
    private View cardBack;
    private View cardLayout;
    private boolean frontVisible = true;

    static TextView txtCategory;
    static TextView txtQuestion;
    static TextView txtAnswer;
    static Button btnQuestion;
    static Question q;

    Bundle qBundle = new Bundle();
    Bundle aBundle = new Bundle();

    //region connection Runnable
    Runnable connect = new Runnable() {
        public void run() {
            // Get question from web service
            String result;
            ObjectMapper mapper = new ObjectMapper();
            try {
                // Parse the URL
                URL url = new URL("http://jservice.io/api/random");
                // Connect
                URLConnection sock = url.openConnection();
                // Read
                BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                result = reader.readLine();
                result = result.substring(1, result.length() - 1);
                Log.v("Result", result);
                q = mapper.readValue(result, Question.class);
                // Close
                reader.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Strip data from question
            final String answer = Cap(q.getAnswer().replace("<i>", "")
                    .replace("</i>", ""));
            final String question = Cap(q.getQuestion());
            final String category = Cap(q.getCategory().getTitle()) + ":";

            //Bundle data
            qBundle.putString("question", question);
            qBundle.putString("category", category);
            aBundle.putString("answer", answer);

            //Send out question data
            Message qMessage = qHandler.obtainMessage();
            qMessage.setData(qBundle);
            qHandler.sendMessage(qMessage);
        }
    };
    //endregion

    private static final Handler qHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            String question = msg.getData().getString("question");
            String category = msg.getData().getString("category");
            txtCategory.setText(category);
            txtQuestion.setText(question);
        }
    };
    private static final Handler aHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String answer = msg.getData().getString("answer");
            txtAnswer.setText(answer);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        loadAnimations();
        cameraDistance();

        btnQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!frontVisible)
                    flipCard(cardLayout);
                new Thread(connect).start();

            }
        });
    }

    private void cameraDistance(){
        int distance = 8000;
        float scale = getResources().getDisplayMetrics().density * distance;
        cardFront.setCameraDistance(scale);
        cardBack.setCameraDistance(scale);
    }
    private void findViews(){
        cardFront = findViewById(R.id.card_front);
        cardBack = findViewById(R.id.card_back);
        cardLayout = findViewById(R.id.cardLayout);
        txtCategory = findViewById(R.id.txtCategory);
        txtQuestion = findViewById(R.id.txtQuestion);
        txtAnswer = cardBack.findViewById(R.id.txtAnswer);
        btnQuestion = findViewById(R.id.btnQuestion);

    }
    private void loadAnimations(){
        setOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.anim_out);
        setIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.anim_in);
    }
    private String Cap(String s){
        s = s.substring(0,1).toUpperCase() + s.substring(1);
        return s;
    }
    public void flipCard(View view){
        if (frontVisible){
            setOut.setTarget(cardFront);
            setIn.setTarget(cardBack);
            setOut.start();
            setIn.start();
            frontVisible = false;
            Message aMessage = aHandler.obtainMessage();
            aMessage.setData(aBundle);
            aHandler.sendMessage(aMessage);
        } else {
            setOut.setTarget(cardBack);
            setIn.setTarget(cardFront);
            setOut.start();
            setIn.start();
            frontVisible = true;
        }
    }
}
