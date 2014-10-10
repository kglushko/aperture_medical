package com.westhealth.aperture.graph;

import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.westhealth.aperture.nfc.R;

/**
 * Created by Kirill on 10/9/2014.
 */
public class GraphFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_linegraph, container, false);
        final Resources resources = getResources();
        Line l = new Line();
        l.setUsingDips(true);
        LinePoint p = new LinePoint();
        p.setX(0);
        p.setY(5);
        p.setColor(resources.getColor(R.color.red));
        p.setSelectedColor(resources.getColor(R.color.transparent_blue));
        l.addPoint(p);
        p = new LinePoint();
        p.setX(8);
        p.setY(8);
        p.setColor(resources.getColor(R.color.blue));
        l.addPoint(p);
        p = new LinePoint();
        p.setX(10);
        p.setY(4);
        l.addPoint(p);
        p.setColor(resources.getColor(R.color.green));
        l.setColor(resources.getColor(R.color.orange));

        GraphView li = (GraphView) v.findViewById(R.id.linegraph);
        li.setUsingDips(true);
        li.addLine(l);
        li.setRangeY(0, 10);
        li.setLineToFill(0);

        li.setOnPointClickedListener(new GraphView.OnPointClickedListener() {

            @Override
            public void onClick(int lineIndex, int pointIndex) {
                Toast.makeText(getActivity(),
                        "Line " + lineIndex + " / Point " + pointIndex + " clicked",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        return v;
    }
}
