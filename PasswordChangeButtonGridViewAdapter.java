package com.nightonke.saver.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.nightonke.saver.R;
import com.nightonke.saver.model.SettingManager;
import com.nightonke.saver.util.CoCoinUtil;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

/**
 * Created by 伟平 on 2015/10/16.
 */



public class PasswordChangeButtonGridViewAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context mContext;

    private ViewHolder holder;

    //duplicated code
    public PasswordChangeButtonGridViewAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return CoCoinUtil.BUTTONS.length;
    }

    @Override
    public Integer getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            function4(position);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == 11) {
            function1();
        } else if (position == 9) {
            function2();
        } else {
            function3(position);
        }

        holder.ml.setRippleDuration(300);
        holder.fl.setBackgroundColor(CoCoinUtil.getAlphaColor(SettingManager.getInstance().getRemindColor()));
        holder.ml.setRippleColor(SettingManager.getInstance().getRemindColor());
        holder.iv.setColor(SettingManager.getInstance().getRemindColor());
        holder.tv.setTextColor(SettingManager.getInstance().getRemindColor());
        holder.fl.setBackgroundColor(CoCoinUtil.getAlphaColor(CoCoinUtil.MY_BLUE));
        holder.ml.setRippleColor(CoCoinUtil.MY_BLUE);
        holder.iv.setColor(CoCoinUtil.MY_BLUE);
        holder.tv.setTextColor(CoCoinUtil.MY_BLUE);

        return convertView;
    }

    private class ViewHolder {
        FrameLayout fl;
        TextView tv;
        MaterialIconView iv;
        MaterialRippleLayout ml;
    }

    public void function1(){
        restore();
        holder.iv.setIcon(MaterialDrawableBuilder.IconValue.CHECK);
    }

    public void function2(){
        holder.iv.setIcon(MaterialDrawableBuilder.IconValue.ERASER);
        restore();
    }

    public void function3(int position){
        start();
        holder.tv.setText(CoCoinUtil.BUTTONS[position]);
    }

    public void function4(int position1){
        start();
        holder.tv.setText(CoCoinUtil.BUTTONS[position1]);
    }

    public void restore(){
        holder.tv.setTypeface(CoCoinUtil.typefaceLatoHairline);
        holder.tv.setVisibility(View.INVISIBLE);
        holder.ml.setRippleAlpha(50);
    }

    public void start(){
        holder.iv.setVisibility(View.INVISIBLE);
        holder.tv.setTypeface(CoCoinUtil.typefaceLatoHairline);
        holder.ml.setRippleDelayClick(false);
    }
}
