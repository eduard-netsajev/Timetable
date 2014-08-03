package org.edunet.timetable;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

class ClassListArrayAdapter extends ArrayAdapter<Lesson> {

    private Context mContext;

    private int selectedItem = -1;

    public void setSelection(int position) {
        selectedItem = position;
    }

    public ClassListArrayAdapter(Context context, int resource, int textViewResourceId, List<Lesson> objects) {
        super(context, resource, textViewResourceId, objects);
        mContext = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ClassView classView;

        //Take the item at position
        Lesson currentListItem = getItem(position);

        if (convertView == null) {
            classView = new ClassView(mContext);
        } else {
            classView = (ClassView) convertView;
        }

        //Set ClassName field text to currentListItem.ClassName
        classView.setClassName(currentListItem.getName());

        //set other 3 fields
        classView.setStartTime(currentListItem.getStart_time());
        classView.setClassRoom(currentListItem.getRoom());
        classView.setClassType(currentListItem.getType());


        return classView;

    }

}
