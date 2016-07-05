package com.example.android.bluetoothlegatt;

/**
 * Created by nbk4 on 1/07/16.
 */
/*
    Relevant functions:
        void    halt_accel          ()
        void    halt_steering       ()
        void    move_forward        (fast)      Boolean: true/false
        void    move_backward       (fast)      Boolean: true/false
        void    steer_left          ()
        void    steer_right         ()
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
            if (Y <= -5) mode_forward(true);
            else         mode_forward(false);
        }
        else if (Y  >= 3) {
            if (Y  >= 7) mode_backward(true);
            else         mode_backward(false);
        }
        //else halt_accel();

        //X movement
        /*if (X<= -3.2)    steer_left();
        else if (X>= 3.2)steer_right();
        //else             halt_steering(); */
    }
}
