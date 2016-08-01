package com.sergey.restclinic.models;

public class Doctor {
    
    String name;
    String id;
    
    // no-op constructor for safety
    public Doctor() {}
    
    public Doctor(String name, String id) {
        this.name = name;
        this.id = id;
    }
}