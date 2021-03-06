package com.nightonke.saver.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.balysv.materialripple.MaterialRippleLayout;
import com.nightonke.saver.R;
import com.nightonke.saver.fragment.RecordCheckDialogFragment;
import com.nightonke.saver.model.CoCoinRecord;
import com.nightonke.saver.model.RecordManager;
import com.nightonke.saver.model.SettingManager;
import com.nightonke.saver.util.CoCoinUtil;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.ActionClickListener;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SelectedValue;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.PieChartView;

/**
 * Created by 伟平 on 2015/10/20.
 */

public class TodayViewRecyclerViewAdapter
        extends RecyclerView.Adapter<TodayViewRecyclerViewAdapter.viewHolder> {

    private OnItemClickListener onItemClickListener;
    private Context mContext;
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
    private int fragmentPosition;
    // the data of this fragment
    private ArrayList<CoCoinRecord> allData;
    // store the sum of expenses of each tag
    private Map<Integer, Double> TagExpanse;
    // store the records of each tag
    private Map<Integer, List<CoCoinRecord>> Expanse;
    // the original target value of the whole pie
    private float[] originalTargets;
    // whether the data of this fragment is empty
    private boolean IS_EMPTY;
    // the sum of the whole pie
    private double Sum;
    // the number of columns in the histogram
    private int columnNumber;
    // the axis date value of the histogram(hour, day of week and month, month)
    private int axis_date;
    // the month number
    private int month;
    // the selected position of one part of the pie
    private int pieSelectedPosition = 0;
    // the last selected position of one part of the pie
    private int lastPieSelectedPosition = -1;
    // the last selected position of one part of the histogram
    private int lastHistogramSelectedPosition = -1;
    // the date string on the footer and header
    private String dateString;
    // the date string shown in the dialog
    private String dateShownString;
    // the string shown in the dialog
    private String dialogTitle;
    // the selected tag in pie
    private int tagId = -1;
    // the selected column in histogram
    private int timeIndex;
    private MaterialDialog dialog;
    private View dialogView;

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

    public TodayViewRecyclerViewAdapter(int start, int end, Context context, int position) {

        mContext = context;
        fragmentPosition = position;
        Sum = 0;

        RecordManager recordManager = RecordManager.getInstance(mContext.getApplicationContext());

        allData = new ArrayList<>();
        if (start != -1)
            for (int i = start; i >= end; i--) allData.add(recordManager.RECORDS.get(i));

        IS_EMPTY = allData.isEmpty();

        setDateString();
        end2(recordManager);
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

    @Override
    public int getItemViewType(int position) {
        if (fragmentPosition == TODAY || fragmentPosition == YESTERDAY) {
            return position == 0 ? TYPE_HEADER : TYPE_BODY;
        }
        return TYPE_HEADER;
    }

    @Override
    public int getItemCount() {
        if (fragmentPosition == TODAY || fragmentPosition == YESTERDAY) {
            return allData.size() + 1;
        }
        return 1;
    }

    @Override
    public TodayViewRecyclerViewAdapter.viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case TYPE_HEADER: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_today_view_head, parent, false);
                return new viewHolder(view) {
                };
            }
            case TYPE_BODY: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_today_view_body, parent, false);
                return new viewHolder(view) {
                };
            }
        }

        return null;
    }

    public void function5(viewHolder oo ){
        if (IS_EMPTY) {
            oo .emptyTip.setVisibility(View.VISIBLE);
            oo .emptyTip.setText(CoCoinUtil.GetTodayViewEmptyTip(fragmentPosition));
            oo .emptyTip.setTypeface(CoCoinUtil.GetTypeface());

            oo .reset.setVisibility(View.GONE);

            oo .pie.setVisibility(View.GONE);
            oo .iconLeft.setVisibility(View.GONE);
            oo .iconRight.setVisibility(View.GONE);

            oo .histogram.setVisibility(View.GONE);
            oo .histogram_icon_left.setVisibility(View.GONE);
            oo .histogram_icon_right.setVisibility(View.GONE);
            oo .all.setVisibility(View.GONE);
            oo .dateBottom.setVisibility(View.GONE);
        }
    }

    public void function6(final viewHolder pol, final ColumnChartData pp, List<Column> col1){
        if (!(fragmentPosition == TODAY || fragmentPosition == YESTERDAY)) {


            for (int i = 0; i < columnNumber; i++) {
                if (lastHistogramSelectedPosition == -1 && originalTargets[i] == 0) {
                    lastHistogramSelectedPosition = i;
                }
                SubcolumnValue value = new SubcolumnValue(
                        originalTargets[i], CoCoinUtil.GetRandomColor());
                List<SubcolumnValue> subcolumnValues = new ArrayList<>();
                subcolumnValues.add(value);
                Column column = new Column(subcolumnValues);
                column.setHasLabels(false);
                column.setHasLabelsOnlyForSelected(false);
                col1.add(column);
            }

            Axis axisX = new Axis();
            List<AxisValue> axisValueList = new ArrayList<>();

            for (int i = 0; i < columnNumber; i++) {
                axisValueList.add(
                        new AxisValue(i).setLabel(CoCoinUtil.GetAxisDateName(axis_date, i)));
            }

            axisX.setValues(axisValueList);
            Axis axisY = new Axis().setHasLines(true);

            pp.setAxisXBottom(axisX);
            pp.setAxisYLeft(axisY);
            pp.setStacked(true);

            pol.histogram.setColumnChartData(pp);
            pol.histogram.setZoomEnabled(false);

            // two control button of histogram//////////////////////////////////////////////////////////////////
            pol.histogram_icon_left.setVisibility(View.VISIBLE);
            pol.histogram_icon_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    function8(-1, columnNumber, pp, pol);
                }
            });
            pol.histogram_icon_right.setVisibility(View.VISIBLE);
            pol.histogram_icon_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    function8(+1, 0, pp, pol);
                }
            });
        }
    }

    public void function8(int val4, int val5, final ColumnChartData p1, final viewHolder pol1){
        do {
            lastHistogramSelectedPosition
                    = (lastHistogramSelectedPosition +val4 + val5)
                    % columnNumber;
        } while (p1.getColumns()
                .get(lastHistogramSelectedPosition)
                .getValues().get(0).getValue() == 0);
        SelectedValue selectedValue =
                new SelectedValue(
                        lastHistogramSelectedPosition,
                        0,
                        SelectedValue.SelectedValueType.NONE);
        pol1.histogram.selectValue(selectedValue);
    }

    public void function7(int val1, int val2, final viewHolder holder2){
        if (lastPieSelectedPosition != -1) {
            pieSelectedPosition = lastPieSelectedPosition;
        }
        pieSelectedPosition
                = (pieSelectedPosition - val1 + val2)
                % val2;
        SelectedValue selectedValue =
                new SelectedValue(
                        pieSelectedPosition,
                        0,
                        SelectedValue.SelectedValueType.NONE);
        holder2.pie.selectValue(selectedValue);
    }

    public void function9(String text, SliceValue sliceValue, double x1){
        if ("zh".equals(CoCoinUtil.GetLanguage())) {
            text = CoCoinUtil.GetSpendString((int) sliceValue.getValue()) +
                    CoCoinUtil.GetPercentString(x1) + "\n" +
                    "于" + CoCoinUtil.GetTagName(tagId);
        } else {
            text = "Spend " + (int)sliceValue.getValue()
                    + " (takes " + String.format("%.2f", x1) + "%)\n"
                    + "in " + CoCoinUtil.GetTagName(tagId);
        }
        if ("zh".equals(CoCoinUtil.GetLanguage())) {
            dialogTitle = dateShownString +
                    CoCoinUtil.GetSpendString((int) sliceValue.getValue()) + "\n" +
                    "于" + CoCoinUtil.GetTagName(tagId);
        } else {
            dialogTitle = "Spend " + (int)sliceValue.getValue()
                    + dateShownString + "\n" +
                    "in " + CoCoinUtil.GetTagName(tagId);
        }
    }

    public void function9(CoCoinRecord coco, int calendar, float[] targets1){
        if (axis_date == Calendar.DAY_OF_WEEK) {
            if (CoCoinUtil.WEEK_START_WITH_SUNDAY) {
                targets1[coco.getCalendar().get(axis_date) - 1]
                        += coco.getMoney();
            } else {
                targets1[(coco.getCalendar().get(axis_date) + 5) % 7]
                        += coco.getMoney();
            }
        } else if (axis_date == Calendar.DAY_OF_MONTH) {
            targets1[coco.getCalendar().get(axis_date) - 1]
                    += coco.getMoney();
        } else {
            targets1[coco.getCalendar().get(axis_date)]
                    += coco.getMoney();
        }
    }

    public void function10(viewHolder holder2, ColumnChartData columnChartData2){
        if (!(fragmentPosition == TODAY || fragmentPosition == YESTERDAY)) {

// histogram data///////////////////////////////////////////////////////////////////////////////////
            float[] targets = new float[columnNumber];
            for (int i = 0; i < columnNumber; i++) targets[i] = 0;

            for (int i = Expanse.get(tagId).size() - 1; i >= 0; i--) {
                CoCoinRecord coCoinRecord = Expanse.get(tagId).get(i);
                function9(coCoinRecord, Calendar.DAY_OF_WEEK, targets);
            }

            lastHistogramSelectedPosition = -1;
            for (int i = 0; i < columnNumber; i++) {
                if (lastHistogramSelectedPosition == -1 && targets[i] != 0) {
                    lastHistogramSelectedPosition = i;
                }
                columnChartData2.getColumns().
                        get(i).getValues().get(0).setTarget(targets[i]);
            }
            holder2.histogram.startDataAnimation();
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

    public void function11(viewHolder holder3){
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

    public void function12(final viewHolder holder4, final ColumnChartData columnChartData3){
        // set the listener of the reset button/////////////////////////////////////////////////////////////
        if (!(fragmentPosition == TODAY || fragmentPosition == YESTERDAY)) {
            holder4.reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tagId = -1;
                    lastHistogramSelectedPosition = -1;

                    for (int i = 0; i < columnNumber; i++) {
                        if (lastHistogramSelectedPosition == -1
                                && originalTargets[i] != 0) {
                            lastHistogramSelectedPosition = i;
                        }
                        columnChartData3.getColumns().
                                get(i).getValues().get(0).setTarget(originalTargets[i]);
                    }

                    holder4.histogram.startDataAnimation();
                }
            });
        }
    }

    public void function4(final viewHolder holder1){
        holder1.date.setText(dateString);
        holder1.dateBottom.setText(dateString);
        holder1.expanseSum.setText(CoCoinUtil.GetInMoney((int) Sum));
        holder1.date.setTypeface(CoCoinUtil.GetTypeface());
        holder1.dateBottom.setTypeface(CoCoinUtil.GetTypeface());
        holder1.expanseSum.setTypeface(CoCoinUtil.typefaceLatoLight);
        function5(holder1);
        holder1.emptyTip.setVisibility(View.GONE);
        final ArrayList<SliceValue> sliceValues = new ArrayList<>();

        for (Map.Entry<Integer, Double> entry : TagExpanse.entrySet()) {
            if (entry.getValue() >= 1) {
                SliceValue sliceValue = new SliceValue(
                        (float)(double)entry.getValue(),
                        mContext.getApplicationContext().getResources().
                                getColor(CoCoinUtil.GetTagColorResource(entry.getKey())));
                sliceValue.setLabel(String.valueOf(entry.getKey()));
                sliceValues.add(sliceValue);
            }
        }

        final PieChartData pieChartData = new PieChartData(sliceValues);
        pieChartData.setHasLabels(false);
        pieChartData.setHasLabelsOnlyForSelected(false);
        pieChartData.setHasLabelsOutside(false);
        pieChartData.setHasCenterCircle(SettingManager.getInstance().getIsHollow());
        holder1.pie.setPieChartData(pieChartData);
        holder1.pie.setChartRotationEnabled(false);
        holder1.iconRight.setVisibility(View.VISIBLE);
        holder1.iconRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                function7(-1,sliceValues.size(), holder1);
            }
        });
        holder1.iconLeft.setVisibility(View.VISIBLE);
        holder1.iconLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                function7(+1, 0, holder1);
            }
        });

        final List<Column> columns = new ArrayList<>();
        final ColumnChartData columnChartData = new ColumnChartData(columns);
        function6(holder1, columnChartData, columns);
        if (fragmentPosition == TODAY || fragmentPosition == YESTERDAY) {
            holder1.histogram_icon_left.setVisibility(View.INVISIBLE);
            holder1.histogram_icon_right.setVisibility(View.INVISIBLE);
            holder1.histogram.setVisibility(View.GONE);
            holder1.dateBottom.setVisibility(View.GONE);
            holder1.reset.setVisibility(View.GONE);
        }
