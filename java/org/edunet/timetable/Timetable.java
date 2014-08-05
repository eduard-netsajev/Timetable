package org.edunet.timetable;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Timetable extends FragmentActivity implements ActivityCommunicator{

    public void passDataToActivity(String someValue){
        TextView today = (TextView) findViewById(R.id.today);
        if(someValue.substring(0,5).equals("TEXT:")){
            today.setText(someValue.substring(5));
        }
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

    // URL to get JSON files
    private final String[] TTurl = {"http://money.vnet.ee/ClassData.json",
                                    "http://money.vnet.ee/GroupsMap.json",
                                    "http://money.vnet.ee/version.json"};

    private final String[] filenames = {"ClassData.json",
                                        "GroupsMap.json",
                                        "version.json"};

    HashMap<String, HashMap<String, List<String>>> spinners_members = new HashMap<String, HashMap<String, List<String>>>();
    Map<String, List<String>> groups_map = null;
    HashMap<String, Lesson> TimeTable;

    List<String> ClassHashes = new ArrayList<String>();
    List<String> groups = new ArrayList<String>();

    List<Lesson> lessons = new ArrayList<Lesson>();

    TextView your_group;
    TableRow top_row;
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
        top_row = (TableRow) findViewById(R.id.top_row);
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

        top_row.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                load_group();
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

            //TODO FIRSTRUN
            sPref = getPreferences(MODE_PRIVATE);
            if (sPref.getBoolean("firstrun", true)) {
                copy_assets_to_internal();
            }
            final int update_need = check_version();
            TimeTable = load_timetable();
            load_groups();


            ///////////

            // Making a request to groups_map_url and getting response
            //   String jsonStr = sh.makeServiceCall(groups_map_url, ServiceHandler.GET);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(update_need == 2){
                        pop_update_question();
                    }

                    //stuff that updates ui
                    init_spinners();

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

                sPref = getPreferences(MODE_PRIVATE);
                if (sPref.getBoolean("firstrun", true)) {
                    ProgramGuide();
                    Editor ed = sPref.edit();
                    ed.putBoolean("firstrun", false).apply();
                }

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
            case R.id.update:
                update_files();
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

            saved_group = get_saved_group();

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
        saved_group = get_saved_group();
        if(saved_group.length() > 1){
            int fac_pos = facultyAdapt.getPosition(saved_group.substring(0, 1));
            spinnerFaculties.setSelection(fac_pos);

            UpdateSpinners(1);

            int prog_pos = programAdapt.getPosition(saved_group.substring(1,4));
            spinnerPrograms.setSelection(prog_pos);

            UpdateSpinners(2);

            int gr_pos = group_idAdapt.getPosition(saved_group.substring(4,6));
            spinnerGroupsIDs.setSelection(gr_pos);
        }
        else{
            Toast.makeText(getApplicationContext(), getString(R.string.No_group), Toast.LENGTH_SHORT).show();
        }
    }

    public void updateLockView() {
        if (get_saved_group().equals(selectedGroup)) {
            Lock.setImageResource(R.drawable.lockclosed);
        } else {
            Lock.setImageResource(R.drawable.lockopened);
        }
    }

    private String get_saved_group(){
        sPref = getPreferences(MODE_PRIVATE);
        return sPref.getString("saved_group", "");
    }

    public String loadJSONFromInternal(String filename) {
        String json = null;
        try {
            InputStream is = openFileInput(filename);

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

        List<String> faculties = new ArrayList<String>();

        for (String faculty: spinners_members.keySet()) {
            faculties.add(faculty);
        }

        Collections.sort(faculties);

        for(String facul : faculties){
            facultyAdapt.add(facul);
        }
    }

    static public void copy_asset(Context context, String file){
        InputStream in = null;
        OutputStream fout = null;
        int count = 0;

        try
        {
            in = context.getAssets().open(file);
            fout = new FileOutputStream(new File(context.getFilesDir() + "/" + file));

            byte data[] = new byte[1024];
            while ((count = in.read(data, 0, 1024)) != -1)
            {
                fout.write(data, 0, count);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (in != null)
            {
                try {
                    in.close();
                } catch (IOException e)
                {
                 Log.d("Exception > ", "happened");
                }
            }
            if (fout != null)
            {
                try {
                    fout.close();
                } catch (IOException e) {
                    Log.d("Exception > ", "happened");
                }
            }
        }
    }

    private void ProgramGuide() {

        final Dialog dialog = new Dialog(this, R.style.TTUthemeDialog);

        dialog.setContentView(R.layout.guide_layout);
        dialog.setTitle(R.string.guide);

        // set the custom dialog components
        TextView text = (TextView) dialog.findViewById(R.id.textViewTop);
        text.setText(R.string.guide1);
        ImageView image = (ImageView) dialog.findViewById(R.id.tutorial_imageLeft);
        image.setImageResource(android.R.drawable.ic_menu_help);
        image.setPadding(125, 40, 0, 40);
        TextView textOR = (TextView) dialog.findViewById(R.id.dialogTextOR);
        textOR.setVisibility(View.VISIBLE);
        textOR.setText(R.string.help);
        textOR.setPadding(0, 40, 0, 40);
        textOR.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        TextView text2 = (TextView) dialog.findViewById(R.id.textViewBot);
        text2.setText(R.string.guide2);

        View dialogview = (View) dialog.findViewById(R.id.dialogView);

        dialogview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            int HelperStage = 0;
            TextView text = (TextView) dialog.findViewById(R.id.textViewTop);
            ImageView image = (ImageView) dialog.findViewById(R.id.tutorial_imageLeft);
            ImageView image2 = (ImageView) dialog.findViewById(R.id.tutorial_imageRight);
            TextView textOR = (TextView) dialog.findViewById(R.id.dialogTextOR);
            TextView text2 = (TextView) dialog.findViewById(R.id.textViewBot);

            @Override
            public void onDismiss(DialogInterface dialog1) {

                switch (HelperStage) {

                    case 0:
                        dialog.setTitle(R.string.Days);
                        text.setText(R.string.guide3);
                        text2.setText(R.string.guide4);
                        textOR.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                        textOR.setText(R.string.or);
                        image.setPadding(0, 0, 0, 0);
                        textOR.setPadding(0, 0, 0, 0);
                        image.setImageResource(R.drawable.swipeleft);
                        image2.setImageResource(R.drawable.swiperight);
                        dialog.show();
                        HelperStage++;
                        break;
                    case 1:
                        dialog.setTitle(R.string.Weeks);
                        text.setText(R.string.guide5);
                        text2.setText(R.string.guide6);
                        image.setImageResource(R.drawable.swipeup);
                        image2.setImageResource(R.drawable.swipedown);
                        dialog.show();
                        HelperStage++;
                        break;
                    case 2:
                        dialog.setTitle(R.string.Lock);
                        text.setText(R.string.guide7);
                        text2.setText(R.string.guide8);
                        image.setImageResource(R.anim.lock);
                        image2.setVisibility(View.GONE);
                        textOR.setVisibility(View.GONE);

                        AnimationDrawable lockAnimation = (AnimationDrawable) image.getDrawable();
                        lockAnimation.start();
                        dialog.show();
                        HelperStage++;
                        break;
                    case 3:
                        dialog.setTitle(R.string.Cuztomization);
                        text.setText(R.string.guide9);
                        text2.setText(R.string.guide10);
                        image.setImageResource(R.drawable.pressandhold);
                        dialog.show();
                        HelperStage++;
                        break;
                    case 4:
                        dialog.setTitle(R.string.EditMenu);
                        text.setText(R.string.guide11);
                        text2.setVisibility(View.GONE);
                        image.setImageResource(R.drawable.actionbar);
                        dialog.show();
                        HelperStage++;
                        break;
                    case 5:
                        dialog.setTitle(R.string.preferences);
                        text.setText(R.string.guide12);
                        text2.setText(R.string.guide13);
                        textOR.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                        text2.setVisibility(View.VISIBLE);
                        image.setImageResource(android.R.drawable.ic_menu_preferences);
                        image.setPadding(125, 40, 0, 40);
                        textOR.setVisibility(View.VISIBLE);
                        textOR.setText(R.string.preferences);
                        textOR.setPadding(0, 40, 0, 40);


                        image.setOnClickListener(new View.OnClickListener() {
                            // @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                //   Intent pref = new Intent("edunet.virukol.tunniplaan.PREFS");
                                //   startActivity(pref);
                            }
                        });
                        textOR.setOnClickListener(new View.OnClickListener() {
                            // @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                //    Intent pref = new Intent("edunet.virukol.tunniplaan.PREFS");
                                //  startActivity(pref);
                            }
                        });


                        dialog.show();
                        HelperStage++;

                    default:

                        break;
                }


            }
        });

    }

    private HashMap<String, Lesson> load_timetable(){
        HashMap<String, Lesson> temp_TimeTable = new HashMap<String, Lesson>();
        try{
            JSONObject json_timetable = new JSONObject(loadJSONFromInternal(filenames[0]));
            //JSONObject json_timetable = new JSONObject(jsonStr2);
            @SuppressWarnings("unchecked")
            Iterator<String> nameItr = json_timetable.keys();

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

                temp_TimeTable.put(hash, tempLesson);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return temp_TimeTable;
    }

    private void load_groups(){
        String jsonStr = loadJSONFromInternal(filenames[1]);
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
    }

    private void copy_assets_to_internal(){
        Context context = getApplicationContext();
        for(String filename : filenames){
            copy_asset(context, filename);
        }
    }

    private int check_version(){
        //the return value symbolizes the need to update ClassData
        //and GroupsMap file. Higher value = higher need to update

        int server_version = 0;
        int local_version = 0;
        boolean user_group_updated = false;

        // Creating service handler class instance
        ServiceHandler sh = new ServiceHandler();

        // Making a request to TT_version_url and getting response
        String jsonStr = sh.makeServiceCall(TTurl[2], ServiceHandler.GET);
        if(jsonStr != null){
            try {
                JSONObject version_json = new JSONObject(jsonStr);
                JSONObject internal_json = new JSONObject(loadJSONFromInternal(filenames[2]));
                server_version = version_json.getInt("version");
                local_version = internal_json.getInt("version");
                JSONArray updates = version_json.getJSONArray("changedGroups");
                for (int i=0; i<updates.length(); i++) {
                    JSONArray groups = updates.getJSONArray(i);
                    for (int j=0; j<groups.length(); j++) {
                        if(groups.getString(j).equals(get_saved_group())){
                            user_group_updated = true;
                        }
                    }
                }
            } catch (JSONException e){
                return 1; //just to update local files
            }
        }
        if(server_version == local_version){
            return 0;
        }
        else{
            if(user_group_updated){
                return 2;
            }
            else {
                return 1;
            }
        }
    }

    private void pop_update_question(){
        Toast.makeText(getApplicationContext(), "Your Timetable data is outdated", Toast.LENGTH_LONG).show();
    }

    private void update_files(){
        int outdated = check_version();
        if(outdated > 0){
        for(int i = 0; i < filenames.length; i++){
            downloadFile(TTurl[i], filenames[i]);
        }
        }
        else{
            Toast.makeText(getApplicationContext(), "Your Timetable data is up to date", Toast.LENGTH_LONG).show();
        }
    }

    public boolean downloadFile(final String path, final String filename)
    {
        try
        {
            URL url = new URL(path);

            Context context = getApplicationContext();

            URLConnection ucon = url.openConnection();
            ucon.setReadTimeout(5000);
            ucon.setConnectTimeout(10000);

            InputStream is = ucon.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

            File file = new File(context.getFilesDir() + "/" + filename);

            if (file.exists())
            {
                file.delete();
            }
            file.createNewFile();

            FileOutputStream outStream = new FileOutputStream(file);
            byte[] buff = new byte[5 * 1024];

            int len;
            while ((len = inStream.read(buff)) != -1)
            {
                outStream.write(buff, 0, len);
            }

            outStream.flush();
            outStream.close();
            inStream.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}

