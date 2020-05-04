package com.example.dxcfitnesstracker.model;

import android.graphics.Rect;
import android.graphics.RectF;

public class BarModel extends BaseModel implements Comparable {

    public BarModel(String _legendLabel, float _value, int _color) {
        super(_legendLabel);
        mValue = _value;
        mColor = _color;
    }

    public BarModel(float _value) {
        super("" + _value);
        mValue = _value;
        mColor = 0xFFFF0000;
    }

    public float getValue() {
        return mValue;
    }

    public void setValue(float _value) {
        mValue = _value;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int _color) {
        mColor = _color;
    }

    public RectF getBarBounds() {
        return mBarBounds;
    }

    public void setBarBounds(RectF _bounds) {
        mBarBounds = _bounds;
    }


    @Override
    public int compareTo(Object o) {
        BarModel bar = (BarModel) o;
        if (this.mValue > bar.getValue()) {
            return 1;
        } else if (this.mValue == bar.getValue()) {
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * Value of the bar.
     */
    private float mValue;

    /**
     * Color in which the bar will be drawn.
     */
    private int mColor;

    /**
     * Bar boundaries.
     */
    private RectF mBarBounds;

    private boolean mShowValue = false;

    private Rect mValueBounds = new Rect();
}