// set value touch listener of pie//////////////////////////////////////////////////////////////////
        holder1.pie.setOnValueTouchListener(new PieChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int p, SliceValue sliceValue) {
                // snack bar
                RecordManager recordManager
                        = RecordManager.getInstance(mContext.getApplicationContext());
                String text = null;
                tagId = Integer.valueOf(String.valueOf(sliceValue.getLabelAsChars()));
                double percent = sliceValue.getValue() / Sum * 100;
                function9(text, sliceValue, percent);
                Snackbar snackbar = Snackbar
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
                                .actionListener(new mActionClickListenerForPie());
                SnackbarManager.show(snackbar);

                if (p == lastPieSelectedPosition) {
                    return;
                } else {
                    lastPieSelectedPosition = p;
                }
                function10(holder1, columnChartData );
            }

            @Override
            public void onValueDeselected() {

            }
        });

        function11(holder1);
        function12(holder1, columnChartData);

// set the listener of the show all button//////////////////////////////////////////////////////////
        holder1.all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FragmentActivity)mContext).getSupportFragmentManager()
                        .beginTransaction()
                        .add(new RecordCheckDialogFragment(), "MyDialog")
                        .commit();
            }
        });

    }

    public void function14(final viewHolder holder7, final int position1){
    holder7.tagImage.setImageResource(
            CoCoinUtil.GetTagIcon(allData.get(position1 - 1).getTag()));
    holder7.money.setText((int) allData.get(position1 - 1).getMoney() + "");
    holder7.money.setTypeface(CoCoinUtil.typefaceLatoLight);
    holder7.cell_date.setText(allData.get(position1 - 1).getCalendarString());
    holder7.cell_date.setTypeface(CoCoinUtil.typefaceLatoLight);
    holder7.remark.setText(allData.get(position1 - 1).getRemark());
    holder7.remark.setTypeface(CoCoinUtil.typefaceLatoLight);
    holder7.index.setText(position1 + "");
    holder7.index.setTypeface(CoCoinUtil.typefaceLatoLight);
    holder7.layout.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String subTitle;
            double spend = allData.get(position1 - 1).getMoney();
            int tagId = allData.get(position1 - 1).getTag();
            if ("zh".equals(CoCoinUtil.GetLanguage())) {
                subTitle = CoCoinUtil.GetSpendString((int)spend) +
                        "于" + CoCoinUtil.GetTagName(tagId);
            } else {
                subTitle = "Spend " + (int)spend +
                        "in " + CoCoinUtil.GetTagName(tagId);
            }
            dialog = new MaterialDialog.Builder(mContext)
                    .icon(CoCoinUtil.GetTagIconDrawable(allData.get(position1 - 1).getTag()))
                    .limitIconToDefaultSize()
                    .title(subTitle)
                    .customView(R.layout.dialog_a_record, true)
                    .positiveText(R.string.get)
                    .show();
            dialogView = dialog.getCustomView();
            TextView remark = (TextView)dialogView.findViewById(R.id.remark);
            TextView date = (TextView)dialogView.findViewById(R.id.date);
            remark.setText(allData.get(position1 - 1).getRemark());
            date.setText(allData.get(position1 - 1).getCalendarString());
        }
    });
}

    //start
    @Override
    public void onBindViewHolder(final viewHolder holder, final int position) {
        int i = getItemViewType(position);
        if (i == 0) {
            function4(holder);
        }else
            function14(holder, position);
    }
    //end

