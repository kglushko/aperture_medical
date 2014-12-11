package com.westhealth.aperture.nfc;

import com.westhealth.aperture.nfc.util.SystemUiHider;
import com.westhealth.aperture.sync.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class SyncActivity extends Activity {

    NdefReaderTask NRT = new NdefReaderTask();

    /**
     * A handle to NFC data
     */
    private NfcAdapter mNfcAdapter;

    /**
     * Main text in center window
     */
    private TextSwitcher mTextView;

    /**
     * Main button on home screen
     */
    private Button mMainButton;

    private Timer mTimer;

    private String[] waitText = {"READY\nTO\nSYNC",
                                 "PLEASE\nHOLD PHONE\nTO DEVICE",
                                 "NFC\nIS\nDISABLED"};

    boolean isOne = false;

    boolean isNFCon = false;

    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            OverviewActivity.overview.finish();
            mNfcAdapter.disableReaderMode(this);
        }
        catch (Exception e) {
            Log.d("AM_SYNC","Attempted to close non-existent activity:: " + e.getMessage());
        }

        decorView = getWindow().getDecorView();

        setContentView(R.layout.activity_sync);

        mMainButton = (Button) findViewById(R.id.main_button);

        // Initialize the textSwitcher
        initTextSwitch();

        flipText();

        // Check and Bind NFC radio
        initNFC();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        // Check and Bind NFC radio
        initNFC();
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    /**
     * Initializes the TextSwitcher and Animations
     */
    private void initTextSwitch() {
        mTextView = (TextSwitcher) findViewById(R.id.fullscreen_content);

        mTextView.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                TextView myText = new TextView(SyncActivity.this);
                myText.setHeight(4000);
                myText.setWidth(2000);
                myText.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
                myText.setTextSize(50);
                myText.setTextColor(Color.argb(255, 0, 78, 88));
                return myText;
            }
        });

        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        // set the animation type of textSwitcher
        mTextView.setInAnimation(in);
        mTextView.setOutAnimation(out);
    }

    /**
     * For that awesome dynamic look
     */
    private void flipText() {
        mTimer = new Timer();
        mNfcAdapter=NfcAdapter.getDefaultAdapter(this);

        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.setText((isNFCon ? (isOne ? waitText[1] : waitText[0]) : waitText[2]));
                        isOne = !isOne;
                    }
                });
            }
        },0, 3000);
    }

    /**
     * This checks if NFC is enabled and binds the App to the radio
     */
    private void initNFC() {
        mNfcAdapter=NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter==null) {
            isNFCon = false;
            // Stop here, we definitely need NFC
            mTextView.setText("NFC IS NOT SUPPORTED BY YOUR DEVICE");
            mMainButton.setText("QUIT");
            mMainButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });
        } else {
            if (!mNfcAdapter.isEnabled()) {
                isNFCon = false;
                // Tell the user we're in bad shape
                mMainButton.setVisibility(View.VISIBLE);
                mMainButton.setText("ENABLE NFC?");
                mMainButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                    }
                });
            } else {
                isNFCon = true;
                mMainButton.setText("CONTINUE W/O SYNC");
                mMainButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new Intent(SyncActivity.this, OverviewActivity.class));
                    }
                });
                discoverTag();
            }
        }
    }

    /**
     *  Actually handle tag discovery here
     */
    public void discoverTag() {
        Intent intent = getIntent();
        NdefMessage msgs[] = new NdefMessage[0];

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            mTimer.cancel();
            mTimer.purge();
            mTextView.setText("SYNCING...");
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                mTextView.setText("COMPLETE");
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            /* This will need modifications */
            if(NRT.saveData(msgs,getApplicationContext(),true) ==  1) {
                startActivity(new Intent(this, OverviewActivity.class));
                //overridePendingTransition(R.xml.fade_in, R.xml.fade_out);
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }
}




