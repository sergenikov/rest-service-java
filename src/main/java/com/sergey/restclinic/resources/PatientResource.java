package com.sergey.restclinic.resources;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.sergey.restclinic.database.DatabaseConnection;
import com.sergey.restclinic.models.Doctor;
import com.sergey.restclinic.models.Patient;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.bson.Document;
import org.bson.types.ObjectId;

/*
NOTE:
I recognize that Patient and Doctor are very similar in basic REST operations.
Ideally they should have the same superclass with these methods implemented
and different methods in these classes.
*/

@Path("/patients")
public class PatientResource {
    
    final String COLLECTION_NAME = "Patient";
        
    @GET
    @Path("getall")
    @Produces(MediaType.APPLICATION_XML)
    public List<Patient> getPatients() {
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        
        final List<Patient> patients = new ArrayList<>();
        
        FindIterable<Document> iterable = db.mongodb.getCollection(COLLECTION_NAME).find();
        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                String name = document.getString("name");
                String id = document.get("_id").toString();
                Patient d = new Patient(name, id);
                patients.add(d);
            }
        });
        return patients;
    }
    
    @GET
    @Path("getbyname")
    @Produces(MediaType.APPLICATION_XML)
    public List<Patient> getPatientByName(@QueryParam("name") String name) {
        
        final List<Patient> patients = new ArrayList<>();
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> docCollection = db.mongodb.getCollection(COLLECTION_NAME);
        
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("name", name);
        FindIterable<Document> iterable = docCollection.find(searchQuery);
        
        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                String name = document.getString("name");
                String id = document.get("_id").toString();
                Patient d = new Patient(name, id);
                patients.add(d);
            }
        });

        return patients;
    }
    
    /**
     * Adding duplicates is allowed.
     * @param name name of the doctor to be added
     * @return Response with http codes and info message
     */
    @POST
    @Path("add")
    @Consumes(MediaType.APPLICATION_XML)
    public Response addPatient(Patient patient) {
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> docCollection = db.mongodb.getCollection(COLLECTION_NAME);
        
        // TODO when done validate fields/params received
        
        System.out.println("DoctorResource.addPatient() adding patient " + patient.getName());
        Document newDoc = new Document();
        newDoc.append("name", patient.getName());
        try {
            docCollection.insertOne(newDoc);
        } catch (MongoWriteConcernException e) {
            System.err.println(e);
            return Response.status(500)
                    .entity("Adding patient failed; name=" + patient.getName() + "; error " + e)
                    .build();
        } catch (MongoException e) {
            System.err.println(e);
            return Response.status(500)
                    .entity("Adding patient failed; name=" + patient.getName() + "; error " + e)
                    .build();
        }
        
        return Response.status(200).entity("Success. Added patient " + patient.getName()).build();
    }
    
    @DELETE
    @Path("remove/{name}")
    public Response removePatient(@PathParam("name") String name) {
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> docCollection = db.mongodb.getCollection(COLLECTION_NAME);
        
        System.out.println("DoctorResource.removePatient(): removing patient " + name);
        
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("name", name);
        
        try {
            docCollection.deleteOne(searchQuery);
        } catch (MongoWriteConcernException e) {
            System.err.println(e);
            return Response.status(500)
                    .entity("Removing patient failed; name=" + name + "; error " + e)
                    .build();
        } catch (MongoException e) {
            System.err.println(e);
            return Response.status(500)
                    .entity("Removing patient failed; name=" + name + "; error " + e)
                    .build();
        }
        // TODO failes to say that it did not remove duplicate records
        // Add check for duplicates on POST
        return Response.status(200).entity("Success. Removed patient " + name).build();
    }

    // A test call to see if api is working
    @GET
    @Path("getpat")
    @Produces(MediaType.APPLICATION_XML)
    public Patient getPat() {
        Patient p = new Patient("test_pat", "98346984756");
        return p;
    }
    
    // A test call to see if api is working and db link is good
    @GET
    @Path("getpatientsplain")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPatientsPlain() {
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        
        final ArrayList<String> patients = new ArrayList<String>();
        
        FindIterable<Document> iterable = db.mongodb.getCollection(COLLECTION_NAME).find();
        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                System.out.println(document);
                patients.add(document.toJson());
            }
        });
        return patients.toString();
    }
    
}