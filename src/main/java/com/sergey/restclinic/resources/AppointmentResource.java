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
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.bson.Document;
import javax.ws.rs.core.Response;

@Path("/appointment") 
public class AppointmentResource {
    
    final String CURRENT_COLLECTION = "Appointment";
    final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    /**
     * Return all appointments that match all of these requirements
     * @param param_doc_name doctor name
     * @param param_pat_name patient name
     * @param param_date     date and time in local time
     * @return 
     */
    @GET
    @Path("get")
    @Produces(MediaType.APPLICATION_XML)
    public List<Appointment> getAppointment(
            @QueryParam("doc_name") String param_doc_name,
            @QueryParam("pat_name") String param_pat_name,
            @QueryParam("date") String param_date) {
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> docCollection = db.mongodb.getCollection(CURRENT_COLLECTION);
        
        // lookup in db for ids
        Doctor d = lookupDoctor(param_doc_name);
        Patient p = lookupPatient(param_pat_name);
        
        if (d == null) {
            documentNotFoundError(d.getName());
        } else if (p == null) {
            documentNotFoundError(p.getName());
        }
        
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        // get date from request
        Date date = null;
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            date = format.parse("2016-05-12T23:00:00Z");
        } catch (ParseException ex) {
            // TODO needs some error handling for server not to freak out
            Logger.getLogger(AppointmentResource.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("in date error");
            return null;
        }
        
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("doc_id", d.getId());
        searchQuery.put("pat_id", p.getId());
        searchQuery.put("datetime", date);
        FindIterable<Document> iterable = docCollection.find(searchQuery);
        
        Appointment appointment = null;
        
        if (iterable.first() == null) {
            return null;
        }
        
        List<Appointment> appts = new ArrayList<>();
        
        // By the time exe gets here we 
        // (1) found both patient and doctor
        // (2) know we found appointment - date OK as well
        for (Document document : iterable) {
            appointment = new Appointment(
                    d,
                    p,
                    param_date,
                    document.getLong("duration")
            );
            appts.add(appointment);
        }
        
        System.out.println("before returning");
        return appts;
    }
    
    @POST
    @Path("add")
    @Consumes(MediaType.APPLICATION_XML)
    public Response createAppointment(Appointment apt) {
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> docCollection = db.mongodb.getCollection(CURRENT_COLLECTION);
        
        // Get dates
        DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);

        
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
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
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
    
    /**
     * Create generic Response for when document is not found in the db.
     * @param docName
     * @return 
     */
    public Response documentNotFoundError(String docName) {
        // Error if can't find either patient or doctor
        return Response.status(400)
                .entity("Appointment creating failed. Can't find patient " 
                        + docName)
                .build();
    }
}


