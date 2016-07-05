package com.example.android.bluetoothlegatt;

/**
 * Created by nbk4 on 1/07/16.
 */
/*
    Relevant functions:
        void    mode_forward        ()
        void    mode_fastforward    ()
        void    mode_backward       ()
        void    mode_fastbackward   ()
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
        //given values X, Y, Z, decide on how the car should move
        //X, Y, Z are accelerometer values
        //gravity gives ~9.81 Newtons, will be distributed across axis
        //based on the orientation of the phone
        //distribution will essentially be 3d pythagoras, or edge lengths
        //of a 4-sided polyhedron/3-sided pyramid

        //Y movement
        if (Y  <= -2) {
            if (Y <= -5) mode_fast_backward();
            else         mode_forward();
        }
        else if (Y  >= 3) {
            if (Y  >= 7) mode_fast_backward();
            else         mode_backward();
        }
        //else halt_accel();

        //X movement
        if (X<= -3.2)    steer_left();
        else if (X>= 3.2)steer_right();
        else             steer_neutral();
    }
}
