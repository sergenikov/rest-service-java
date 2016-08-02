package com.sergey.restclinic.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Appointment {
    private String _id;
    private Date date;
    private Duration duration;
    private Doctor doctor;
    private Patient patient;
    
    public Appointment() {}
    
    // with id
    public Appointment(String id, Date date, Doctor doctor, Patient patient, Duration duration) {
        this._id = id;
        this.date = date;
        this.doctor = doctor;
        this.patient = patient;
        this.duration = duration;
    }
    
    // without id
    public Appointment(Doctor doctor, Patient patient, Date date, Duration duration) {
        this.date = date;
        this.doctor = doctor;
        this.patient = patient;
        this.duration = duration;
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

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

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

    /**
     * @return the duration
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(Duration duration) {
        this.duration = duration;
    }
}
