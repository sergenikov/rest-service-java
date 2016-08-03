package com.sergey.test;

import com.mongodb.client.MongoCollection;
import com.sergey.restclinic.database.DatabaseConnection;
import org.bson.Document;

public class AppointmentResourceTest {
    
    DatabaseConnection db = DatabaseConnection.getInstance();
    MongoCollection<Document> docCollection = db.mongodb.getCollection("Doctor");
   
    
}
