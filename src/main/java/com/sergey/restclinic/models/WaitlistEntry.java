package com.sergey.restclinic.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WaitlistEntry {
    
    Appointment appointment;
    
    public WaitlistEntry() {};
    
    public WaitlistEntry(Appointment apt) {
        this.appointment = apt;
    }
}
