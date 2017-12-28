package com.nightonke.saver.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;

import com.nightonke.saver.R;
import com.nightonke.saver.fragment.RecordCheckDialogFragment;
import com.nightonke.saver.model.CoCoinRecord;
import com.nightonke.saver.model.RecordManager;
import com.nightonke.saver.util.CoCoinUtil;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.SubcolumnValue;

/**
 * Created by FlavioForenza on 28/12/17.
 */

abstract class TodayViewRecyclerViewAdapterMethods1 extends RecyclerView.Adapter<TodayViewRecyclerViewAdapter.viewHolder> {
    static final int TYPE_HEADER = 0;
    static final int TYPE_BODY = 1;
    static final int TODAY = 0;
    static final int YESTERDAY = 1;
    static final int THIS_WEEK = 2;
    static final int LAST_WEEK = 3;
    static final int THIS_MONTH = 4;
    static final int LAST_MONTH = 5;
    static final int THIS_YEAR = 6;
    static final int LAST_YEAR = 7;
    protected Context mContext;
    protected int fragmentPosition;
    // the data of this fragment
    protected ArrayList<CoCoinRecord> allData;
    // store the sum of expenses of each tag
    protected Map<Integer, Double> TagExpanse;
    // store the records of each tag
    protected Map<Integer, List<CoCoinRecord>> Expanse;
    // the original target value of the whole pie
    protected float[] originalTargets;
    // whether the data of this fragment is empty
    protected boolean IS_EMPTY;
    // the sum of the whole pie
    protected double Sum;
    // the number of columns in the histogram
    protected int columnNumber;
    // the axis date value of the histogram(hour, day of week and month, month)
    protected int axis_date;
    // the month number
    protected int month;
    // the selected position of one part of the pie
    protected int pieSelectedPosition = 0;
    // the last selected position of one part of the pie
    protected int lastPieSelectedPosition = -1;
    // the last selected position of one part of the histogram
    protected int lastHistogramSelectedPosition = -1;
    // the date string on the footer and header
    protected String dateString;
    // the string shown in the dialog
    protected String dialogTitle;
    // the selected tag in pie
    protected int tagId = -1;
    private TodayViewRecyclerViewAdapter.OnItemClickListener onItemClickListener;
    // the selected column in histogram
    private int timeIndex;

    TodayViewRecyclerViewAdapterMethods1(Context context, int position) {
        mContext = context;
        IS_EMPTY = allData.isEmpty();
        fragmentPosition = position;
        allData = new ArrayList<>();
        Sum = 0;
    }

    public void function1(int value1, int value2, int value3, int axis2){
        if(fragmentPosition == value1 || fragmentPosition == value2){
            columnNumber = value3;
            axis_date = axis2;
        }
    }

    public void end1(int size, RecordManager recordManager){
        for (int j = 2; j < size; j++) {
            TagExpanse.put(recordManager.TAGS.get(j).getId(), Double.valueOf(0));
            Expanse.put(recordManager.TAGS.get(j).getId(), new ArrayList<CoCoinRecord>());
        }

        size = allData.size();
        for (int i = 0; i < size; i++) {
            CoCoinRecord coCoinRecord = allData.get(i);
            TagExpanse.put(coCoinRecord.getTag(),
                    TagExpanse.get(coCoinRecord.getTag()) + Double.valueOf(coCoinRecord.getMoney()));
            Expanse.get(coCoinRecord.getTag()).add(coCoinRecord);
            Sum += coCoinRecord.getMoney();
            int var1 = Calendar.DAY_OF_WEEK;
            function2(coCoinRecord, var1 );
            int var2 =  Calendar.DAY_OF_MONTH;
            function2(coCoinRecord, var2);
            function3(coCoinRecord, var1, var2);
        }
    }

