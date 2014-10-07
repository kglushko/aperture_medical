package com.westhealth.aperture.nfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import android.view.View;
import android.widget.TextView;


public class HeartRateActivity extends FragmentActivity implements DateTimePicker.OnCompleteListener {

    /**
     * A handle to NFC data
     */
    private NfcAdapter mNfcAdapter;

    public static HeartRateActivity heartRateActivity;

    private View decorView;

    private long start_time, end_time;

    private boolean startCalled = false;

    DateTimePicker mDialogStart, mDialogEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        heartRateActivity = this;

        mDialogStart  = new DateTimePicker();
        mDialogEnd  = new DateTimePicker();

        mNfcAdapter=NfcAdapter.getDefaultAdapter(this);
        mNfcAdapter.enableReaderMode (this, new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(Tag tag) {

            }
        }, NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, new Bundle());

        try {
            OverviewActivity.overview.finish();
        }
        catch (Exception e) {
            Log.d("AM_SYNC", "Attempted to close non-existent activity:: " + e.getMessage());
        }

        setContentView(R.layout.activity_heart_rate);
        decorView = getWindow().getDecorView();

        findViewById(R.id.startDateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogStart.show(getSupportFragmentManager(),"AM_SYNC");
                startCalled = true;
            }
        });

        findViewById(R.id.endDateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogEnd.show(getSupportFragmentManager(),"AM_SYNC");
                startCalled = false;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mDialogStart  = new DateTimePicker();
        mDialogEnd  = new DateTimePicker();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, OverviewActivity.class));
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

    public void onComplete(long time) {
        if(startCalled) {
            start_time = time;
        }
        else {
            end_time = time;
        }
    }
}
