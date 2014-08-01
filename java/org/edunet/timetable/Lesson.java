package org.edunet.timetable;

import java.util.List;

/**
 * Created by Netšajev on 31/07/2014.
 * Lesson class to hold data of single instance of lesson
 */
class Lesson {

    private String code;
    private String comments;
    private int day;
    private int weeks;
    private String start_time;
    private String end_time;
    private String name;
    private String room;
    private List<String> groups;
    private List<String> teacher;
    private String type;
    private String interval;

    public Lesson(String code, String comments, int day, int weeks, String start_time,
                  String end_time, String name, String room, List<String> groups,
                  List<String> teacher, String type, String interval){

        this.code = code;
        this.comments = comments;
        this.day = day;
        this.weeks = weeks;
        this.start_time = start_time;
        this.end_time = end_time;
        this.name = name;
        this.room = room;
        this.groups = groups;
        this.teacher = teacher;
        this.type = type;
        this.interval = interval;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getWeeks() {
        return weeks;
    }

    public void setWeeks(int weeks) {
        this.weeks = weeks;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getTeacher() {
        return teacher;
    }

    public void setTeacher(List<String> teacher) {
        this.teacher = teacher;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }
}