    public void end2(RecordManager recordManager){
        if (!IS_EMPTY) {
            function1(TODAY, YESTERDAY, 24, Calendar.HOUR_OF_DAY);
            function1(THIS_WEEK, LAST_WEEK, 7, Calendar.DAY_OF_WEEK);

            columnNumber = allData.get(0).getCalendar().getActualMaximum(Calendar.DAY_OF_MONTH);
            function1(THIS_WEEK, LAST_WEEK, columnNumber, Calendar.DAY_OF_WEEK);

            function1(THIS_YEAR, LAST_YEAR, 12, Calendar.MONTH);


            TagExpanse = new TreeMap<>();
            Expanse = new HashMap<>();
            originalTargets = new float[columnNumber];
            for (int i = 0; i < columnNumber; i++) originalTargets[i] = 0;

            int size = recordManager.TAGS.size();
            end1(size, recordManager);
            TagExpanse = CoCoinUtil.SortTreeMapByValues(TagExpanse);
        }
    }

    public void function3(CoCoinRecord coco1, int vai1, int vai2){
        if(!((axis_date==vai1)&&(axis_date==vai2))){
            originalTargets[coco1.getCalendar().get(axis_date)] += coco1.getMoney();
        }
    }

    public void function2(CoCoinRecord coco, int calendar){
        if (axis_date == calendar) {
            if (CoCoinUtil.WEEK_START_WITH_SUNDAY)
                originalTargets[coco.getCalendar().get(axis_date) - 1]
                        += coco.getMoney();
            else originalTargets[(coco.getCalendar().get(axis_date) + 5) % 7]
                    += coco.getMoney();
        }
    }

    public void end3(String text){
        if ("zh".equals(CoCoinUtil.GetLanguage()))
            text = getSnackBarDateString() + text + "\n" +
                    "于" + CoCoinUtil.GetTagName(tagId);
        else
            text += getSnackBarDateString() + "\n"
                    + "in " + CoCoinUtil.GetTagName(tagId);
    }

    public void end4(String text){
        if ("zh".equals(CoCoinUtil.GetLanguage()))
            text = getSnackBarDateString() + "\n" + text;
        else
            text += "\n" + getSnackBarDateString();
    }

    public void function11(TodayViewRecyclerViewAdapter.viewHolder holder3){
        if (!(fragmentPosition == TODAY || fragmentPosition == YESTERDAY)) {

// set value touch listener of histogram////////////////////////////////////////////////////////////
            holder3.histogram.setOnValueTouchListener(
                    new ColumnChartOnValueSelectListener() {
                        @Override
                        public void onValueSelected(int columnIndex,
                                                    int subcolumnIndex, SubcolumnValue value) {
                            lastHistogramSelectedPosition = columnIndex;
                            timeIndex = columnIndex;
                            // snack bar
                            RecordManager recordManager
                                    = RecordManager.getInstance(mContext.getApplicationContext());

                            String text = CoCoinUtil.GetSpendString((int) value.getValue());
                            if (tagId != -1)
                                // belongs a tag
                                end3(text);
                            else
                                // don't belong to any tag
                                end4(text);

// setting the snack bar and dialog title of histogram//////////////////////////////////////////////
                            dialogTitle = text;
                            Snackbar snackbar =
                                    Snackbar
                                            .with(mContext)
                                            .type(SnackbarType.MULTI_LINE)
                                            .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                            .position(Snackbar.SnackbarPosition.BOTTOM)
                                            .margin(15, 15)
                                            .backgroundDrawable(CoCoinUtil.GetSnackBarBackground(
                                                    fragmentPosition - 2))
                                            .text(text)
                                            .textTypeface(CoCoinUtil.GetTypeface())
                                            .textColor(Color.WHITE)
                                            .actionLabelTypeface(CoCoinUtil.GetTypeface())
                                            .actionLabel(mContext.getResources()
                                                    .getString(R.string.check))
                                            .actionColor(Color.WHITE)
                                            .actionListener(new mActionClickListenerForHistogram());
                            SnackbarManager.show(snackbar);
                        }

                        @Override
                        public void onValueDeselected() {

                        }
                    });
        }
    }

