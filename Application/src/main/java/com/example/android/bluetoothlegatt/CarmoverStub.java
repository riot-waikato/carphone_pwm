package com.example.android.bluetoothlegatt;

/**
 * Created by nbk4 on 1/07/16.
 */
public class CarmoverStub {
    protected final int STOP = 0;
    protected final int FORWARD_LOW = 1;
    protected final int FORWARD_MID = 2;
    protected final int FORWARD_HIGH = 3;
    protected final int BACKWARD_LOW = 4;
    protected final int BACKWARD_MID = 5;
    protected final int BACKWARD_HIGH = 6;
    protected final int LEFT_HALF = 1;
    protected final int LEFT_FULL = 2;
    protected final int RIGHT_HALF = 3;
    protected final int RIGHT_FULL = 4;
    protected char[] chars;
    protected float X;
    protected float Y;
    protected float Z;
    public void setX(float x) {
        X = x;
    }
    public void setY(float y) {
        Y = y;
    }
    public void setZ(float z) {
        Z = z;
    }
    public char[] getChars() {
        return chars;
    }
    public void setChars(char[] ch) {
        chars = ch;
    }
    public CarmoverStub() {

    }
    public void Act50ms() {

    }
    protected void halt_motors() {
        chars[0] = '0';

    }
    protected void steer_neutral() {
        chars[1] ='0';
    }
    protected void mode_forward() {
        chars[0] = ('0' + FORWARD_LOW);
    }
    protected void mode_fast_forward() {
        chars[0] = '0' + FORWARD_HIGH;
    }
    protected void mode_fast_backward() {
        chars[0] = '0' + BACKWARD_HIGH;
    }
    protected void mode_backward() {
        chars[0] = ('0' + BACKWARD_LOW);
    }
    protected void steer_left() {
        chars[1] = LEFT_FULL;
    }
    protected void steer_right() {
        chars[1] = RIGHT_FULL;
    }
}