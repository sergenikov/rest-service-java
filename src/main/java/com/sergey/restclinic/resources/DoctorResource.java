package com.sergey.restclinic.resources;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.sergey.restclinic.database.DatabaseConnection;
import com.sergey.restclinic.models.Doctor;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.bson.Document;
import org.bson.types.ObjectId;

@Path("/doctors")
public class DoctorResource {
        
    @GET
    @Path("getall")
    @Produces(MediaType.APPLICATION_XML)
    public List<Doctor> getDoctors() {
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        
        final List<Doctor> doctors = new ArrayList<>();
        
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
    @Path("getbyname")
    @Produces(MediaType.APPLICATION_XML)
    public List<Doctor> getDoctorByName2(@QueryParam("name") String name) {
        
        final List<Doctor> doctors = new ArrayList<>();
        
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
    
    /**
     * Adding duplicates is allowed.
     * @param name name of the doctor to be added
     * @return Response with http codes and info message
     */
    @POST
    @Path("add")
    public Response addDoctor(String name) {
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> docCollection = db.mongodb.getCollection("Doctor");
        
        // TODO when done validate fields/params received
        
        System.out.println("DoctorResource.addDoctor() adding doctor " + name);
        Document newDoc = new Document();
        newDoc.append("name", name);
        try {
            docCollection.insertOne(newDoc);
        } catch (MongoWriteConcernException e) {
            Response.status(500)
                    .entity("Adding doctor failed; name=" + name + "; error " + e)
                    .build();
            System.err.println(e);
        } catch (MongoException e) {
            Response.status(500)
                    .entity("Adding doctor failed; name=" + name + "; error " + e)
                    .build();
            System.err.println(e);
        }
        
        return Response.status(200).entity("Success. Added doctor " + name).build();
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