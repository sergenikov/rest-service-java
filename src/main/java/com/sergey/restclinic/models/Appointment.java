package com.sergey.restclinic.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Appointment {
    private String _id;
    private Doctor doctor;
    private Patient patient;
    private String start;
    private String end;
    
//    private String date;
//    private long duration; // duration in minutes; long for Duration object
    
    public Appointment() {}
    
    public Appointment(String start, String end, Doctor doctor, Patient patient) {
        this.start = start;
        this.end = end;
        this.doctor = doctor;
        this.patient = patient;
    }

    /**
     * @return the _id
     */
    public String getId() {
        return _id;
    }

    /**
     * @param _id the _id to set
     */
    public void setId(String _id) {
        this._id = _id;
    }
//
//    /**
//     * @return the date
//     */
//    public String getDate() {
//        return date;
//    }
//
//    /**
//     * @param date the date to set
//     */
//    public void setDate(String date) {
//        this.date = date;
//    }

    /**
     * @return the doctor
     */
    public Doctor getDoctor() {
        return doctor;
    }

    /**
     * @param doctor the doctor to set
     */
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    /**
     * @return the patient
     */
    public Patient getPatient() {
        return patient;
    }

    /**
     * @param patient the patient to set
     */
    public void setPatient(Patient patient) {
        this.patient = patient;
    }

//    /**
//     * @return the duration
//     */
//    public long getDuration() {
//        return duration;
//    }
//
//    /**
//     * @param duration the duration to set
//     */
//    public void setDuration(long duration) {
//        this.duration = duration;
//    }

    /**
     * @return the startDatetime
     */
    public String getStart() {
        return start;
    }

    /**
     * @param startDatetime the startDatetime to set
     */
    public void setStart(String startDatetime) {
        this.start = startDatetime;
    }

    /**
     * @return the endDatetime
     */
    public String getEnd() {
        return end;
    }

    /**
     * @param endDatetime the endDatetime to set
     */
    public void setEnd(String endDatetime) {
        this.end = endDatetime;
    }
}
