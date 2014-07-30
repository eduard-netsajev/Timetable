package org.edunet.timetable;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Timetable extends FragmentActivity {

    TTableFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;

    Spinner spinnerFaculties;
    Spinner spinnerPrograms;
    Spinner spinnerGroupsIDs;

    ArrayAdapter<String> group_idAdapt;
    ArrayAdapter<String> programAdapt;
    ArrayAdapter<String> facultyAdapt;

    //////////////////////////




    private ProgressDialog pDialog;

    // URL to get GroupsMap JSON
    private final static String groups_map_url = "http://money.vnet.ee/GroupsMap.json";

    Map<String, JSONArray> groups_map = null;

    HashMap<String, HashMap<String, List<String>>> spinners_members = new HashMap<String, HashMap<String, List<String>>>();

    List<String> groups = new ArrayList<String>();

    //////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable);

        mAdapter = new TTableFragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);

        // Starting program execution
        //////////////////////////////////
        new GetGroupsMap().execute();

        mPager.setCurrentItem(HelperFunctions.getTodayPageNumber());

        //We set this on the indicator, NOT the pager
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //Toast.makeText(Timetable.this, "Changed to page " + position, Toast.LENGTH_SHORT).show();
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

                // Making a request to groups_map_url and getting response
                String jsonStr = sh.makeServiceCall(groups_map_url, ServiceHandler.GET);

                Log.d("Response: ", "> " + jsonStr);

                if (jsonStr != null) {
                    try {
                        JSONObject GroupsMap = new JSONObject(jsonStr);
                        // Assume you have a Map<String, String> in JSONObject jdata
                        @SuppressWarnings("unchecked")
                        Iterator<String> nameItr = GroupsMap.keys();
                        groups_map = new HashMap<String, JSONArray>();
                        while(nameItr.hasNext()) {
                            String group = nameItr.next();
                            //TODO converge JSONArray into List
                            groups_map.put(group, GroupsMap.getJSONArray(group));
                            groups.add(group);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("ServiceHandler", "Couldn't get any data from the groups_map_url");
                }

                //TODO chunk groups into hashmap of faculties, programs and groups in 1 iteration

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

                        spinnerFaculties = (Spinner) findViewById(R.id.faculty_spinner);
                        spinnerPrograms = (Spinner) findViewById(R.id.groups_spinner);
                        spinnerGroupsIDs = (Spinner) findViewById(R.id.groupsID_spinner);

                        //TODO check if this is needed here
                        //              spinnerPrograms.setPadding(10, 0, 0, 0);
//                spinnerGroupsIDs.setPadding(5, 0, 0, 0);

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
                                // UpdateNumbersSpinner(numbersAdapt, Raspisanie, selectedGroupName);
                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });
                        spinnerPrograms.setOnItemSelectedListener(new OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                // UpdateNumbersSpinner(numbersAdapt, Raspisanie, selectedGroupName);
                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });
                        spinnerGroupsIDs.setOnItemSelectedListener(new OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                // UpdateNumbersSpinner(numbersAdapt, Raspisanie, selectedGroupName);
                            }
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });


                        List<String> faculties = new ArrayList<String>();
                        List<String> groups = new ArrayList<String>();

                        //TODO ADD GROUPS, FACULTIES, IDs
                        for (Map.Entry<String, HashMap<String, List<String>>> faculty: spinners_members.entrySet()) {
                           // facultyAdapt.add(faculty.getKey());
                            faculties.add(faculty.getKey());
                            for (Map.Entry<String, List<String>> program: faculty.getValue().entrySet()) {
                                //programAdapt.add(program.getKey());
                                groups.add(program.getKey());
                            }
                        }


                        Collections.sort(faculties);
                        Collections.sort(groups);

                        for(String facul : faculties){
                            facultyAdapt.add(facul);
                        }
                        for(String group : groups){
                            programAdapt.add(group);
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
                if (pDialog.isShowing())
                    pDialog.dismiss();

            }

        }



    //////////////////////////


    //////////////////////////

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
            /*
            case R.id.add_page:
                if (mAdapter.getCount() < 10) {
                    mAdapter.setCount(mAdapter.getCount() + 1);
                    mIndicator.notifyDataSetChanged();
                }
                return true;

            case R.id.remove_page:
                if (mAdapter.getCount() > 1) {
                    mAdapter.setCount(mAdapter.getCount() - 1);
                    mIndicator.notifyDataSetChanged();
                }
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    //FUNCTIONS


}

