package org.edunet.timetable;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.style.UpdateAppearance;
import android.text.style.UpdateLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.edunet.timetable.Lesson;

public class Timetable extends FragmentActivity implements ActivityCommunicator{

    public void passDataToActivity(String someValue){
        TextView today = (TextView) findViewById(R.id.today);
        if(someValue.substring(0,5).equals("TEXT:")){
            today.setText(someValue.substring(5));
        }

        Log.d("passDataToActivity -> ", someValue);
    }

    ///////////////
    //interface through which communication is made to fragment
    public FragmentCommunicator fragmentCommunicator;
   // private CustomAdapter customAdapter;
    public static ArrayList<Fragment> listFragments;
    //////////////
    TTableFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;

    Spinner spinnerFaculties;
    Spinner spinnerPrograms;
    Spinner spinnerGroupsIDs;

    ArrayAdapter<String> group_idAdapt;
    ArrayAdapter<String> programAdapt;
    ArrayAdapter<String> facultyAdapt;

    String selectedGroup = "";
    String saved_group = "";

    Typeface tf;
    SharedPreferences sPref;
    ImageButton Lock;
    //////////////////////////

    private ProgressDialog pDialog;

    // URL to get GroupsMap JSON
    private final static String groups_map_url = "http://money.vnet.ee/GroupsMap.json";
    private final static String class_data_url = "http://money.vnet.ee/ClassData.json";

    HashMap<String, HashMap<String, List<String>>> spinners_members = new HashMap<String, HashMap<String, List<String>>>();
    Map<String, List<String>> groups_map = null;
    HashMap<String, Lesson> TimeTable;

    List<String> ClassHashes = new ArrayList<String>();
    List<String> groups = new ArrayList<String>();

    List<Lesson> lessons = new ArrayList<Lesson>();

    TextView your_group;
    //////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable);

