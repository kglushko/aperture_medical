package com.westhealth.aperture.graph;

import android.app.Activity;
import android.app.Application;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

import com.westhealth.aperture.sync.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Kirill on 11/10/2014.
 */
public class GraphTask extends Application {

    private GraphView graphView;
    private Resources res;

    private ArrayList<Double> timestamp;
    private ArrayList<Double> data;

    private long starttime;
    private long endtime;

    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    public void setEndtime(long endtime) {
        this.endtime = endtime;
    }

    public GraphTask(GraphView gv, Resources resources) {
        res = resources;
        graphView = gv;
    }

    public void setData(ArrayList<Double> data) {
        this.data = data;
    }

    public void setTimestamp(ArrayList<Double> timestamp) {
        this.timestamp = timestamp;
    }

    public void graphData(String type) {

        graphView.removeAllLines();

        Line l = new Line();
        l.setUsingDips(false);
        LinePoint p;

        // Remember to default start and end times at the beginning of today and
        // the end of today.
        boolean begin = false, end = false;
        float xmin = 0, xmax = 0;

        for(int i = 0; i < timestamp.size(); i++) {
            if((timestamp.get(i).longValue() <= (endtime)&& timestamp.get(i).longValue() >= (starttime))) {
                if(begin == false) {
                    xmin = i;
                    begin = true;
                }
                if(end == false) {
                    xmax = i;
                    begin = true;
                }
                p = new LinePoint();
                p.setX(i);

                p.setY(data.get(i).floatValue());

                p.setColor(res.getColor(R.color.baseCeleste));
                l.addPoint(p);
            }
        }

        graphView.setRangeX(xmin,xmax);

        l.setColor(res.getColor(R.color.textColor));

        graphView.setUsingDips(false);
        graphView.addLine(l);

        if (type.contains("Data")) {
            graphView.setRangeY(data.get(data.indexOf(Collections.min(data))).floatValue() - 10,
                    data.get(data.indexOf(Collections.max(data))).floatValue() + 10);
        } else if (type.contains("Batt")) {
            graphView.setRangeY(-10,110);
        }

        graphView.setLineToFill(0);

    }
}
