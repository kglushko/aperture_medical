package com.westhealth.aperture.nfc;

import android.content.Intent;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.westhealth.aperture.graph.GraphView;
import com.westhealth.aperture.graph.Line;
import com.westhealth.aperture.graph.LinePoint;
import com.westhealth.aperture.sync.R;

import java.util.Random;


public class StatActivity extends FragmentActivity implements DateTimePicker.OnCompleteListener {

    public static StatActivity statActivity;

    private View decorView;

    private long start_time, end_time;

    private boolean startCalled = false;

    private DateTimePicker mDialogStart, mDialogEnd;

    private GraphView graphView;

    private TextView mTitleText;

    private String mStatType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        statActivity = this;
        setContentView(R.layout.activity_stat);

        decorView = getWindow().getDecorView();

        Bundle mType = getIntent().getExtras();
        if(mType != null){
            mStatType = mType.getString("Type");
        }

        mTitleText = (TextView)findViewById(R.id.title_text);
        graphView  = (GraphView)findViewById(R.id.graph_view);

        graphView.pauseThread(false);

        setTitle(mStatType);

        mDialogStart  = new DateTimePicker();
        mDialogEnd  = new DateTimePicker();

        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
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

        //GraphView mHeartGraph = (GraphView)findViewById(R.id.fullscreen_content);

        findViewById(R.id.startDateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogStart.show(getSupportFragmentManager(), "AM_SYNC");
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

        //viewTest();

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
            graphView.pauseThread(true);
    }

    @Override
    public void onPause() {
        super.onPause();
            graphView.pauseThread(true);
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

    public void setTitle(String type) {
        if(type.contains("Temp")) {
            mTitleText.setText("TEMPURATURE DATA");
        } else if(type.contains("HR")) {
            mTitleText.setText("HEART RATE DATA");
        } else if(type.contains("BP")) {
            mTitleText.setText("BLOOD PRESSURE DATA");
        } else if(type.contains("Hydro")) {
            mTitleText.setText("HYDRATION DATA");
        }
    }


/*    public void viewTest() {
        final Resources resources = getResources();
        Line l = new Line();
        l.setUsingDips(false);
        LinePoint p;

        Random rand = new Random();

        for(int i = 0; i < 50; i++) {
            p = new LinePoint();
            p.setX(i);
            p.setY(5);
            p.setColor(resources.getColor(R.color.baseCeleste));
            l.addPoint(p);
        }

        l.setColor(resources.getColor(R.color.textColor));

        graphView.setUsingDips(false);
        graphView.addLine(l);
        graphView.setRangeY(0, 10);
        graphView.setLineToFill(0);

        graphView.setOnPointClickedListener(new GraphView.OnPointClickedListener() {

            @Override
            public void onClick(int lineIndex, int pointIndex) {
                Toast.makeText(getApplicationContext(),
                        "Line " + lineIndex + " / Point " + pointIndex + " clicked",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

    }*/
}
