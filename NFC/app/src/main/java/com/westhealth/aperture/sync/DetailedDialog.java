package com.westhealth.aperture.sync;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;


/**
 * Created by Kirill on 10/5/2014.
 */
public class DetailedDialog extends DialogFragment {

    public TextView mTextview;
    public Button mConfirm;
    public String mType;

    ArrayList<Double> mData, mTime;
    int mIndex;

    public void setmData(ArrayList<Double> mData) {
        this.mData = mData;
    }

    public void setmTime(ArrayList<Double> mTime) {
        this.mTime = mTime;
    }

    public void setmIndex(int mIndex) {
        this.mIndex = mIndex;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.detail_popup, container);

        mTextview = (TextView)view.findViewById(R.id.data_text);

        if(mTextview != null) mTextview.setText(parseMetric(mType, mData, mTime, mIndex));

        mConfirm = (Button)view.findViewById(R.id.detail_diag_confirm);
        mConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private String parseMetric(String type, ArrayList<Double> data, ArrayList<Double> time, int index) {

        DecimalFormat df = new DecimalFormat("#.00");
        Double ddata = data.get(index);

        if(type.contains("Temp")) {
            return df.format(ddata) + "°F - ("
                    + df.format(((Double)((ddata - 32) / 1.8)))
                    + "°C)\n" + gaugeTMP(ddata)
                    + setDataTime(time.get(index));
        } else if(type.contains("HR")) {
            return df.format(ddata)
                    + " BPM\n" + gaugeBPM(ddata)
                    + setDataTime(time.get(index));
        } else if(type.contains("BP")) {
            return df.format(ddata) + " Milliseconds\n"
                    + gaugePTT(ddata)
                    + setDataTime(time.get(index));
        } else if(type.contains("Hydro")) {
            return df.format(ddata) + "PF\n"
                    + gaugeHYD(ddata)
                    + setDataTime(time.get(index));
        } else if(type.contains("Batt")) {
            return ddata.toString() + "%"
                    + setDataTime(time.get(index));
        } else {
            return "ERROR";
        }
    }

    private  String gaugeTMP(Double data) {
        return  (data < 90     ? "\nLOW"           :
                (data < 94.5   ? "\nNORMAL / LOW"  :
                (data < 99     ? "\nNORMAL"        :
                (data < 103.5  ? "\nFEVER"         :    "\nHIGH FEVER"
                ))));
    }

    private String gaugePTT(Double data){
        return  (data < 60   ? "\nVERY HIGH PRESSURE" :
                (data < 80   ? "\nHIGH PRESSURE"      :
                (data < 100  ? "\nNORMAL PRESSURE"    :
                (data < 120  ? "\nLOW PRESSURE"       :    "\nVERY LOW PRESSURE"
                ))));
    }

    private String gaugeBPM(Double data) {
        return  (data < 20  ? "\nVERY LOW" :
                (data < 40  ? "\nLOW"      :
                (data < 60  ? "\nNORMAL"   :
                (data < 80  ? "\nHIGH"     :    "\nVERY HIGH"
                ))));
    }

    private String gaugeHYD(Double data) {
        return (data < 20  ? "\nVERY DEHYDRATED" :
               (data < 40  ? "\nDEHYDRATED"      :
               (data < 60  ? "\nHYDRATED"        :
               (data < 80  ? "\nWELL HYDRATED"   :    "\nOVER HYDRATED"
               ))));
    }

    private String setDataTime(Double time) {
        Calendar dataDate = Calendar.getInstance();
        dataDate.setTimeInMillis(time.longValue());
        int mYear = dataDate.get(Calendar.YEAR);
        int mMonth = dataDate.get(Calendar.MONTH);
        int mDay = dataDate.get(Calendar.DAY_OF_MONTH);
        int mHour = dataDate.get(Calendar.HOUR);
        int mMin = dataDate.get(Calendar.MINUTE);
        int mAmPm = dataDate.get(Calendar.AM_PM);


        StringBuilder s = new StringBuilder();

        s.append("\n\n");
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

