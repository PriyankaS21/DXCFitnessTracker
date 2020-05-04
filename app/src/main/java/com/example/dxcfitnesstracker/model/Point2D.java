package com.example.dxcfitnesstracker.model;

/**
 * A simple wrapper class for the representation of coordinates.
 */
public class Point2D {

    public Point2D(float _x, float _y) {
        mX = _x;
        mY = _y;
    }

    public float getX() {
        return mX;
    }

    public void setX(float _x) {
        mX = _x;
    }

    public float getY() {
        return mY;
    }

    public void setY(float _y) {
        mY = _y;
    }

    private float mX;
    private float mY;
}
