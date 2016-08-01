package com.sergey.restclinic.resources;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.sergey.restclinic.database.DatabaseConnection;
import com.sergey.restclinic.models.Doctor;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
//                System.out.println(document);
                String name = document.getString("name");
                String id = document.get("_id").toString();
                Doctor d = new Doctor(name, id);
                doctors.add(d);  
            }
        });
//        doctors.add(new Doctor("tomas", "16253461523"));
//        doctors.add(new Doctor("max", "1398472983475"));
        return doctors;
    }
    
    @GET
    @Path("getphil")
    @Produces(MediaType.APPLICATION_XML)
    public Doctor getDoctor() {
        Doctor d = new Doctor("phil", "98346984756");
        return d;
    }
    
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