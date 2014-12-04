package com.westhealth.aperture.sync;

import com.westhealth.aperture.nfcutils.NfcDataParseTask;
import com.westhealth.aperture.util.SystemUiHider;

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

import java.text.DecimalFormat;
import java.util.ArrayList;

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
                   mTranButton,
                   mHydroButton;

    private ImageView mHrView,
                      mTempView,
            mTranView,
                      mHydroView;

    private Double currentHeartRate,
                   currentTransitTime,
                   currentHydration,
                   currentTemperature;

    private View decorView;

    public static Activity overview;

    NfcDataParseTask ndpt;

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

        endStat();

        setContentView(R.layout.activity_overview);

        decorView = getWindow().getDecorView();

        ndpt = new NfcDataParseTask(getApplicationContext(), fileList());

        currentHeartRate = ndpt.parseCurrentHeartRate();
        currentTransitTime = ndpt.parseCurrentTransit();
        currentHydration = ndpt.parseCurrentHydration();
        currentTemperature = ndpt.parseCurrentTemperature();

        allocButtons();

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
        ArrayList<Double> battStatus = ndpt.parseBattery();

        mMainButton = (Button)findViewById(R.id.main_button);
        mMainButton.setText("DEVICE BATTERY: " + battStatus.get(battStatus.size()-1).toString() + "%");

        mMainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manageIntents(4);
            }
        });

        //Temp Button -----------------------------------------------------------------------------
        mTempButton = (Button) findViewById(R.id.temp_button);
        mTempView = (ImageView) findViewById(R.id.imageView_temp);
        mTempButton.setTextColor(Color.parseColor("#004e58"));
        mTempButton.setText(appendData(currentTemperature,"Temp"));
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
        mHrButton.setText(appendData(currentHeartRate, "HR"));
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
        mTranButton = (Button) findViewById(R.id.tran_button);
        mTranView = (ImageView) findViewById(R.id.imageView_bp);
        mTranButton.setTextColor(Color.parseColor("#004e58"));
        mTranButton.setText(appendData(currentTransitTime,"BP"));
        mTranView.setImageResource(R.drawable.bp_light);

        mTranButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manageIntents(2);
                mTranButton.setTextColor(Color.parseColor("#eef1f5"));
                mTranView.setImageResource(R.drawable.bp_white);
            }
        });
        mTranView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manageIntents(2);
                mTranButton.setTextColor(Color.parseColor("#eef1f5"));
                mTranView.setImageResource(R.drawable.bp_white);
            }
        });
        //------------------------------------------------------------------------------------------
        // Hydration Button ------------------------------------------------------------------------
        mHydroButton = (Button) findViewById(R.id.hydro_button);
        mHydroView = (ImageView) findViewById(R.id.imageView_hydro);
        mHydroButton.setTextColor(Color.parseColor("#004e58"));
        mHydroButton.setText(appendData(currentHydration,"Hydro"));
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
                Intent intent = new Intent(this, StatActivity.class);
        switch(stat) {
            case 0:
                intent.putExtra("Type","Temp");
                break;
            case 1:
                intent.putExtra("Type","HR");
                break;
            case 2:
                intent.putExtra("Type","BP");
                break;
            case 3:
                intent.putExtra("Type","Hydro");
                break;
            case 4:
                intent.putExtra("Type","Batt");
                break;
        }
        startActivity(intent);
    }

    private void endStat() {
        try {
            StatActivity.statActivity.finish();
        }
        catch (Exception e) {
            Log.d("AM_SYNC", "Attempted to close non-existent activity:: " + e.getMessage());
        }
    }

    private String appendData(Double data, String type) {

        DecimalFormat df = new DecimalFormat("#.00");

        if(type.contains("Temp")) {
            return df.format(data) + "°F";
        } else if(type.contains("HR")) {
            return df.format(data) + " BPM";
        } else if(type.contains("BP")) {
            return df.format(data) + " MS";
        } else if(type.contains("Hydro")) {
            return df.format(data) + " PF";
        } else {
            return "ERROR";
        }
    }
}
