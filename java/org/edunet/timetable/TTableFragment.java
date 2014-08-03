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
import java.util.List;


public final class TTableFragment extends ListFragment implements FragmentCommunicator {
    private static final String KEY_CONTENT = "TTableFragment:Content";


    //////////////////////////
    //interface via which we communicate to hosting Activity
    private ActivityCommunicator activityCommunicator;
    private String activityAssignedValue ="";
    private static final String STRING_VALUE ="stringValue";
    public Context context;


  //  private ProgressDialog pDialog;

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

    public static TTableFragment newInstance(int content) {
        TTableFragment fragment = new TTableFragment();

        fragment.mContent = content;

        return fragment;
    }

    private int mContent = -1;

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

          //  pDialog = new ProgressDialog(context);
          //  pDialog.setMessage("Please wait...");
          //  pDialog.setCancelable(false);
          //  pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
          //  if (pDialog.isShowing())
          //      pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            //TODO SET ADAPTER
             //setListAdapter(adapter);
            ListAdapter = new ClassListArrayAdapter(context, R.layout.list_item, R.id.email, lessons_today);

            send_lesson_count();

            setListAdapter(ListAdapter);

            ListView lv = getListView();

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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            send_lesson_count();
        }
    }

    public void send_lesson_count(){

        if(this.getUserVisibleHint()) {
            ((Timetable) context).fragmentCommunicator = this;
            if (lessons_today.size() == 0) {
                // TextView today = (TextView) findViewById(R.id.today);
                activityCommunicator.passDataToActivity("TEXT:No lessons");
            } else {
                activityCommunicator.passDataToActivity("TEXT:" + lessons_today.size() + " lessons");
            }
        }
    }

    @Override
        public void onResume() {
            super.onResume();
        lessons_today = new ArrayList<Lesson>();

        List<Lesson> lessons_all =((Timetable)context).lessons;
        for(Lesson lesson : lessons_all){
            if(lesson == null){
                //if this happens, means that GroupsMap is not for this ClassData
                //i.e. there is no lesson with given hash code
                Log.d("Another ", "Null lesson");
            }
            else if(HelperFunctions.ThisDay(lesson, mContent)){
                lessons_today.add(lesson);
            }
        }
        if(ListAdapter != null) {
            ListAdapter.clear();
            ListAdapter.addAll(lessons_today);
        }
        }

    //FragmentCommunicator interface implementation
    @Override
    public void passDataToFragment(String someValue){
        activityAssignedValue = someValue;
        send_lesson_count();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

         setRetainInstance(true);

        return inflater.inflate(R.layout.fragment_timetable, container, false);

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STRING_VALUE,activityAssignedValue);
    }
}