// view holder class////////////////////////////////////////////////////////////////////////////////
    public static class viewHolder extends RecyclerView.ViewHolder {
        @Optional
        @InjectView(R.id.date)
        TextView date;
        @Optional
        @InjectView(R.id.date_bottom)
        TextView dateBottom;
        @Optional
        @InjectView(R.id.expanse)
        TextView expanseSum;
        @Optional
        @InjectView(R.id.empty_tip)
        TextView emptyTip;
        @Optional
        @InjectView(R.id.chart_pie)
        PieChartView pie;
        @Optional
        @InjectView(R.id.histogram)
        ColumnChartView histogram;
        @Optional
        @InjectView(R.id.icon_left)
        MaterialIconView iconLeft;
        @Optional
        @InjectView(R.id.icon_right)
        MaterialIconView iconRight;
        @Optional
        @InjectView(R.id.histogram_icon_left)
        MaterialIconView histogram_icon_left;
        @Optional
        @InjectView(R.id.histogram_icon_right)
        MaterialIconView histogram_icon_right;
        @Optional
        @InjectView(R.id.icon_reset)
        MaterialIconView reset;
        @Optional
        @InjectView(R.id.all)
        MaterialIconView all;
        @Optional
        @InjectView(R.id.tag_image)
        ImageView tagImage;
        @Optional
        @InjectView(R.id.money)
        TextView money;
        @Optional
        @InjectView(R.id.cell_date)
        TextView cell_date;
        @Optional
        @InjectView(R.id.remark)
        TextView remark;
        @Optional
        @InjectView(R.id.index)
        TextView index;
        @Optional
        @InjectView(R.id.material_ripple_layout)
        MaterialRippleLayout layout;

        viewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

// set the listener of the check button on the snack bar of pie/////////////////////////////////////
    private class mActionClickListenerForPie implements ActionClickListener {
        @Override
        public void onActionClicked(Snackbar snackbar) {
            List<CoCoinRecord> shownCoCoinRecords = Expanse.get(tagId);
            ((FragmentActivity)mContext).getSupportFragmentManager()
                    .beginTransaction()
                    .add(new RecordCheckDialogFragment(), "MyDialog")
                    .commit();
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
            function22(shownCoCoinRecords, index);
            ((FragmentActivity)mContext).getSupportFragmentManager()
                    .beginTransaction()
                    .add(new RecordCheckDialogFragment(), "MyDialog")
                    .commit();
        }
}

    private void function22( ArrayList<CoCoinRecord> temp, int ind) {
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

    public String function55(){
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
            default:
                return null;
        }
    }

    public String function66(){
        switch (fragmentPosition) {
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

    // set the dateString shown in snack bar in this fragment///////////////////////////////////////////
    private String getSnackBarDateString() {
        function55();
        if(fragmentPosition>=0 && fragmentPosition<=3) {
            function55();
        }else
            function66();
        return "";
    }

    public void method1(String basicTodayDateString1, Calendar today1, String str1){
        dateString = basicTodayDateString1.substring(6, basicTodayDateString1.length());
        dateShownString = mContext.getResources().getString(Integer.parseInt(str1));
        month = today1.get(Calendar.MONTH);
    }

    public void method2(Calendar today1, String str1){
        Calendar leftWeekRange = CoCoinUtil.GetThisWeekLeftRange(today1);
        Calendar rightWeekRange = CoCoinUtil.GetThisWeekRightShownRange(today1);
        dateString = CoCoinUtil.GetMonthShort(leftWeekRange.get(Calendar.MONTH) + 1)
                + " " + leftWeekRange.get(Calendar.DAY_OF_MONTH) + " " +
                leftWeekRange.get(Calendar.YEAR) + " - " +
                CoCoinUtil.GetMonthShort(rightWeekRange.get(Calendar.MONTH) + 1)
                + " " + rightWeekRange.get(Calendar.DAY_OF_MONTH) + " " +
                rightWeekRange.get(Calendar.YEAR);
        dateShownString = mContext.getResources().getString(Integer.parseInt(str1));
        month = -1;
    }

    public void method3(Calendar today1, String str1){
        dateString = CoCoinUtil.GetMonthShort(today1.get(Calendar.MONTH) + 1)
                + " " + today1.get(Calendar.YEAR);
        dateShownString
                = mContext.getResources().getString(Integer.parseInt(str1));
        month = today1.get(Calendar.MONTH);
    }

    public void method4(String str1){
        dateShownString = mContext.getResources().getString(Integer.parseInt(str1));
        month = -1;
    }

// set the dateString of this fragment//////////////////////////////////////////////////////////////
    private void setDateString() {
        String basicTodayDateString;
        String basicYesterdayDateString;
        Calendar today = Calendar.getInstance();
        Calendar yesterday = CoCoinUtil.GetYesterdayLeftRange(today);
        basicTodayDateString = "--:-- ";
        basicTodayDateString += CoCoinUtil.GetMonthShort(today.get(Calendar.MONTH) + 1)
                + " " + today.get(Calendar.DAY_OF_MONTH) + " " +
                today.get(Calendar.YEAR);
        basicYesterdayDateString = "--:-- ";
        basicYesterdayDateString += CoCoinUtil.GetMonthShort(today.get(Calendar.MONTH) + 1)
                + " " + yesterday.get(Calendar.DAY_OF_MONTH) + " " +
                yesterday.get(Calendar.YEAR);
        int c = fragmentPosition;
        if(c>= 0 && c<=3){
            function03(basicTodayDateString, today);
        }else
            function47(today);
    }

    public void function03(String basicTodayDateString, Calendar today) {
        switch (fragmentPosition) {
            case TODAY:
                method1(basicTodayDateString, today, "today");
                break;
            case YESTERDAY:
                method1(basicTodayDateString, today, "yesterday");
                break;
            case THIS_WEEK:
                method2(today, "this week");
                break;
            case LAST_WEEK:
                method2(today, "last week");
                break;
                default:
        }
    }

    public void function47(Calendar today){
        switch (fragmentPosition) {
            case THIS_MONTH:
                method3(today, "this month");
                break;
            case LAST_MONTH:
                Calendar lastMonthCalendar = CoCoinUtil.GetLastMonthLeftRange(today);
                method3(today, "last month");
                break;
            case THIS_YEAR:
                dateString = today.get(Calendar.YEAR) + "";
                method4("this year");
                break;
            case LAST_YEAR:
                Calendar lastYearCalendar = CoCoinUtil.GetLastYearLeftRange(today);
                dateString = lastYearCalendar.get(Calendar.YEAR) + "";
                method4("last year");
                break;
                default:
        }
    }

    public void fh(String p1, String p2){
        if ("zh".equals(CoCoinUtil.GetLanguage())) {
            p1 = mContext.getResources().getString(R.string.on);
            p2 = CoCoinUtil.GetSpendString((int)Sum);
        } else {
            p1 = CoCoinUtil.GetSpendString((int)Sum);
            p2 = "";
        }
    }

    private void getAllDataDialogTitle() {
        String prefix=null;
        String postfix=null;
        fh(prefix, postfix);
        int h = fragmentPosition;
        if(h>0 && h<=3){
            function33(prefix, postfix);
        }else
            function77(prefix, postfix);
    }

    public String function77(String prefix, String postfix){
        switch (fragmentPosition) {

            case THIS_MONTH:
                return prefix + mContext.getResources().
                        getString(R.string.this_month_date_string) + postfix;
            case LAST_MONTH:
                return prefix + mContext.getResources().
                        getString(R.string.last_month_date_string) + postfix;
            case THIS_YEAR:
                return prefix + mContext.getResources().
                        getString(R.string.this_year_date_string) + postfix;
            case LAST_YEAR:
                return prefix + mContext.getResources().
                        getString(R.string.last_year_date_string) + postfix;
            default:
                return "";
        }
    }

    public String function33(String prefix, String postfix){
        switch (fragmentPosition) {
            case TODAY:
                return prefix + mContext.getResources().
                        getString(R.string.today_date_string) + postfix;
            case YESTERDAY:
                return prefix + mContext.getResources().
                        getString(R.string.yesterday_date_string) + postfix;
            case THIS_WEEK:
                return prefix + mContext.getResources().
                        getString(R.string.this_week_date_string) + postfix;
            case LAST_WEEK:
                return prefix + mContext.getResources().
                        getString(R.string.last_week_date_string) + postfix;
        }
        return prefix;
    }
}
