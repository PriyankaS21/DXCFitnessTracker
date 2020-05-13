package com.example.dxcfitnesstracker.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import com.example.dxcfitnesstracker.BuildConfig;
import com.example.dxcfitnesstracker.util.Logger;
import com.example.dxcfitnesstracker.util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class Database extends SQLiteOpenHelper {

    private final static String DB_NAME = "steps";
    private final static int DB_VERSION = 3;
    private final static String DB_NAME2 = "personal_info";

    private static Database instance;
    private static final AtomicInteger openCounter = new AtomicInteger();

    private Database(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized Database getInstance(final Context c) {
        if (instance == null) {
            instance = new Database(c.getApplicationContext());
        }
        openCounter.incrementAndGet();
        return instance;
    }

    @Override
    public void close() {
        if (openCounter.decrementAndGet() == 0) {
            super.close();
        }
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DB_NAME + " (date INTEGER, steps INTEGER, calorie REAL)");
        db.execSQL("CREATE TABLE " + DB_NAME2 + " (weight INTEGER, height INTEGER, age INTEGER)");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            // drop PRIMARY KEY constraint
            db.execSQL("CREATE TABLE " + DB_NAME + "2 (date INTEGER, steps INTEGER, calorie REAL)");
            db.execSQL("INSERT INTO " + DB_NAME + "2 (date, steps, calorie) SELECT date, steps, calorie FROM " +
                    DB_NAME);
            db.execSQL("DROP TABLE " + DB_NAME);
            db.execSQL("ALTER TABLE " + DB_NAME + "2 RENAME TO " + DB_NAME + "");
        }
    }

    /**
     * Query the 'steps' table. Remember to close the cursor!
     *
     * @param columns       the colums
     * @param selection     the selection
     * @param selectionArgs the selction arguments
     * @param groupBy       the group by statement
     * @param having        the having statement
     * @param orderBy       the order by statement
     * @return the cursor
     */
    public Cursor query(final String[] columns, final String selection,
                        final String[] selectionArgs, final String groupBy, final String having,
                        final String orderBy, final String limit) {
        return getReadableDatabase()
                .query(DB_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * Inserts a new entry in the database, if there is no entry for the given
     * date yet. Steps should be the current number of steps and it's negative
     * value will be used as offset for the new date. Also adds 'steps' steps to
     * the previous day, if there is an entry for that date.
     * <p>
     * This method does nothing if there is already an entry for 'date' - use   *
     *
     * @param date  the date in ms since 1970
     * @param steps the current step value to be used as negative offset for the
     *              new day; must be >= 0
     */
    public void insertNewDay(long date, int steps, double calorie) {
        getWritableDatabase().beginTransaction();
        try {
            Cursor c = getReadableDatabase().query(DB_NAME, new String[]{"date"}, "date = ?",
                    new String[]{String.valueOf(date)}, null, null, null);
            if (c.getCount() == 0 && steps >= 0) {

                // add 'steps' to yesterdays count
                addToLastEntry(steps, calorie);

                // add today
                ContentValues values = new ContentValues();
                values.put("date", date);
                // use the negative steps as offset
                values.put("steps", -steps);
                values.put("calorie", calorie);
                getWritableDatabase().insert(DB_NAME, null, values);
            }
            c.close();
            if (BuildConfig.DEBUG) {
                Logger.log("insertDay " + date + " / " + steps + calorie);
                logState();
            }
            getWritableDatabase().setTransactionSuccessful();
        } finally {
            getWritableDatabase().endTransaction();
        }
    }

    /*
     * Insert personal information to table
     * */
    public void insertPersonalInfo(int weight, int height, int age) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("weight", weight);
        cv.put("height", height);
        cv.put("age", age);
        long result = db.insert(DB_NAME2, null, cv);

    }

    /*
     * extract weight from personal info table
     */
    public int getWeight() {
        Cursor c = null;
        String weightString = "";
        int weight = 5;
        int row_id = 1;
        try {
            c = getReadableDatabase().rawQuery("SELECT weight FROM " + DB_NAME2 + " WHERE rowid=?", new String[]{row_id + ""});
            if (c.getCount() > 0) {
                c.moveToFirst();
                weightString = c.getString(c.getColumnIndex("weight"));
                weight = Integer.parseInt(weightString);
            }
            return weight;
        } finally {
            c.close();
        }
    }

    /*
     * Get height from the table
     * */
    public int getHeight() {
        Cursor c = null;
        String heightString = "";
        int height = 152;
        int row_id = 1;
        try {
            c = getReadableDatabase().rawQuery("SELECT height FROM " + DB_NAME2 + " WHERE rowid=?", new String[]{row_id + ""});
            if (c.getCount() > 0) {
                c.moveToFirst();
                heightString = c.getString(c.getColumnIndex("height"));
                //c.getInt(0);
                height = Integer.parseInt(heightString);
            }
            return height;
        } finally {
            c.close();
        }
    }

    /**
     * Adds the given number of steps to the last entry in the database
     *
     * @param steps the number of steps to add
     */
    public void addToLastEntry(int steps, double calorie) {
        getWritableDatabase().execSQL("UPDATE " + DB_NAME + " SET steps = steps + " + steps + ", calorie = calorie + " + calorie +
                " WHERE date = (SELECT MAX(date) FROM " + DB_NAME + ")");
    }

    /**
     * Inserts a new entry in the database, overwriting any existing entry for the given date.
     *
     * @param date  the date in ms since 1970
     * @param steps the step value for 'date'; must be >= 0
     * @return true if a new entry was created, false if there was already an
     * entry for 'date' (and it was overwritten)
     */

    /**
     * Writes the current steps database to the log
     */
    public void logState() {
        if (BuildConfig.DEBUG) {
            Cursor c = getReadableDatabase()
                    .query(DB_NAME, null, null, null, null, null, "date DESC", "5");
            Logger.log(c);
            c.close();
        }
    }

    /**
     * Get the total of steps taken without today's value
     *
     * @return number of steps taken, ignoring today
     */
    public int getTotalWithoutToday() {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"SUM(steps)"}, "steps > 0 AND date > 0 AND date < ?",
                        new String[]{String.valueOf(Util.getToday())}, null, null, null);
        c.moveToFirst();
        int re = c.getInt(0);
        c.close();
        return re;
    }

    /**
     * Get the maximum of steps walked in one day and the date that happend
     *
     * @return a pair containing the date (Date) in millis since 1970 and the
     * step value (Integer)
     */
    public Pair<Date, Integer> getRecordData() {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"date, steps"}, "date > 0", null, null, null,
                        "steps DESC", "1");
        c.moveToFirst();
        Pair<Date, Integer> p = new Pair<Date, Integer>(new Date(c.getLong(0)), c.getInt(1));
        c.close();
        return p;
    }

    /**
     * Get the number of steps taken for a specific date.
     * <p>
     * If date is Util.getToday(), this method returns the offset which needs to
     * be added to the value returned by getCurrentSteps() to get today's steps.
     */
    public int getSteps(final long date) {
        Cursor c = getReadableDatabase().query(DB_NAME, new String[]{"steps"}, "date = ?",
                new String[]{String.valueOf(date)}, null, null, null);
        c.moveToFirst();
        int re;
        if (c.getCount() == 0) re = Integer.MIN_VALUE;
        else re = c.getInt(0);
        c.close();
        return re;
    }

    /*
     * Get the amount of calorie burnt for a specific date.
     * If date is Util.getToday(), this method returns the offset which needs to
     * be added to the value returned by getCurrentCalorie() to get today's calorie burnt.
     * */

    public double getCalorie(final long date) {
        Cursor c = getReadableDatabase().query(DB_NAME, new String[]{"calorie"}, "date = ?",
                new String[]{String.valueOf(date)}, null, null, null);
        c.moveToFirst();
        double ca;
        if (c.getCount() == 0) ca = Integer.MIN_VALUE;
        else ca = c.getInt(2);
        c.close();
        return ca;
    }

    /**
     * Gets the last num entries in descending order of date (newest first)
     *
     * @param num the number of entries to get
     * @return a list of long,integer pair - the first being the date, the second the number of steps
     */
    public List<Pair<Long, Integer>> getLastEntries(int num) {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"date", "steps"}, "date > 0", null, null, null,
                        "date DESC", String.valueOf(num));
        int max = c.getCount();
        List<Pair<Long, Integer>> result = new ArrayList<>(max);
        if (c.moveToFirst()) {
            do {
                result.add(new Pair<>(c.getLong(0), c.getInt(1)));
            } while (c.moveToNext());
        }
        return result;
    }

    /**
     * Get the number of steps taken between 'start' and 'end' date
     * <p/>
     * Note that todays entry might have a negative value, so take care of that
     * if 'end' >= Util.getToday()!
     *
     * @param start start date in ms since 1970 (steps for this date included)
     * @param end   end date in ms since 1970 (steps for this date included)
     * @return the number of steps from 'start' to 'end'. Can be < 0 as todays
     * entry might have negative value
     */
    public int getSteps(final long start, final long end) {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"SUM(steps)"}, "date >= ? AND date <= ?",
                        new String[]{String.valueOf(start), String.valueOf(end)}, null, null, null);
        int re;
        if (c.getCount() == 0) {
            re = 0;
        } else {
            c.moveToFirst();
            re = c.getInt(0);
        }
        c.close();
        return re;
    }

    /*
     * Get the amount of calorie burnt between 'start and 'end' date
     *
     * */

    public double getCalorie(final long start, final long end) {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"SUM(calorie)"}, "date >= ? AND date <= ?",
                        new String[]{String.valueOf(start), String.valueOf(end)}, null, null, null);
        double ca;
        if (c.getCount() == 0) {
            ca = 0;
        } else {
            c.moveToFirst();
            ca = c.getDouble(0);
        }
        c.close();
        return ca;
    }

    /**
     * Removes all entries with negative values.
     * <p/>
     * Only call this directly after boot, otherwise it might remove the current
     * day as the current offset is likely to be negative
     */
    void removeNegativeEntries() {
        getWritableDatabase().delete(DB_NAME, "steps < ?", new String[]{"0"});
        getWritableDatabase().delete(DB_NAME, "calorie < ?", new String[]{"0"});
    }

    /**
     * Get the number of 'valid' days (= days with a step value > 0).
     * <p>
     * The current day is not added to this number.
     *
     * @return the number of days with a step value > 0, return will be >= 0
     */
    public int getDaysWithoutToday() {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"COUNT(*)"}, "steps > ? AND date < ? AND date > 0",
                        new String[]{String.valueOf(0), String.valueOf(Util.getToday())}, null,
                        null, null);
        c.moveToFirst();
        int re = c.getInt(0);
        c.close();
        return re < 0 ? 0 : re;
    }

    /**
     * Get the number of 'valid' days (= days with a step value > 0).
     * <p/>
     * The current day is also added to this number, even if the value in the
     * database might still be < 0.
     * <p/>
     * It is safe to divide by the return value as this will be at least 1 (and
     * not 0).
     *
     * @return the number of days with a step value > 0, return will be >= 1
     */
    public int getDays() {
        // todays is not counted yet
        int re = this.getDaysWithoutToday() + 1;
        return re;
    }

    /**
     * Saves the current 'steps since boot' sensor value in the database.
     *
     * @param steps since boot
     */
    public void saveCurrentSteps(int steps) {
        ContentValues values = new ContentValues();
        values.put("steps", steps);
        if (getWritableDatabase().update(DB_NAME, values, "date = -1", null) == 0) {
            values.put("date", -1);
            getWritableDatabase().insert(DB_NAME, null, values);
        }
        if (BuildConfig.DEBUG) {
            Logger.log("saving steps in db: " + steps);
        }
    }

    /**
     * Reads the latest saved value for the 'steps since boot' sensor value.
     *
     * @return the current number of steps saved in the database or 0 if there
     * is no entry
     */
    public int getCurrentSteps() {
        int re = getSteps(-1);
        return re == Integer.MIN_VALUE ? 0 : re;
    }

    /*
     * Saves the current 'calorie burnt since boot' sensor value in the database.
     * */

    public void saveCurrentCalorie(double calorie) {
        ContentValues values = new ContentValues();
        values.put("calorie", calorie);
        if (getWritableDatabase().update(DB_NAME, values, "date = -1", null) == 0) {
            values.put("date", -1);
            getWritableDatabase().insert(DB_NAME, null, values);
        }
        if (BuildConfig.DEBUG) {
            Logger.log("saving calories in db: " + calorie);
        }
    }

    /*
     * Reads the latest saved value for the 'calorie burnt since boot' sensor value.
     *
     * @return the current number of calorie burnt saved in the database or 0 if there
     * is no entry
     */

    public double getCurrentCalorie() {
        double ca = getCalorie(-1);
        return ca == Double.MIN_VALUE ? 0 : ca;
    }

    /*
     * To check whether data exist or not in table
     */
    public boolean dataExists() {
        String COLUMN_NAME = "weight";
        String[] columns = {COLUMN_NAME};
        String limit = "1";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor mCursor = db.query(DB_NAME2, columns, null, null, null, null, null, limit);

        if (mCursor != null) {
            return true;
            /* record exist */
        } else {
            return false;
            /* record not exist */
        }
    }

    /*
     * Clear data from table if exist
     */
    public void clearData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + DB_NAME2);
        db.close();

    }
}
