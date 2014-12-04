package com.westhealth.aperture.sync;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.westhealth.aperture.nfcutils.NfcDataParseTask;

import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * Created by Kirill on 10/5/2014.
 */
public class DateTimePicker extends DialogFragment {

    public Button mConfirm, mCancel;

    public long mintime, maxtime;

    public void setMintime(long mintime) {
        this.mintime = mintime;
    }

    public void setMaxtime(long maxtime) {
        this.maxtime = maxtime;
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
        final View view = inflater.inflate(R.layout.date_time_dialog, container);

        mConfirm = (Button)view.findViewById(R.id.datetime_confirm);

        DatePicker dp = (DatePicker)view.findViewById(R.id.datePicker);
        dp.setMinDate(mintime);
        dp.setMaxDate(maxtime);

        mConfirm.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
                DatePicker dp = (DatePicker)view.findViewById(R.id.datePicker);
                TimePicker tp = (TimePicker)view.findViewById(R.id.timePicker);



                Calendar cal = new GregorianCalendar(dp.getYear(),dp.getMonth(),dp.getDayOfMonth(),
                                                     tp.getCurrentHour(),tp.getCurrentMinute());

                onTimeSet(cal.getTimeInMillis());
                getDialog().dismiss();
            }
        });

        mCancel = (Button)view.findViewById(R.id.datetime_cancel);
        mCancel.setOnClickListener(new View.OnClickListener() {

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

    private OnCompleteListener mListener;

    public static interface OnCompleteListener {
        public abstract void onComplete(long time);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }

    public void onTimeSet(long time) {
        this.mListener.onComplete(time);
    }
}

