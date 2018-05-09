package com.android.mandalchat.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.android.mandalchat.R;
import com.android.mandalchat.data.StaticConfig;
import com.android.mandalchat.util.AndyUtils;
import com.android.mandalchat.util.CommanClass;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {

    CommanClass cc;

    FloatingActionButton fab;
    CardView cvAdd;
    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private EditText editTextUsername, editTextPassword, et_mobile,et_joining,et_profession;
    public static String STR_EXTRA_ACTION_REGISTER = "register";

    Calendar myCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        cc = new CommanClass(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        cvAdd = (CardView) findViewById(R.id.cv_add);
        editTextUsername = (EditText) findViewById(R.id.et_username);
        editTextPassword = (EditText) findViewById(R.id.et_password);
        et_mobile = (EditText) findViewById(R.id.et_mobile);
        et_joining = (EditText) findViewById(R.id.et_joining);
        et_profession = (EditText) findViewById(R.id.et_profession);

        clickListner();



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ShowEnterAnimation();
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateRevealClose();
            }
        });
    }

    private void clickListner() {

        et_joining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new DatePickerDialog(RegisterActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd-MM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        et_joining.setText(sdf.format(myCalendar.getTime()));
    }

    private void ShowEnterAnimation() {
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.fabtransition);
        getWindow().setSharedElementEnterTransition(transition);

        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                cvAdd.setVisibility(View.GONE);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
                animateRevealShow();
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }


        });
    }

    public void animateRevealShow() {
        Animator mAnimator = ViewAnimationUtils.createCircularReveal(cvAdd, cvAdd.getWidth()/2,0, fab.getWidth() / 2, cvAdd.getHeight());
        mAnimator.setDuration(500);
        mAnimator.setInterpolator(new AccelerateInterpolator());
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                cvAdd.setVisibility(View.VISIBLE);
                super.onAnimationStart(animation);
            }
        });
        mAnimator.start();
    }

    public void animateRevealClose() {
        Animator mAnimator = ViewAnimationUtils.createCircularReveal(cvAdd,cvAdd.getWidth()/2,0, cvAdd.getHeight(), fab.getWidth() / 2);
        mAnimator.setDuration(500);
        mAnimator.setInterpolator(new AccelerateInterpolator());
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cvAdd.setVisibility(View.INVISIBLE);
                super.onAnimationEnd(animation);
                fab.setImageResource(R.drawable.ic_signup);
                RegisterActivity.super.onBackPressed();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
        mAnimator.start();
    }
    @Override
    public void onBackPressed() {
        animateRevealClose();
    }

    public void clickRegister(View view) {
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();
        String mobile = et_mobile.getText().toString();
        String joining_date = et_joining.getText().toString().toString();
        String profession = et_profession.getText().toString().toString();

        if (!cc.isConnectingToInternet()) {

        }else if (username.equals("")) {
            cc.showToast("Please enter email");

            editTextUsername.requestFocus();
        }else if (!AndyUtils.eMailValidation(editTextUsername.getText().toString())) {
            cc.showToast("Please enter valid email");

            editTextUsername.requestFocus();
        }else if (password.equals("")) {

            cc.showToast("Please enter password");
            editTextPassword.requestFocus();
        }else if (mobile.equals("")) {

            cc.showToast("Please enter mobile number");
            et_mobile.requestFocus();

        }else if (joining_date.equals("")) {

            cc.showToast("Please choose joining date");

        }else if (profession.equals("")) {

            cc.showToast("Please enter profession");

        }else {
            Intent data = new Intent();
            data.putExtra(StaticConfig.STR_EXTRA_USERNAME, username);
            data.putExtra(StaticConfig.STR_EXTRA_PASSWORD, password);
            data.putExtra(StaticConfig.STR_EXTRA_JOINING, joining_date);
            data.putExtra(StaticConfig.STR_EXTRA_PROFESSION, profession);
            data.putExtra(StaticConfig.STR_EXTRA_MOBILE, mobile);
            data.putExtra(StaticConfig.STR_EXTRA_ACTION, STR_EXTRA_ACTION_REGISTER);
            setResult(RESULT_OK, data);
            finish();
        }

        /*if(validate(username, password, repeatPassword)){
            Intent data = new Intent();
            data.putExtra(StaticConfig.STR_EXTRA_USERNAME, username);
            data.putExtra(StaticConfig.STR_EXTRA_PASSWORD, password);
            data.putExtra(StaticConfig.STR_EXTRA_ACTION, STR_EXTRA_ACTION_REGISTER);
            setResult(RESULT_OK, data);
            finish();
        }else {
            Toast.makeText(this, "Invalid email or not match password", Toast.LENGTH_SHORT).show();
        }*/
    }

    /**
     * Validate email, pass == re_pass
     * @param emailStr
     * @param password
     * @return
     */
    private boolean validate(String emailStr, String password, String repeatPassword) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return password.length() > 0 && repeatPassword.equals(password) && matcher.find();
    }
}
