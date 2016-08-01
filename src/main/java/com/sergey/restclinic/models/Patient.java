package com.sergey.restclinic.models;

public class Patient {
    String name;
    String id;
    
    // no-op constructor for safety
    public Patient() {}
    
    public Patient(String name, String id) {
        this.name = name;
        this.id = id;
    }
}
