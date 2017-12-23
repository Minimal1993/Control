package com.nightonke.saver.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.*;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.github.johnpersano.supertoasts.SuperToast;
import com.nightonke.saver.R;
import com.nightonke.saver.adapter.*;
import com.nightonke.saver.fragment.*;
import com.nightonke.saver.model.*;
import com.nightonke.saver.ui.*;
import com.nightonke.saver.util.CoCoinUtil;
import net.steamcrafted.materialiconlib.MaterialIconView;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.UpdateListener;


/**
 * @author liu ji
 * @version 1.4
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
        @Override
        public void onItemClick(AdapterView<?> parent1, View bo, int pol, long ident) {
            buttonClickOperation(false, pol);
        }
    };

    private AdapterView.OnItemLongClickListener gridViewLongClickListener
            = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent2, View view2, int position2, long id2) {
            buttonClickOperation(true, position2);
            return true;
        }
    };


    private void caseVerifyState(boolean longClick1, int position1){
        if (CoCoinUtil.ClickButtonDelete(position1)) {
            if (longClick1) {
                CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].init();
                oldPsw = "";
            } else {
                CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE]
                        .clear(oldPsw.length() - 1);
                if (oldPsw.length() != 0)
                    oldPsw = oldPsw.substring(0, oldPsw.length() - 1);
            }
        } else if (CoCoinUtil.ClickButtonCommit(position1)) {

        } else {
            CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE]
                    .set(oldPsw.length());
            oldPsw += CoCoinUtil.BUTTONS[position1];
            int num = oldPsw.length();
            function1(num);
        }
    }

    public void function1(int number){
        if (number == 4) {
            if (oldPsw.equals(SettingManager.getInstance().getPassword())) {
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
    }

    //duplicate code start here
    private void caseNewPass(boolean longClick3, int position3){
        if (CoCoinUtil.ClickButtonDelete(position3)) {
            if (longClick3) {
                CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].init();
                newPsw = "";
            } else {
                function8(newPsw);
            }
        }else {
            function9(position3);
            if (newPsw.length() == 4) {
                // finish the new password input
                CURRENT_STATE = PASSWORD_AGAIN;
                viewPager.setCurrentItem(PASSWORD_AGAIN, true);
            }
        }
    }

    public void function9(int posizione){
        CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].set(newPsw.length());
        newPsw += CoCoinUtil.BUTTONS[posizione];
    }

    public void function8(String psw){
        CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE]
                .clear(psw.length() - 1);
        if (psw.length() != 0)
            psw = psw.substring(0, psw.length() - 1);
    }

    private void casePassAgain(boolean longClick11, int position11){
        if (CoCoinUtil.ClickButtonDelete(position11)) {
            if (longClick11) {
                CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].init();
                againPsw = "";
            } else {
                function8(againPsw);
            }
        } else {
            function9(position11);
            if (againPsw.length() == 4) {
                // if the password again is equal to the new password
                function2(againPsw, newPsw);
            }
        }
    }


    public void function2(String altraPsw, String nuovaPsw){
        if (altraPsw.equals(nuovaPsw)) {
            CURRENT_STATE = -1;
            showToast(2);
            SettingManager.getInstance().setPassword(nuovaPsw);
            if (SettingManager.getInstance().getLoggenOn()) {
                User currentUser = BmobUser.getCurrentUser(
                        CoCoinApplication.getAppContext(), User.class);
                currentUser.setAccountBookPassword(nuovaPsw);
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
            nuovaPsw = "";
            altraPsw = "";
            showToast(1);
        }
    }
    //duplicated code end here

    //duplicated code start here pt 2
    private void buttonClickOperation(boolean longClick22, int position22) {
        switch (CURRENT_STATE) {
            case VERIFY_STATE:
                caseVerifyState(longClick22, position22);
                break;
            case NEW_PASSWORD:
                caseNewPass(longClick22, position22);
                break;
            case PASSWORD_AGAIN:
                casePassAgain(longClick22, position22);
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
                String string3 = "Password wrong";
                first(string3);
                superToast.setBackground(SuperToast.Background.RED);
                break;
            // password is different
            case 1:
                String string = "Password different";
                first(string);
                superToast.setBackground(SuperToast.Background.RED);
                break;
            // success
            case 2:
                String string2 = "You did it";
                first(string2);
                superToast.setBackground(SuperToast.Background.GREEN);
                break;
            default:
                break;
        }
        superToast.show();
    }

    public void first(String stringa){
        superToast.setText(stringa);
        superToast.getTextView().setTypeface(CoCoinUtil.typefaceLatoLight);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                coordinate(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                coordinate2(ev);
                break;
            case MotionEvent.ACTION_UP:
                coordinate2(ev);
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
    //duuplicated code end here pt2

    public void coordinate(MotionEvent tr){
        x2 = tr.getX();
        y2 = tr.getY();
    }

    public boolean coordinate2(MotionEvent ps){
        coordinate(ps);
        if (Math.abs(x1 - x2) < 20) {
            return false;
        }else
            return true;
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
