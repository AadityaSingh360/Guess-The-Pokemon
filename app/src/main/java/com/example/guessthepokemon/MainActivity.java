package com.example.guessthepokemon;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    Button button1, button2, button3, button4;
    ImageView pokemonImage;
    int chosenPokemon,correctAnswer;
    String[] links = new String[25];
    String[] names = new String[25];
    String[] answers = new String[4];

    public void chosen(View view){
        if(view.getTag().toString().equals(Integer.toString(correctAnswer)))
            Toast.makeText(getApplicationContext(), "CORRECT!!", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getApplicationContext(), "WRONG! It was " + names[chosenPokemon],Toast.LENGTH_LONG).show();
        try {
            createQuestion();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void createQuestion() throws ExecutionException, InterruptedException {
        Random random = new Random();
        int prevAnswer = 0;

        chosenPokemon = random.nextInt(24);
        while(prevAnswer == chosenPokemon){
            chosenPokemon = random.nextInt(24);
        }
        prevAnswer = chosenPokemon;
        System.out.println("Chosen Pokemon = " + chosenPokemon);

        ImageDownloader imgTask = new ImageDownloader();
        Bitmap image = imgTask.execute(links[chosenPokemon]).get();
        pokemonImage.setImageBitmap(image);

        correctAnswer = random.nextInt(4);
        int incorrectAnswer, pAnswer = 5;

        for(int i=0;i<4;i++){
            if(i == correctAnswer)
                answers[i] = names[chosenPokemon];
            else {
                incorrectAnswer = random.nextInt(24);
                while(incorrectAnswer == correctAnswer || pAnswer == incorrectAnswer)
                    incorrectAnswer = random.nextInt(24);
                answers[i] = names[incorrectAnswer];
                pAnswer = incorrectAnswer;
            }
        }
        button1.setText(answers[0]);
        button2.setText(answers[1]);
        button3.setText(answers[2]);
        button4.setText(answers[3]);
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try{
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                Bitmap mybitmap = BitmapFactory.decodeStream(in);
                return mybitmap;
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String,Void,String>
    {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try{
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();

                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();

                for (String line; (line = r.readLine()) != null; ) {
                    Log.i("Read","reading...");
                    total.append(line);
                }

                result = total.toString();
                Log.i("Read","complete!!");
                return result;
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pokemonImage = findViewById(R.id.pokemonImage);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        DownloadTask task = new DownloadTask();
        String result = "";

        try {
            Log.i("Connection:", "connecting......");
            result = task.execute("https://m.ranker.com/list/complete-list-of-all-pokemon-characters/video-game-info").get();

            String[] splitString = result.split("Define Base Objects") ;
            Pattern p = Pattern.compile("data-src=\"(.*?)\" data-srcset=");
            Matcher m = p.matcher(splitString[0]);

            int x=0;
            while(m.find()){
                links[x] = m.group(1);
                links[x] = links[x].replaceAll("\\s", "");
                x++;
            }

            Pattern p1 = Pattern.compile("meta itemprop=\"name\" content=\"(.*?)\"/> <a rel=\"noopener nofollow");
            Matcher m1 = p1.matcher(splitString[0]);

            x=0;
            while(m1.find()){
                names[x] = m1.group(1);
                names[x] = names[x].replaceAll("\\s", "");
                x++;
            }
            names[0]="Bulbasaur";

            createQuestion();
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
        }

}
