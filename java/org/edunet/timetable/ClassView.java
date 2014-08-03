package org.edunet.timetable;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

class ClassView extends LinearLayout {

    private TextView StartTime;
    private TextView ClassName;
    private TextView ClassRoom;
    private TextView ClassType;


    public ClassView(Context context) {
        super(context);

        LayoutInflater.from(context).inflate(R.layout.list_item, this);

        this.StartTime = (TextView) findViewById(R.id.name);
        this.ClassName = (TextView) findViewById(R.id.email);
        this.ClassRoom = (TextView) findViewById(R.id.textViewClassRoom);
        this.ClassType = (TextView) findViewById(R.id.mobile);

        Typeface tf =((Timetable)context).tf;
        if(tf != null) {
            this.ClassName.setTypeface(tf);
        }
    }

    public void setClassName(String Name) {
        this.ClassName.setText(Name);
    }

    public void setStartTime(String Number) {
        this.StartTime.setText(Number);
    }

    public void setClassRoom(String Room) {
        this.ClassRoom.setText(Room);
    }

    public void setClassType(String Groups) {
        this.ClassType.setText(Groups);
    }
}