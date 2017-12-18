package com.nightonke.saver.activity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.SuperToast;
import com.nightonke.saver.R;
import com.nightonke.saver.adapter.PasswordChangeButtonGridViewAdapter;
import com.nightonke.saver.adapter.PasswordChangeFragmentAdapter;
import com.nightonke.saver.fragment.CoCoinFragmentManager;
import com.nightonke.saver.fragment.PasswordChangeFragment;
import com.nightonke.saver.model.SettingManager;
import com.nightonke.saver.model.User;
import com.nightonke.saver.ui.FixedSpeedScroller;
import com.nightonke.saver.ui.MyGridView;
import com.nightonke.saver.util.CoCoinUtil;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.lang.reflect.Field;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.UpdateListener;

/**
 * @author LIn jo
 * @version 1.3
 */

public class EditPasswordActivity extends AppCompatActivity {
    //some declarations

    private Context mContext;

    private MyGridView myGridView;
    private PasswordChangeButtonGridViewAdapter myGridViewAdapter;

    private MaterialIconView back;

    private static final int VERIFY_STATE = 0;
    private static final int NEW_PASSWORD = 1;
    private static final int PASSWORD_AGAIN = 2;

    private int CURRENT_STATE = VERIFY_STATE;

    private String oldPsw = "";
    private String newPsw = "";
    private String againPsw = "";

    private ViewPager viewPager;
    private FragmentPagerAdapter adapter;

    private SuperToast superToast;

    private float x1, y1, x2, y2;

    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        mContext = this;

        int currentapiVersion = Build.VERSION.SDK_INT;

        if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP) {
            // Do something for lollipop and above versions
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(mContext, R.color.statusBarColor));
        } else{
            // do something for phones running an SDK before lollipop
        }

        viewPager = (ViewPager)findViewById(R.id.viewpager);

        /*
        removed animation because this code provokes a reflection
         */
