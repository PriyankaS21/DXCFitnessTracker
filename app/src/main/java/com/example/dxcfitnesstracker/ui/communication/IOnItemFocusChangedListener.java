package com.example.dxcfitnesstracker.ui.communication;

public interface IOnItemFocusChangedListener {

    /**
     * Called when a new item in PieChart is selected
     *
     * @param _Position List position of the item.
     */
    void onItemFocusChanged(int _Position);
}
