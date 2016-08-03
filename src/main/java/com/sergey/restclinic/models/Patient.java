package com.sergey.restclinic.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Patient {
    private String name;
    private String id;
    
    // no-op constructor for safety
    public Patient() {}
    
    public Patient(String name) {
        this.name = name;
    }
    
    public Patient(String name, String id) {
        this.name = name;
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
}