/*        try {
            Interpolator sInterpolator = new AccelerateInterpolator();
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller
                    = new FixedSpeedScroller(viewPager.getContext(), sInterpolator);
            scroller.setmDuration(1000);
            mScroller.set(viewPager, scroller);
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }*/

        adapter = new PasswordChangeFragmentAdapter(getSupportFragmentManager());

        viewPager.setOffscreenPageLimit(3);
        viewPager.setScrollBarFadeDuration(1000);

        viewPager.setAdapter(adapter);

        myGridView = (MyGridView)findViewById(R.id.gridview);
        myGridViewAdapter = new PasswordChangeButtonGridViewAdapter(this);
        myGridView.setAdapter(myGridViewAdapter);

        myGridView.setOnItemClickListener(gridViewClickListener);
        myGridView.setOnItemLongClickListener(gridViewLongClickListener);

        myGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        myGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        View lastChild = myGridView.getChildAt(myGridView.getChildCount() - 1);
                        myGridView.setLayoutParams(
                                new LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.FILL_PARENT, lastChild.getBottom()));
                    }
                });

        back = (MaterialIconView)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        superToast = new SuperToast(this);

        title = (TextView)findViewById(R.id.title);
        title.setTypeface(CoCoinUtil.typefaceLatoLight);
        if (SettingManager.getInstance().getFirstTime()) {
            title.setText(mContext.getResources().getString(R.string.app_name));
        } else {
            title.setText(mContext.getResources().getString(R.string.change_password));
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SuperToast.cancelAllSuperToasts();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    @Override
    public void finish() {
        SuperToast.cancelAllSuperToasts();
        super.finish();
    }

    private AdapterView.OnItemClickListener gridViewClickListener
            = new AdapterView.OnItemClickListener() {
        //FIXED
        @Override
        public void onItemClick(AdapterView<?> parenti, View vista, int posizione, long ident) {
            buttonClickOperation(false, posizione);
        }
    };

    private AdapterView.OnItemLongClickListener gridViewLongClickListener
            = new AdapterView.OnItemLongClickListener() {
        //FIXED
        @Override
        public boolean onItemLongClick(AdapterView<?> parente1, View vista1, int posizion1, long ident1) {
            buttonClickOperation(true, posizione);
            return true;
        }
    };

    //FIXED
    private void caseVerifyState(int posizione2){
        if (CoCoinUtil.ClickButtonDelete(posizione2)) {
            if (longClick) {
                CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].init();
                oldPsw = "";
            } else {
                CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE]
                        .clear(oldPsw.length() - 1);
                if (oldPsw.length() != 0)
                    oldPsw = oldPsw.substring(0, oldPsw.length() - 1);
            }
        } else if (CoCoinUtil.ClickButtonCommit(position)) {

        } else {
            CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE]
                    .set(oldPsw.length());
            oldPsw += CoCoinUtil.BUTTONS[position];
            if (oldPsw.length() == 4) {
                function1(oldPsw.equals(SettingManager.getInstance().getPassword()));
            }
        }
    }

    public void function1(String stringa){
        if (stringa) {
            // old password correct
            // notice that if the old password is correct,
            // we won't go back to VERIFY_STATE any more
            CURRENT_STATE = NEW_PASSWORD;
            viewPager.setCurrentItem(NEW_PASSWORD, true);
        } else {
            // old password wrong
            CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE]
                    .clear(4);
            showToast(0);
            oldPsw = "";
        }
    }



    //FIXED
    private void caseNewPass(int position1){
        if (CoCoinUtil.ClickButtonDelete(position1)) {
            if (longClick) {
                CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].init();
                newPsw = "";
            } else {
                CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE]
                        .clear(newPsw.length() - 1);
                if (newPsw.length() != 0)
                    newPsw = newPsw.substring(0, newPsw.length() - 1);
            }
        } else if (CoCoinUtil.ClickButtonCommit(position1)) {

        } else {
            CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE]
                    .set(newPsw.length());
            newPsw += CoCoinUtil.BUTTONS[position];
            if (newPsw.length() == 4) {
                // finish the new password input
                CURRENT_STATE = PASSWORD_AGAIN;
                viewPager.setCurrentItem(PASSWORD_AGAIN, true);
            }
        }
    }

    //FIXED
    private void casePassAgain(boolean longClick1, int position2){
        if (CoCoinUtil.ClickButtonDelete(position2)) {
            if (longClick1) {
                CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].init();
                againPsw = "";
            } else {
                CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE]
                        .clear(againPsw.length() - 1);
                if (againPsw.length() != 0)
                    againPsw = againPsw.substring(0, againPsw.length() - 1);
            }
        } else if (CoCoinUtil.ClickButtonCommit(position2)) {

        } else {
            CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE]
                    .set(againPsw.length());
            againPsw += CoCoinUtil.BUTTONS[position2];
            if (againPsw.length() == 4) {
                function2(againPsw, newPsw);
            }
        }
    }

    public void function2(altrapsw, nuovapsw){
        // if the password again is equal to the new password
        if (altrapsw.equals(newPsw)) {
            CURRENT_STATE = -1;
            showToast(2);
            SettingManager.getInstance().setPassword(newPsw);
            if (SettingManager.getInstance().getLoggenOn()) {
                User currentUser = BmobUser.getCurrentUser(
                        CoCoinApplication.getAppContext(), User.class);
                currentUser.setAccountBookPassword(nuovapsw);
                currentUser.update(CoCoinApplication.getAppContext(),
                        currentUser.getObjectId(), new UpdateListener() {
                            @Override
                            public void onSuccess() {
                                Log.d("Saver", "Update password successfully.");
                            }

                            @Override
                            public void onFailure(int code, String msg) {
                                Log.d("Saver", "Update password failed.");
                            }
                        });
            }
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);
        } else {
            CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].clear(4);
            CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE - 1].init();
            CURRENT_STATE = NEW_PASSWORD;
            viewPager.setCurrentItem(NEW_PASSWORD, true);
            nuovapsw = "";
            altrapsw = "";
            showToast(1);
        }
    }

    private void buttonClickOperation(boolean longClick3, int position3) {
        switch (CURRENT_STATE) {
            case VERIFY_STATE:
                caseVerifyState(position3);
                break;
            case NEW_PASSWORD:
                caseNewPass(position3);
                break;
            case PASSWORD_AGAIN:
                casePassAgain(longClick3, position3);
                break;
            default:
                break;
        }
    }

    private void showToast(int toastType) {
        SuperToast.cancelAllSuperToasts();

        superToast.setAnimations(CoCoinUtil.TOAST_ANIMATION);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setTextColor(Color.parseColor("#ffffff"));
        superToast.setTextSize(SuperToast.TextSize.SMALL);

        switch (toastType) {
            // old password wrong
            case 0:

                superToast.setText(
                        mContext.getResources().getString(R.string.toast_password_wrong));
                superToast.setBackground(SuperToast.Background.RED);
                superToast.getTextView().setTypeface(CoCoinUtil.typefaceLatoLight);

                break;
            // password is different
            case 1:

                superToast.setText(
                        mContext.getResources().getString(R.string.different_password));
                superToast.setBackground(SuperToast.Background.RED);
                superToast.getTextView().setTypeface(CoCoinUtil.typefaceLatoLight);

                break;
            // success
            case 2:

                superToast.setText(
                        mContext.getResources().getString(R.string.set_password_successfully));
                superToast.setBackground(SuperToast.Background.GREEN);
                superToast.getTextView().setTypeface(CoCoinUtil.typefaceLatoLight);

                break;
            default:
                break;
        }

        superToast.show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = ev.getX();
                y1 = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                x2 = ev.getX();
                y2 = ev.getY();
                if (Math.abs(x1 - x2) > 20) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                x2 = ev.getX();
                y2 = ev.getY();
                if (Math.abs(x1 - x2) > 20) {
                    return true;
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        for (int i = 0; i < 3; i++) {
            CoCoinFragmentManager.passwordChangeFragment[i].onDestroy();
            CoCoinFragmentManager.passwordChangeFragment[i] = null;
        }
        super.onDestroy();
    }

}
