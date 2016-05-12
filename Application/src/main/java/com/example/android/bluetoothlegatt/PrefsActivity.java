package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * Created by nbk4 on 10/05/16.
 */
public class PrefsActivity extends Activity {
    private String backkey = "background_color";
    private String slidekey = "slide_color";
    private String indicatorkey = "indicator_color ";
    private String prefskey = "CARPHONEPREFS";
    private int backcolor = 0xffa500;
    private int slidecolor = 0xff0030;
    private int forecolor = 0xffa500;
    private boolean backchanged = false;
    private boolean slidechanged = false;
    private boolean forechanged = false;

    private int seperate = 0;
    private int invert_disp = 1;
    private int invert_cont = 1;
    private String sepkey = "seperate";
    private String invdispkey = "invdispay";
    private String invcontkey = "invcontol";
    private boolean sepchanged = true;
    private boolean dispchanged = true;
    private boolean contchanged = true;

    private SharedPreferences sharedprefs;



    private void updatePrefs() {
        SharedPreferences.Editor editor = sharedprefs.edit();
        if(backchanged) {
            editor.putString(backkey, Integer.toString(backcolor));
            backchanged = false;
        }
        if(slidechanged) {
            editor.putString(indicatorkey, Integer.toString(slidecolor));
            slidechanged = false;
        }
        if(forechanged) {
            editor.putString(slidekey, Integer.toString(forecolor));
            forechanged = false;
        }

        if(sepchanged) {
            editor.putString(sepkey, Integer.toString(seperate));
            sepchanged = false;
        }
        if(dispchanged) {
            editor.putString(invdispkey, Integer.toString(invert_disp));
            dispchanged = false;
        }
        if(contchanged) {
            editor.putString(invcontkey, Integer.toString(invert_cont));
            contchanged = false;
        }
        editor.commit();
        System.out.println("Succesful commit!");
    }

    private void initPrefs() {
        SharedPreferences.Editor editor = sharedprefs.edit();

        editor.putString(backkey, Integer.toString(backcolor));
        backchanged = false;
        editor.commit();
        editor.putString(indicatorkey, Integer.toString(slidecolor));
        slidechanged = false;
        editor.commit();
        editor.putString(slidekey, Integer.toString(forecolor));
        forechanged = false;
        editor.commit();
        System.out.println("Succesful commit!");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefset);

        Button b = (Button)findViewById(R.id.editbackground);
        b.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                getColor(backkey, backcolor);
            }
        });
        //b.setBackgroundColor(backcolor);

        b = (Button)findViewById(R.id.editslide);
        b.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                getColor(slidekey, slidecolor);
            }
        });
        //b.setBackgroundColor(slidecolor);

        b = (Button)findViewById(R.id.editindicator);
        b.setOnClickListener( new View.OnClickListener() {

            public void onClick(View v) {
                getColor(indicatorkey, forecolor);
            }
        });
        //b.setBackgroundColor(forecolor);

        CheckBox c = (CheckBox) findViewById(R.id.invertcontrol);
        c.setOnCheckedChangeListener( new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                getChecks(invcontkey, isChecked);
            }
        });

        c = (CheckBox) findViewById(R.id.invertdisplay);
        c.setOnCheckedChangeListener( new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                getChecks(invdispkey, isChecked);
            }
        });

        c = (CheckBox) findViewById(R.id.seperatedisplay);
        c.setOnCheckedChangeListener( new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                getChecks(sepkey, isChecked);
            }
        });

        sharedprefs = getApplicationContext().getSharedPreferences(prefskey, Context.MODE_PRIVATE);
    }

    private int getChecks (final String key, boolean checked) {
        if(key == sepkey) {
            sepchanged = true;
            if(checked)
                seperate = 200;
            else
                seperate = 0;
        }
        else if (key == invdispkey) {
            dispchanged = true;
            if(checked)
                invert_disp = -1;
            else
                invert_disp = 1;
        }
        else if (key == invcontkey) {
            contchanged = true;
            if(checked)
                invert_cont = -1;
            else
                invert_cont = 1;
        }
        updatePrefs();
        return 0;
    }
    private int getColor (final String key, int color) {
        final ColorPicker cp = new ColorPicker(this, (color>>16)&0xff, (color >> 8)&0xff, (color >> 0)&0xFF ); //dereference rgb color into r/g/b color
        cp.show();

        Button okColor = (Button)cp.findViewById(R.id.okColorButton);
        okColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = null;
                /* Or the android RGB Color (see the android Color class reference) */
                if(key ==backkey) {
                    backcolor = cp.getColor();
                    backchanged = true;
                    b = (Button)findViewById(R.id.editbackground);
                }
                else if (key == slidekey) {
                    slidecolor = cp.getColor();
                    slidechanged = true;
                    b = (Button)findViewById(R.id.editslide);
                }
                else if (key == indicatorkey) {
                    forechanged = true;
                    forecolor = cp.getColor();
                    b = (Button)findViewById(R.id.editindicator);
                }
                b.setBackgroundColor(cp.getColor());
                System.out.println("******** " + cp.getColor());
                updatePrefs();
                cp.dismiss();
            }
        });
        return backcolor;
    }
}
