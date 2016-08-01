package com.sergey.restclinic.database;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 *
 * @author SineCombo
 */
public class DatabaseConnection {
    
    public static DatabaseConnection db = null;
    public MongoDatabase mongodb;
    
    /*
    Singleton constructor for the database connection
    */
    private DatabaseConnection() {
        String dbName = "clinic";
        MongoClient mongoClient = new MongoClient();
        mongodb = mongoClient.getDatabase(dbName);
        
    }
    
    public static synchronized DatabaseConnection getInstance() {
        if (db == null) {
            db = new DatabaseConnection();
        }
        return db;
    }
}
