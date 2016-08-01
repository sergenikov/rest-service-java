package com.sergey.restclinic.resources;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBCursor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.sergey.restclinic.database.DatabaseConnection;
import com.sergey.restclinic.models.Doctor;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.bson.Document;
import org.bson.types.ObjectId;

@Path("/doctors")
public class DoctorResource {
    
    @GET
    @Path("getdoctors")
    @Produces(MediaType.APPLICATION_XML)
    public List<Doctor> getDoctors() {
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        
        final List<Doctor> doctors = new ArrayList<Doctor>();
        
        FindIterable<Document> iterable = db.mongodb.getCollection("Doctor").find();
        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                String name = document.getString("name");
                String id = document.get("_id").toString();
                Doctor d = new Doctor(name, id);
                doctors.add(d);
            }
        });
        return doctors;
    }
    
    @GET
    @Path("getdoctorbyname")
    @Produces(MediaType.APPLICATION_XML)
    public List<Doctor> getDoctorByName2(@QueryParam("name") String name) {
        
        final List<Doctor> doctors = new ArrayList<Doctor>();
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> docCollection = db.mongodb.getCollection("Doctor");
        
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("name", name);
        FindIterable<Document> iterable = docCollection.find(searchQuery);
        
        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                String name = document.getString("name");
                String id = document.get("_id").toString();
                Doctor d = new Doctor(name, id);
                doctors.add(d);
            }
        });

        return doctors;
    }
    
    // A test call to see if api is working
    @GET
    @Path("getphil")
    @Produces(MediaType.APPLICATION_XML)
    public Doctor getDoctor() {
        Doctor d = new Doctor("phil", "98346984756");
        return d;
    }
    
    // A test call to see if api is working and db link is good
    @GET
    @Path("getdoctorsplain")
    @Produces(MediaType.TEXT_PLAIN)
    public String getDoctorsPlain() {
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        
        final ArrayList<String> doctors = new ArrayList<String>();
        
        FindIterable<Document> iterable = db.mongodb.getCollection("Doctor").find();
        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                System.out.println(document);
                doctors.add(document.toJson());
            }
        });
        return doctors.toString();
    }
    
}