package com.example.brewclock;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class BrewClockActivity extends Activity implements OnClickListener {
    /** Properties **/
    protected Button brewAddTime;
    protected Button brewDecreaseTime;
    protected Button startBrew;
    protected TextView brewCountLabel;
    protected TextView brewTimeLabel;

    protected int brewTime;
    protected int leftBrewTimeInSec;
    protected CountDownTimer brewCountDownTimer;
    protected int brewCount;
    protected boolean isBrewing = false;
    protected MediaPlayer mp;
    private SharedPreferences state;
    private final String LEFT_BREW_TIME = "left_brew_time";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        state = getSharedPreferences(getString(R.string.state_file),
                Context.MODE_PRIVATE);

        brewCount = state.getInt(getString(R.string.brew_count),0);
        brewTime  = state.getInt(getString(R.string.brew_time),3);

        setContentView(R.layout.main);

        // Connect interface elements to properties
        brewAddTime = (Button) findViewById(R.id.brew_time_up);
        brewDecreaseTime = (Button) findViewById(R.id.brew_time_down);
        startBrew = (Button) findViewById(R.id.brew_start);
        brewCountLabel = (TextView) findViewById(R.id.brew_count_label);
        brewTimeLabel = (TextView) findViewById(R.id.brew_time);

        // Setup ClickListeners
        brewAddTime.setOnClickListener(this);
        brewDecreaseTime.setOnClickListener(this);
        startBrew.setOnClickListener(this);
        mp = MediaPlayer.create(this,R.raw.gong);

        // Set the initial brew values
        setBrewCount(brewCount);
        setBrewTime(brewTime);
    }

    /** Methods **/

    /**
     * Set an absolute value for the number of minutes to brew. Has no effect if a brew
     * is currently running.
     * @param minutes The number of minutes to brew.
     */
    public void setBrewTime(int minutes) {
        if(isBrewing)
            return;
        brewTime =  minutes < 1? 1: minutes;
        leftBrewTimeInSec = brewTime * 60;
        brewTimeLabel.setText(String.valueOf(brewTime) + "m");
    }

    /**
     * Set the number of brews that have been made, and update the interface.
     * @param count The new number of brews
     */
    public void setBrewCount(int count) {
        brewCount = count;
        brewCountLabel.setText(String.valueOf(brewCount));
    }

    /**
     * Start the brew timer
     */
    public void startBrew() {
        // Create a new CountDownTimer to track the brew time
        brewCountDownTimer = new CountDownTimer(leftBrewTimeInSec * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                leftBrewTimeInSec = (int) millisUntilFinished / 1000;
                brewTimeLabel.setText(Utility.secondsToPrettyTime(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                isBrewing = false;
                setBrewCount(brewCount + 1);

                brewTimeLabel.setText("Brew Up!");
                startBrew.setText("Start");
                mp.start();
            }
        };

        brewCountDownTimer.start();
        startBrew.setText("Stop");
        isBrewing = true;
    }

    /**
     * Stop the brew timer
     */
    public void stopBrew() {
        if(brewCountDownTimer != null)
            brewCountDownTimer.cancel();

        isBrewing = false;
        startBrew.setText("Start");
    }

    /** Interface Implementations **/
  /* (non-Javadoc)
   * @see android.view.View.OnClickListener#onClick(android.view.View)
   */
    public void onClick(View v) {
        if(v == brewAddTime)
            setBrewTime(brewTime + 1);
        else if(v == brewDecreaseTime)
            setBrewTime(brewTime -1);
        else if(v == startBrew) {
            if(isBrewing)
                stopBrew();
            else
                startBrew();
        }
    }

    @Override
    protected void onPause() {
        Log.i("IN_PAUSE", "writing down stuff for later");
        SharedPreferences.Editor editor = state.edit();
        editor.putInt(getString(R.string.brew_count),brewCount);
        editor.putInt(getString(R.string.brew_time),brewTime);
        editor.commit();
        super.onPause();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        leftBrewTimeInSec = savedInstanceState.getInt(LEFT_BREW_TIME);
        startBrew();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(LEFT_BREW_TIME,leftBrewTimeInSec);
        super.onSaveInstanceState(outState);
    }
}