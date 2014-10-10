package com.westhealth.aperture.graph;

import java.util.ArrayList;

public class Line {
    private ArrayList<LinePoint> mPoints = new ArrayList<LinePoint>();
    private int mColor;
    private boolean mShowPoints = true;
    // 6 has been the default prior to the addition of custom stroke widths
    private int mStrokeWidth = 6;
    // Since this is a new addition, it has to default to false to be backwards compatible
    private boolean mUseDips = false;


    public boolean isUsingDips() {
        return mUseDips;
    }

    public void setUsingDips(boolean treatSizesAsDips) {
        mUseDips = treatSizesAsDips;
    }

    public int getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        if (strokeWidth < 0) {
            throw new IllegalArgumentException("strokeWidth must not be less than zero");
        }
        mStrokeWidth = strokeWidth;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public ArrayList<LinePoint> getPoints() {
        return mPoints;
    }

    public void setPoints(ArrayList<LinePoint> points) {
        mPoints = points;
    }

    public void addPoint(LinePoint point) {
        LinePoint p;
        for (int i = 0; i < mPoints.size(); i++) {
            p = mPoints.get(i);
            if (point.getX() < p.getX()) {
                mPoints.add(i, point);
                return;
            }
        }
        mPoints.add(point);
    }

    public void removePoint(LinePoint point) {
        mPoints.remove(point);
    }

    public LinePoint getPoint(int index) {
        return mPoints.get(index);
    }

    public LinePoint getPoint(float x, float y) {
        LinePoint p;
        for (int i = 0; i < mPoints.size(); i++) {
            p = mPoints.get(i);
            if (p.getX() == x && p.getY() == y) {
                return p;
            }
        }
        return null;
    }

    public int getSize() {
        return mPoints.size();
    }

    public boolean isShowingPoints() {
        return mShowPoints;
    }

    public void setShowingPoints(boolean showPoints) {
        mShowPoints = showPoints;
    }

}
