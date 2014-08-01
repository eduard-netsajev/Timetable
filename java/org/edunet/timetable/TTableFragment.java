package org.edunet.timetable;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public final class TTableFragment extends ListFragment implements FragmentCommunicator {
    private static final String KEY_CONTENT = "TTableFragment:Content";


    //////////////////////////
    //interface via which we communicate to hosting Activity
    private ActivityCommunicator activityCommunicator;
    private String activityAssignedValue ="";
    private static final String STRING_VALUE ="stringValue";
    public Context context;


    private ProgressDialog pDialog;

    List<Lesson> lessons_today = new ArrayList<Lesson>();
    ClassListArrayAdapter ListAdapter;

    //////////////////////////

    //since Fragment is Activity dependent you need Activity context in various cases
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        context = getActivity();
        activityCommunicator =(ActivityCommunicator)context;
    }

    public static TTableFragment newInstance(String content) {
        TTableFragment fragment = new TTableFragment();

        fragment.mContent = "Today is " + content;

        return fragment;
    }

    private String mContent = "???";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            activityAssignedValue = savedInstanceState.getString(STRING_VALUE);
        }

       // if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
       //     mContent = savedInstanceState.getString(KEY_CONTENT);
     //   }

        new GetContacts().execute();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    public void init() {
       /* dialog = new ProgressDialog(context);
        dialog.setCancelable(true);
        customAdapter = new CustomAdapter(context,
                R.layout.fb_friend_list_item, new ArrayList());
        listView.setAdapter(customAdapter);
        //communicating to activity via ActivityCommunicator interface

        activityButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                activityCommunicator.passDataToActivity("Hi from Custom Fragment");
            }
        });*/
    }
    ////////////////////







    /**
     * Async task class to get json by making HTTP call
     * */
    public class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog

            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            //TODO SET ADAPTER
             //setListAdapter(adapter);
            ListAdapter = new ClassListArrayAdapter(context, R.layout.list_item, R.id.email, lessons_today);

            setListAdapter(ListAdapter);

            ListView lv = getListView();


            //ListView day_list = (ListView) view.findViewById(R.id.list);

            // Listview on item click listener
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                        activityCommunicator.passDataToActivity("Hi from Custom Fragment");

                }
            });

        }

    }

    @Override
        public void onResume() {
            super.onResume();
            lessons_today =((Timetable)context).lessons;



            //        textView.setText(activityAssignedValue);
        }
    //FragmentCommunicator interface implementation
    @Override
    public void passDataToFragment(String someValue){
        activityAssignedValue = someValue;
        // textView.setText(activityAssignedValue);
    }



    ////////////////////


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


       /* RelativeLayout layout = new RelativeLayout(context);

        ClassView text = new ClassView(context);
        text.setClassName("Name");
        text.setClassType("Type");
        text.setStartTime("StartTime");
        text.setClassRoom("Room");

        layout.addView(text);

        return layout;
*/

//        View view = inflater.inflate(R.layout.fragment_timetable, null);

        //ListView day_list = (ListView) view.findViewById(R.id.list);


        setRetainInstance(true);

        return inflater.inflate(R.layout.fragment_timetable, container, false);

    }

    /*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_timetable, container, false);

    }*/

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putString(KEY_CONTENT, mContent);
        outState.putString(STRING_VALUE,activityAssignedValue);
    }
}




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
        if(currentListItem != null){
            Log.d("Null", currentListItem.toString());
            //return classView;
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

class ClassView extends LinearLayout {

    private TextView StartTime;
    private TextView ClassName;
    private TextView ClassRoom;
    private TextView ClassType;


    public ClassView(Context context) {
        super(context);

    //    LayoutInflater inflater =
                //LayoutInflater.from(context);//getLayoutInflater();
        //inflater.inflate(R.layout.fragment_timetable, container, false);

      //  inflater.inflate(R.layout.list_item, this);
        Log.d("I WAS HERE > ", "YEA");
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