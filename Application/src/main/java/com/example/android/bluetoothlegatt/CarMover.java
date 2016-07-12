package com.example.android.bluetoothlegatt;

/**
 * Created by nbk4 on 1/07/16.
 */
/*
    Available functions:
        void    mode_forward        ()
        void    mode_fast_forward    ()
        void    mode_backward       ()
        void    mode_fast_backward   ()
        void    steer_left          ()
        void    steer_right         ()
        void    steer_neutral       ()
 */
public class CarMover extends CarmoverStub {
    public
    CarMover() {
        super();
    }

    //this is the part that the students should be modifying
    @Override
    public void Act50ms() {
        // In this function you decide what the car will be
        // doing in the next 50ms period

        // The values X, Y, Z and T are available for use.

        // X, Y and Z are the current accelerometer readings
        // Gravity gives ~9.81 m/s/s, which will be distributed
        // across the coordinate axes based on the orientation
        // of the phone.

        // T is the elapsed time in seconds since the timer
        // was started.  Controls on the screen allow you to
        // stop, start and reset the timer.

        if (X < -4) {
            steer_left();
        } else if (X > 4) {
            steer_right();
        } else if (Y > 4) {
            mode_forward();
        }
    }
}
