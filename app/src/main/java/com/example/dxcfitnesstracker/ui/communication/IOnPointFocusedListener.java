package com.example.dxcfitnesstracker.ui.communication;

/**
 * Point focus change listener for ValueLineChart
 */
public interface IOnPointFocusedListener {

    /**
     * Called when a new point in the ValueLineChart was selected
     *
     * @param _PointPos List position of the selected point.
     */
    void onPointFocused(int _PointPos);
}
