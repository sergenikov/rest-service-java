package com.sergey.restclinic.resources;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.sergey.restclinic.database.DatabaseConnection;
import com.sergey.restclinic.models.Appointment;
import com.sergey.restclinic.models.Doctor;
import com.sergey.restclinic.models.Patient;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.bson.Document;
import javax.ws.rs.core.Response;

@Path("/appointment") 
public class AppointmentResource {
    
    final String COLLECTION_NAME = "Appointment";
    final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
    
    
    @POST
    @Path("add")
    @Consumes(MediaType.APPLICATION_XML)
    public Response createAppointment(Appointment apt) {
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> docCollection = db.mongodb.getCollection(COLLECTION_NAME);
        
        // Get dates
        String sampleDate = "2016-09-05 12:30";
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);

        
        System.out.println("AppointmentResource.createAppointment(): " 
                + " doctor "    + apt.getDoctor().getName() 
                + " patient "   + apt.getPatient().getName()
                + " duration "  + apt.getDuration()
                + " date "      + apt.getDate().toString());
        
        // lookup in db for ids
        Doctor d = lookupDoctor(apt.getDoctor().getName());
        Patient p = lookupPatient(apt.getPatient().getName());
        
        // Error if can't find either patient or doctor
        if (d == null) {
            return Response.status(400)
                    .entity("Appointment creating failed. Can't find doctor " 
                            + apt.getDoctor().getName())
                    .build();
        } else if (p == null) {
            return Response.status(400)
                    .entity("Appointment creating failed. Can't find patient " 
                            + apt.getPatient().getName())
                    .build();
        }
        
        // create new appointment object
        Appointment a;
        a = new Appointment(
                d,
                p,
                apt.getDate(),
                apt.getDuration()
        );
        System.out.println("AppointmentResource.createAppointment(): Created appointment" 
                + a.toString());
        
        // get date from request
        Date date = null;
        try {
            date = format.parse(apt.getDate());
        } catch (ParseException ex) {
            // TODO needs some error handling for server not to freak out
            Logger.getLogger(AppointmentResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500)
                    .entity("Appointment creating failed. Error " + ex)
                    .build();
        }
        
        // create document
        Document newDoc = new Document();
        newDoc.append("doc_id", a.getDoctor().getId());
        newDoc.append("pat_id", a.getPatient().getId());
        newDoc.append("datetime", date);
        newDoc.append("duration", a.getDuration());
        
        System.out.println("AppointmentResource.createAppointment(): Created document" 
                + newDoc.toString());

        // insert document into the db
        try {
            docCollection.insertOne(newDoc);
        } catch (MongoException e) {
            System.err.println(e);
            return Response.status(500)
                    .entity("Appointment creating failed. Error " + e)
                    .build();
        }
        
        System.out.println("AppointmentResource.createAppointment(): after inserting" 
                + newDoc.toString());
        
        return Response.status(200).entity("Success. Added appointment.").build();
    }
    
    /**
     * Return doctor by name from the db
     * @param name doctor's name
     * @return Doctor object or null if did not find anything
     * When iterating over doctors found assumes there is only one doctor
     * with that name - return first document found.
     */
    public Doctor lookupDoctor(String name) {
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> docCollection = db.mongodb.getCollection("Doctor");
        
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("name", name);
        FindIterable<Document> iterable = docCollection.find(searchQuery);
        
        Doctor d = null;
        
        if (iterable.first() == null) {
            return null;
        }
        
        for (Document document : iterable) {
            String doc_name = document.getString("name");
            String id = document.get("_id").toString();
            d = new Doctor(doc_name, id);
            return d;
        }

        return d;
    }
    
    /**
     * Return patient by name from db
     * @param name patient's name
     * @return Patient object
     * When iterating over doctors found assumes there is only one patient
     * with that name - return first document found.
     * TODO In reality phone number or some unique id will be used.
     */
    public Patient lookupPatient(String name) {
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> docCollection = db.mongodb.getCollection("Patient");
        
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("name", name);
        FindIterable<Document> iterable = docCollection.find(searchQuery);
        
        Patient p = null;
        
        if (iterable.first() == null) {
            return null;
        }
        
        for (Document document : iterable) {
            String doc_name = document.getString("name");
            String id = document.get("_id").toString();
            p = new Patient(doc_name, id);
            return p;
        }

        return p;
    }
}


