package com.westhealth.aperture.nfc;

import com.westhealth.aperture.nfc.util.SystemUiHider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class OverviewActivity extends Activity {

    /**
     * A handle to NFC data
     */
    private NfcAdapter mNfcAdapter;

    /**
     * The buttons
     */
    private Button mMainButton,
                   mHrButton,
                   mTempButton,
                   mBpButton,
                   mHydroButton;

    private ImageView mHrView,
                      mTempView,
                      mBpView,
                      mHydroView;

    private View decorView;

    public static Activity overview;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overview = this;

        mNfcAdapter= NfcAdapter.getDefaultAdapter(this);

        mNfcAdapter.enableReaderMode (this, new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(Tag tag) {

            }
        }, NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, new Bundle());

        endOthers();

        setContentView(R.layout.activity_overview);

        decorView = getWindow().getDecorView();

        allocButtons();

        String temp = "";

        for(String s : fileList())
        {
            temp += s + "\n";
        }
        //mTextView.setText(temp);

        Button mClearButton = (Button) findViewById(R.id.clear);

        mClearButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    for (String s : fileList()) {
                        deleteFile(s);
                    }
                }
            }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SyncActivity.class));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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

    private void allocButtons() {
        mMainButton = (Button)findViewById(R.id.main_button);
        mMainButton.setText("RE-SYNC");

        mMainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manageIntents(4);
            }
        });

        //Temp Button -----------------------------------------------------------------------------
        mTempButton = (Button) findViewById(R.id.temp_button);
        mTempView = (ImageView) findViewById(R.id.imageView_temp);
        mTempButton.setTextColor(Color.parseColor("#004e58"));
        mTempView.setImageResource(R.drawable.temp_light);

        mTempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manageIntents(0);
                mTempButton.setTextColor(Color.parseColor("#eef1f5"));
                mTempView.setImageResource(R.drawable.temp_white);
            }
        });
        mTempView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manageIntents(0);
                mTempButton.setTextColor(Color.parseColor("#eef1f5"));
                mTempView.setImageResource(R.drawable.temp_white);
            }
        });

        //------------------------------------------------------------------------------------------
        // Heart Rate Button -----------------------------------------------------------------------
        mHrButton = (Button) findViewById(R.id.hr_button);
        mHrView = (ImageView) findViewById(R.id.imageView_hr);
        mHrButton.setTextColor(Color.parseColor("#004e58"));
        mHrView.setImageResource(R.drawable.hr_light);

        mHrButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manageIntents(1);
                mHrButton.setTextColor(Color.parseColor("#eef1f5"));
                mHrView.setImageResource(R.drawable.hr_white);
            }
        });
        mHrView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manageIntents(1);
                mHrButton.setTextColor(Color.parseColor("#eef1f5"));
                mHrView.setImageResource(R.drawable.hr_white);
            }
        });
        //------------------------------------------------------------------------------------------
        // Blood Pressure Button -------------------------------------------------------------------
        mBpButton = (Button) findViewById(R.id.bp_button);
        mBpView = (ImageView) findViewById(R.id.imageView_bp);
        mBpButton.setTextColor(Color.parseColor("#004e58"));
        mBpView.setImageResource(R.drawable.bp_light);

        mBpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manageIntents(2);
                mBpButton.setTextColor(Color.parseColor("#eef1f5"));
                mBpView.setImageResource(R.drawable.bp_white);
            }
        });
        mBpView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manageIntents(2);
                mBpButton.setTextColor(Color.parseColor("#eef1f5"));
                mBpView.setImageResource(R.drawable.bp_white);
            }
        });
        //------------------------------------------------------------------------------------------
        // Hydration Button ------------------------------------------------------------------------
        mHydroButton = (Button) findViewById(R.id.hydro_button);
        mHydroView = (ImageView) findViewById(R.id.imageView_hydro);
        mHydroButton.setTextColor(Color.parseColor("#004e58"));
        mHydroView.setImageResource(R.drawable.hydro_light);

        mHydroButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manageIntents(3);
                mHydroButton.setTextColor(Color.parseColor("#eef1f5"));
                mHydroView.setImageResource(R.drawable.hydro_white);
            }
        });
        mHydroView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manageIntents(3);
                mHydroButton.setTextColor(Color.parseColor("#eef1f5"));
                mHydroView.setImageResource(R.drawable.hydro_white);
            }
        });
        //------------------------------------------------------------------------------------------
    }

    private void manageIntents(int stat) {
        switch(stat) {
            case 0:
                startActivity(new Intent(this, TemperatureActivity.class));
                overridePendingTransition(R.xml.fade_in, R.xml.fade_out);
                break;
            case 1:
                startActivity(new Intent(this, HeartRateActivity.class));
                overridePendingTransition(R.xml.fade_in, R.xml.fade_out);
                break;
            case 2:
                startActivity(new Intent(this, BloodPressureActivity.class));
                overridePendingTransition(R.xml.fade_in, R.xml.fade_out);
                break;
            case 3:
                startActivity(new Intent(this, HydrationActivity.class));
                overridePendingTransition(R.xml.fade_in, R.xml.fade_out);
                break;
            case 4:
                startActivity(new Intent(this, SyncActivity.class));
                overridePendingTransition(R.xml.fade_in, R.xml.fade_out);
                break;
        }
    }

    private void endOthers() {
        try {
            TemperatureActivity.temperatureActivity.finish();
        }
        catch (Exception e) {
            Log.d("AM_SYNC", "Attempted to close non-existent activity:: " + e.getMessage());
        }
        try {
            HeartRateActivity.heartRateActivity.finish();
        }
        catch (Exception e) {
            Log.d("AM_SYNC", "Attempted to close non-existent activity:: " + e.getMessage());
        }
        try {
            BloodPressureActivity.bloodPressureActivity.finish();
        }
        catch (Exception e) {
            Log.d("AM_SYNC", "Attempted to close non-existent activity:: " + e.getMessage());
        }
        try {
            HydrationActivity.hydrationActivity.finish();
        }
        catch (Exception e) {
            Log.d("AM_SYNC", "Attempted to close non-existent activity:: " + e.getMessage());
        }
    }
}
