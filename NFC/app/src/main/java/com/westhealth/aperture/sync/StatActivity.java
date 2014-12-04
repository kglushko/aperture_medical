package com.westhealth.aperture.sync;


import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.westhealth.aperture.graph.GraphTask;
import com.westhealth.aperture.graph.GraphView;
import com.westhealth.aperture.nfcutils.NfcDataParseTask;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;


public class StatActivity extends FragmentActivity implements DateTimePicker.OnCompleteListener {

    public static StatActivity statActivity;

    private View decorView;

    private long start_time, end_time;

    private boolean startCalled = false;

    private DateTimePicker mDialogStart, mDialogEnd;

    private DetailedDialog mDetail;

    private TextView mTitleText, mTimeScopeView;

    private String mStatType;

    private GraphView graphView;

    private GraphTask graphTask;

    private NfcDataParseTask nfcDataParseTask;

    private long getEndOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime().getTime();
    }

    private long getStartOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime().getTime();
    }

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
        mDetail = new DetailedDialog();

        start_time = getStartOfToday();
        end_time = getEndOfToday();

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

        findViewById(R.id.startDateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogStart.setMintime(nfcDataParseTask.getMinDate());
                mDialogStart.setMaxtime(nfcDataParseTask.getMaxDate());
                mDialogStart.show(getSupportFragmentManager(), "AM_SYNC");
                startCalled = true;
            }
        });

        findViewById(R.id.endDateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogEnd.setMintime(nfcDataParseTask.getMinDate());
                mDialogEnd.setMaxtime(nfcDataParseTask.getMaxDate());
                mDialogEnd.show(getSupportFragmentManager(),"AM_SYNC");
                startCalled = false;
            }
        });

        mTimeScopeView = (TextView)findViewById(R.id.timeScopeView);
        mTimeScopeView.setText(setDataTime(start_time) + " - " + setDataTime(end_time));

        graphData(mStatType);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mDialogStart  = new DateTimePicker();
        mDialogEnd  = new DateTimePicker();
        mDetail = new DetailedDialog();
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
            if(start_time < end_time) {
                graphTask.setStarttime(start_time);
                graphData(mStatType);
                graphView.pauseThread(false);
                graphView.reDraw();
                mTimeScopeView.setText(setDataTime(start_time)
                        + " - "
                        + setDataTime(end_time));
            }
            else {
            }
        }
        else {
            end_time = time;
            if(start_time < end_time) {
                graphTask.setEndtime(end_time);
                graphData(mStatType);
                graphView.pauseThread(false);
                graphView.reDraw();
                mTimeScopeView.setText(setDataTime(start_time)
                        + " - "
                        + setDataTime(end_time));
            }
            else {
            }
        }
    }

    private void setTitle(String type) {
        if(type.contains("Temp")) {
            mTitleText.setText("TEMPURATURE DATA");
        } else if(type.contains("HR")) {
            mTitleText.setText("HEART RATE DATA");
        } else if(type.contains("BP")) {
            mTitleText.setText("BLOOD PRESSURE DATA");
        } else if(type.contains("Hydro")) {
            mTitleText.setText("HYDRATION DATA");
        } else if(type.contains("Batt")) {
            mTitleText.setText("BATTERY HISTORY");
        }
    }

    private void graphData(final String type) {

        ArrayList<Double> detailedData = new ArrayList<Double>();

        graphTask = new GraphTask(graphView, getResources());

        nfcDataParseTask = new NfcDataParseTask(getApplicationContext(), fileList());

        graphTask.setStarttime(start_time);

        graphTask.setEndtime(end_time);

        graphTask.setTimestamp(nfcDataParseTask.getParsedTimeStamp());

        if(type.contains("Temp")) {
            detailedData = nfcDataParseTask.parseTemperature();
        } else if(type.contains("HR")) {
            detailedData = nfcDataParseTask.parseHeartRate();
        } else if(type.contains("BP")) {
            detailedData = nfcDataParseTask.parseTransit();
        } else if(type.contains("Hydro")) {
            detailedData = nfcDataParseTask.parseHydration();
        } else if(type.contains("Batt")) {
            detailedData = nfcDataParseTask.parseBattery();
        }

        graphTask.setData(detailedData);

        graphTask.graphData(type.contains("Batt") ? "Batt" : "Data");

        final ArrayList<Double> dataToShow = detailedData;
        final ArrayList<Double> timeToShow = nfcDataParseTask.getParsedTimeStamp();

        graphView.setOnPointClickedListener(new GraphView.OnPointClickedListener() {
            @Override
            public void onClick(int lineIndex, int pointIndex) {
                mDetail.setmData(dataToShow);
                mDetail.setmTime(timeToShow);
                mDetail.setmType(type);
                mDetail.setmIndex(pointIndex);
                mDetail.show(getSupportFragmentManager(), "AM_SYNC");
            }
        });
    }

    private String setDataTime(long time) {
        Calendar dataDate = Calendar.getInstance();
        dataDate.setTimeInMillis(time);
        int mYear = dataDate.get(Calendar.YEAR);
        int mMonth = dataDate.get(Calendar.MONTH);
        int mDay = dataDate.get(Calendar.DAY_OF_MONTH);
        int mHour = dataDate.get(Calendar.HOUR);
        int mMin = dataDate.get(Calendar.MINUTE);
        int mAmPm = dataDate.get(Calendar.AM_PM);


        StringBuilder s = new StringBuilder();

        if (mHour == 0)
            mHour = 12;
        s.append(mHour);
        s.append(":");
        if(mMin < 10)
            s.append("0");
        s.append(mMin);
        if(mAmPm == 0)
            s.append(" AM");
        else
            s.append(" PM");
        s.append("  ");
        s.append(mMonth + 1);
        s.append("/");
        s.append(mDay);
        s.append("/");
        s.append(mYear);

        return s.toString();
    }
}