    private void funtion12(ArrayList<CoCoinRecord> temp, int ind) {
            if (tagId != -1) {
                for (int i = 0; i < Expanse.get(tagId).size(); i++)
                    if (Expanse.get(tagId).get(i).getCalendar().get(axis_date) == ind)
                        temp.add(Expanse.get(tagId).get(i));
            } else {
                for (int i = 0; i < allData.size(); i++)
                    if (allData.get(i).getCalendar().get(axis_date) == ind)
                        temp.add(allData.get(i));
            }
        }

    public String zh(String str1, String str2, String str3){
            if ("zh".equals(CoCoinUtil.GetLanguage()))
                // 在今天9点
                return mContext.getResources().getString(Integer.parseInt(str1)) +
                        mContext.getResources().getString(Integer.parseInt(str2)) +
                        timeIndex +
                        mContext.getResources().getString(Integer.parseInt(str3));
            else
                // at 9 o'clock today
                return mContext.getResources().getString(Integer.parseInt(str1)) +
                        timeIndex + " " +
                        mContext.getResources().getString(Integer.parseInt(str3)) + " " +
                        mContext.getResources().getString(Integer.parseInt(str2));
        }

    public String fl(String str1){
            return mContext.getString(Integer.parseInt(str1))
                    + CoCoinUtil.GetWeekDay(timeIndex);
        }

    public String f2(String str3){
            return mContext.getResources().getString(R.string.on) +
                    CoCoinUtil.GetMonthShort(month) + CoCoinUtil.GetWhetherBlank() +
                    (timeIndex + 1) + CoCoinUtil.GetWhetherFuck();
        }

    public String d3(String str1, String str2){
            if ("zh".equals(CoCoinUtil.GetLanguage()))
                // 在今年1月
                return mContext.getResources().getString(Integer.parseInt(str1)) +
                        mContext.getResources().getString(Integer.parseInt(str2)) +
                        CoCoinUtil.GetMonthShort(timeIndex + 1);
            else
                // in Jan. 1
                return mContext.getResources().getString(Integer.parseInt(str1)) +
                        CoCoinUtil.GetMonthShort(timeIndex + 1) + " " +
                        mContext.getResources().getString(Integer.parseInt(str2));
        }

    // set the dateString shown in snack bar in this fragment///////////////////////////////////////////
        private String getSnackBarDateString() {
            switch (fragmentPosition) {
                case TODAY:
                    zh("at", "today_date_string", "o_clock");
                case YESTERDAY:
                    zh("at", "yesterday", "o'clock");
                case THIS_WEEK:
                    // 在周一
                    // on Monday
                    fl("on");
                case LAST_WEEK:
                    // 在上周一
                    // on last Monday
                    return mContext.getResources().getString(R.string.on)
                            + mContext.getResources().getString(R.string.last)
                            + CoCoinUtil.GetWeekDay(timeIndex);
                case THIS_MONTH:
                    // 在1月1日
                    // on Jan. 1
                    f2("on");
                case LAST_MONTH:
                    // 在1月1日
                    // on Jan. 1
                    f2("on");
                case THIS_YEAR:
                    d3("in", "this year");
                case LAST_YEAR:
                    d3("in", "last year");
                default:
                    return "";
            }
        }

    // set the listener of the check button on the snack bar of histogram///////////////////////////////
        private class mActionClickListenerForHistogram implements ActionClickListener {
            @Override
            public void onActionClicked(Snackbar snackbar) {
                ArrayList<CoCoinRecord> shownCoCoinRecords = new ArrayList<>();
                int index = timeIndex;
                if (axis_date == Calendar.DAY_OF_WEEK) {
                    if (CoCoinUtil.WEEK_START_WITH_SUNDAY) index++;
                    else
                        if (index == 6) index = 1;
                        else index += 2;
                }
                if (fragmentPosition == THIS_MONTH || fragmentPosition == LAST_MONTH) index++;
                funtion12(shownCoCoinRecords, index);
                ((FragmentActivity)mContext).getSupportFragmentManager()
                        .beginTransaction()
                        .add(new RecordCheckDialogFragment(), "MyDialog")
                        .commit();
            }
    }
}