        tf = Typeface.createFromAsset(getAssets(),"Andada-Regular.ttf");
        mAdapter = new TTableFragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);

        Lock = (ImageButton) findViewById(R.id.lock);
        your_group = (TextView) findViewById(R.id.your_group);

        // Starting program execution
        //////////////////////////////////

        Lock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                save_group();
            }
        });

        new GetGroupsMap().execute();

        mPager.setCurrentItem(HelperFunctions.getTodayPageNumber());

        //We set this on the indicator, NOT the pager
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //TODO
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }
    /**
         * Async task class to get json by making HTTP call
         * */
        public class GetGroupsMap extends AsyncTask<Void, Void, Void> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // Showing progress dialog

                pDialog = new ProgressDialog(Timetable.this);
                pDialog.setMessage("Please wait...");
                pDialog.setCancelable(false);
                pDialog.show();

            }

            @Override
            protected Void doInBackground(Void... arg0) {

                // Creating service handler class instance
                ServiceHandler sh = new ServiceHandler();

                // Making a request to class_data_url and getting response
                //String jsonStr2 = sh.makeServiceCall(class_data_url, ServiceHandler.GET);

                try{
                    JSONObject json_timetable = new JSONObject(loadJSONFromAsset("ClassData.json"));
                    //JSONObject json_timetable = new JSONObject(jsonStr2);
                    @SuppressWarnings("unchecked")
                    Iterator<String> nameItr = json_timetable.keys();
                    TimeTable = new HashMap<String, Lesson>();

                    String code, comments, start_time, end_time, name, room, type, interval;
                    int day, weeks;
                    List<String> groups_list, teacher_list;

                    while(nameItr.hasNext()){
                        groups_list = new ArrayList<String>();
                        teacher_list = new ArrayList<String>();

                        String hash = nameItr.next();
                        JSONObject lesson_json = json_timetable.getJSONObject(hash);

                        code = lesson_json.getString("ainekood");
                        comments = lesson_json.getString("comments");
                        start_time = lesson_json.getString("start_time");
                        end_time = lesson_json.getString("end_time");
                        name = lesson_json.getString("name");
                        room = lesson_json.getString("room");
                        type = lesson_json.getString("type");
                        interval = lesson_json.getString("lasts");

                        day = lesson_json.getInt("day");
                        weeks = lesson_json.getInt("weeks");

                        JSONArray jsonArray = lesson_json.getJSONArray("groups");
                        if (jsonArray != null) {
                            int len = jsonArray.length();
                            for (int i=0;i<len;i++){
                                groups_list.add(jsonArray.get(i).toString());
                            }
                        }
                        jsonArray = lesson_json.getJSONArray("teacher");
                        if (jsonArray != null) {
                            int len = jsonArray.length();
                            for (int i=0;i<len;i++){
                                teacher_list.add(jsonArray.get(i).toString());
                            }
                        }

                        Lesson tempLesson = new Lesson(code, comments, day, weeks, start_time,
                                end_time, name, room, groups_list, teacher_list, type, interval);

                        TimeTable.put(hash, tempLesson);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ///////////

                // Making a request to groups_map_url and getting response
                String jsonStr = sh.makeServiceCall(groups_map_url, ServiceHandler.GET);

                if (jsonStr != null) {
                    try {
                        //Converting hashmap GroupsMap.json into Java object
                        JSONObject GroupsMap = new JSONObject(jsonStr);
                        @SuppressWarnings("unchecked")
                        Iterator<String> nameItr = GroupsMap.keys();
                        groups_map = new HashMap<String, List<String>>();
                        while(nameItr.hasNext()) {
                            String group = nameItr.next();
                            groups.add(group);

                            List<String> hashed_classes = new ArrayList<String>();
                            JSONArray jsonArray = GroupsMap.getJSONArray(group);

                            for (int i=0; i<jsonArray.length(); i++) {
                                hashed_classes.add(jsonArray.getString(i));
                            }
                            groups_map.put(group, hashed_classes);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("ServiceHandler", "Couldn't get any data from the groups_map_url");
                }

                //chunk groups into hashmap of faculties, programs and groups in 1 iteration
                for (String group : groups){
                    String faculty_name = group.substring(0, 1);
                    String group_name = group.substring(1, 4);
                    String group_id = group.substring(4);

                    if (spinners_members.get(faculty_name) != null) {
                        if (spinners_members.get(faculty_name).get(group_name) != null) {
                            spinners_members.get(faculty_name).get(group_name).add(group_id);
                        }else {
                            // No such group key
                            List<String> tempList = new ArrayList<String>();
                            tempList.add(group_id);
                            spinners_members.get(faculty_name).put(group_name, tempList);
                        }
                    }else {
                        // No such faculty key, meaning no such group key as well
                        List<String> tempList = new ArrayList<String>();
                        tempList.add(group_id);
                        HashMap<String, List<String>> tempMap = new HashMap<String, List<String>>();
                        tempMap.put(group_name, tempList);
                        spinners_members.put(faculty_name, tempMap);
                    }

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                //stuff that updates ui
                        init_spinners();

                        List<String> faculties = new ArrayList<String>();

                        for (String faculty: spinners_members.keySet()) {
                            faculties.add(faculty);
                        }

                        Collections.sort(faculties);

                        for(String facul : faculties){
                            facultyAdapt.add(facul);
                        }
                    }

                });
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                /**
                 * Updating parsed JSON data into spinners
                 * */

                // Dismiss the progress dialog
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                    load_group();
                    your_group.setText(selectedGroup);

                }
            }

        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.random:
                int page = 4;
                mPager.setCurrentItem(page);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //FUNCTIONS
    public void UpdateSpinners(int spinner) {

        String chosenFaculty = (String) spinnerFaculties.getSelectedItem();

        if (spinner == 1) {
            populatePrograms(chosenFaculty);
        }
        else if (spinner == 2){
            String chosenProgram = (String) spinnerPrograms.getSelectedItem();
            populateGroups(chosenFaculty, chosenProgram);
        }
    }

    public void populatePrograms(String chosenFaculty){


        if(spinnerPrograms.getSelectedItemPosition() > spinnerPrograms.getCount()){
            spinnerPrograms.setSelection(0);
        }
        programAdapt.clear();

        ArrayList<String> programList = new ArrayList<String>();
        for (Map.Entry<String, List<String>> map : spinners_members.get(chosenFaculty).entrySet()) {
            programList.add(map.getKey());
        }
        Collections.sort(programList);
        for (String program : programList) {
            programAdapt.add(program);
        }

        if(spinnerPrograms.getSelectedItemPosition() > spinnerPrograms.getCount()){
            spinnerPrograms.setSelection(0);
        }
        String chosenProgram = (String) spinnerPrograms.getSelectedItem();

        if (chosenProgram != null) {
            populateGroups(chosenFaculty, chosenProgram);
        }
    }

    public void populateGroups(String chosenFaculty, String chosenProgram) {
        group_idAdapt.clear();

        List<String> groupList = new ArrayList<String>();
        for (String group : spinners_members.get(chosenFaculty).get(chosenProgram)) {
            groupList.add(group);
        }

        Collections.sort(groupList);

        if(saved_group.length() > 1){

            sPref = getPreferences(MODE_PRIVATE);
            saved_group = sPref.getString("saved_group", "");

            String my_program = saved_group.substring(1,4);

            if(!chosenProgram.equals(my_program)){
                String course = saved_group.substring(4, 5);
                for (int i = 0; i < groupList.size(); i++) {
                    String group = groupList.get(i);
                    group_idAdapt.add(group);
                    if(group.substring(0, 1).equals(course)){
                        if(course.length() < 3) {
                            spinnerGroupsIDs.setSelection(i);
                            course = "done";
                        }
                    }
                }
            }
            else {
                String course = saved_group.substring(4, 6);
                for (int i = 0; i < groupList.size(); i++) {
                    String group = groupList.get(i);
                    group_idAdapt.add(group);
                    if(group.substring(0, 2).equals(course)){
                        if(course.length() < 3) {
                            spinnerGroupsIDs.setSelection(i);
                            course = "done";
                        }
                    }
                }
            }
        }
        else {
            Log.d("Set group selection to ", "fds" );
            for (String group : groupList) {
                group_idAdapt.add(group);
            }
        }

        if(spinnerGroupsIDs.getSelectedItemPosition() >= spinnerGroupsIDs.getCount()){
            spinnerGroupsIDs.setSelection(0);
        }

        UpdateHashTable();
    }

    public void UpdateHashTable(){

        String chosenProgram = (String) spinnerPrograms.getSelectedItem();
        String chosenGroup = (String) spinnerGroupsIDs.getSelectedItem();
        String chosenFaculty = (String) spinnerFaculties.getSelectedItem();

        if(chosenGroup != null && chosenProgram != null) {
            String total_group = chosenFaculty + chosenProgram + chosenGroup;
            if(!total_group.equals(selectedGroup)){
                selectedGroup = total_group;
                ClassHashes = new ArrayList<String>(groups_map.get(total_group));
                UpdateList();
                updateLockView();
            }
        }
    }

    public void UpdateList(){
        lessons = new ArrayList<Lesson>();
        if(ClassHashes != null) {
            for (String hash : ClassHashes) {
                lessons.add(TimeTable.get(hash));
            }
            //TODO refresh pages
            if(fragmentCommunicator != null) {
               mAdapter.notifyDataSetChanged();
               fragmentCommunicator.passDataToFragment("Hell yeah, update dem pages");
            }
        }
    }

    public void save_group(){
        sPref = getPreferences(MODE_PRIVATE);
        Editor ed = sPref.edit();
        ed.putString("saved_group", selectedGroup);
        ed.apply();

        your_group.setText(selectedGroup);

        updateLockView();
    }

    public void load_group(){
        sPref = getPreferences(MODE_PRIVATE);
        saved_group = sPref.getString("saved_group", "");
        if(saved_group.length() > 1){
            Log.d("Loading ", saved_group);
            int fac_pos = facultyAdapt.getPosition(saved_group.substring(0, 1));
            spinnerFaculties.setSelection(fac_pos);

            UpdateSpinners(1);

            int prog_pos = programAdapt.getPosition(saved_group.substring(1,4));
            spinnerPrograms.setSelection(prog_pos);

            UpdateSpinners(2);

            int gr_pos = group_idAdapt.getPosition(saved_group.substring(4,6));
            spinnerGroupsIDs.setSelection(gr_pos);
        }
    }

    public void updateLockView() {



        sPref = getPreferences(MODE_PRIVATE);
        if (sPref.getString("saved_group", "").equals(selectedGroup)) {
            Lock.setImageResource(R.drawable.lockclosed);
        } else {
            Lock.setImageResource(R.drawable.lockopened);
        }
    }

    public String loadJSONFromAsset(String filename) {
        String json = null;
        try {
            InputStream is = getAssets().open(filename);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void init_spinners() {

        spinnerFaculties = (Spinner) findViewById(R.id.faculty_spinner);
        spinnerPrograms = (Spinner) findViewById(R.id.groups_spinner);
        spinnerGroupsIDs = (Spinner) findViewById(R.id.groupsID_spinner);

        group_idAdapt = new ArrayAdapter<String>(Timetable.this, R.layout.custom_spinner_list);
        programAdapt = new ArrayAdapter<String>(Timetable.this, R.layout.custom_spinner_list);
        facultyAdapt = new ArrayAdapter<String>(Timetable.this, R.layout.custom_spinner_list);

        group_idAdapt.setDropDownViewResource(R.layout.customer_spinner);
        programAdapt.setDropDownViewResource(R.layout.customer_spinner);
        facultyAdapt.setDropDownViewResource(R.layout.customer_spinner);

        spinnerPrograms.setAdapter(programAdapt);
        spinnerGroupsIDs.setAdapter(group_idAdapt);
        spinnerFaculties.setAdapter(facultyAdapt);

        spinnerFaculties.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                UpdateSpinners(1);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerPrograms.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                UpdateSpinners(2);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerGroupsIDs.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                UpdateHashTable();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

}

