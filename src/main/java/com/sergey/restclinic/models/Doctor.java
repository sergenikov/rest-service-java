package com.sergey.restclinic.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Doctor {
    
    String _id;
    String name;
    String phone;
    
    // no-op constructor for safety
    public Doctor() {}
    
    public Doctor(String name, String id) {
        this.name = name;
        this._id = id;
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
}