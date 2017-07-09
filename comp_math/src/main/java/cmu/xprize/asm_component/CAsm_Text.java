package cmu.xprize.asm_component;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.TCONST;

/**
 *
 */
public class CAsm_Text extends TextView implements IEventListener {

    float scale = getResources().getDisplayMetrics().density;
    int textSize = (int)(ASM_CONST.textSize*scale);

    public boolean isWritable = false;
    public boolean isClicked = false;

    private boolean isStruck = false;
    private boolean isBorrowable = false;

    public CAsm_Text(Context context) {

        super(context);
        reset();
    }

    public CAsm_Text(Context context, boolean isMultiplication) {

        super(context);
        reset(isMultiplication);
    }

    public CAsm_Text(Context context, AttributeSet attrs) {

        super(context, attrs);
        reset();
    }

    public CAsm_Text(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        reset();
    }

    public void setWritable(boolean _isWritable) {this.isWritable = _isWritable; }

    public boolean getIsClicked() {
        if (isClicked) {
            isClicked = false;
            return true;
        } else return false;
    }

    public void reset() {
        reset(false);
    }

    public void reset(boolean isMultiplication) {
        isClicked = false;

        setStruck(false);
        setWritable(false);
        setBorrowable(false);
        setEnabled(false);

        setTextColor(Color.BLACK);
        setGravity(Gravity.CENTER);
        if (!isMultiplication) setTextSize(textSize);

        setBackground(null);
        setTypeface(null);
        setSingleLine(true);

        setPaintFlags(0);

    }

    public void setResult(){

        setEnabled(true);
        setBackground(getResources().getDrawable(R.drawable.back));
        setWritable(true);

    }

    public void cancelResult(){

        setEnabled(false);
        setBackground(null);
        setWritable(false);

    }

    public void setStruck(boolean _isStruck){

        this.isStruck = _isStruck;

        if (isStruck) {
            setPaintFlags(getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            setAlpha(0.5f);
        }
        else {
            setPaintFlags(getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            setAlpha(1f);
        }

    }

    public boolean getIsStruck() {return this.isStruck;}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            if (isWritable || isBorrowable) {
                isClicked = true;
            }
        }
        return false;
    }

    public void onEvent(IEvent event) {
        //Called when user responds through the fingerwriter, to change the text to user's response.
        String response  = (String)event.getString(TCONST.FW_VALUE);
        this.setText(response);
    }

    public void setIsClicked(boolean _isClicked) {this.isClicked = _isClicked;}

    public void setBorrowable(boolean _isBorrowable) {this.isBorrowable = _isBorrowable;}

    public boolean getIsBorrowable() {return isBorrowable;}

    public Integer getDigit() {

        String input = getText().toString();

        if (input.equals("")) {
            return null;
        }
        else {
            try {
                return Integer.parseInt(input);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }

    }

    /* Adapted from Kevin's CAnimatorUtil. Using setTranslationX instead of setX. */
    public void wiggle(long duration, int repetition, long delay, float magnitude) {

        float currTranslation = getTranslationX();
        float offset = magnitude*getWidth();
        float[] pts = {currTranslation, offset + currTranslation, currTranslation,
                currTranslation-offset, currTranslation};

        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "translationX", pts);
        anim.setDuration(duration);
        anim.setRepeatCount(repetition);
        anim.setStartDelay(delay);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();

    }
}