package com.sergey.restclinic.resources;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.sergey.restclinic.database.DatabaseConnection;
import java.util.ArrayList;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.bson.Document;

@Path("/doctors")
public class DoctorResource {
    
    @GET
    @Path("getdoctors")
    @Produces(MediaType.TEXT_PLAIN)
    public String getDoctors() {
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        
        final ArrayList<String> rows = new ArrayList<String>();
        FindIterable<Document> iterable = db.mongodb.getCollection("Doctor").find();
        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                System.out.println(document);
                rows.add(document.toJson());
            }
        });
        return rows.toString();
    }
    
}


//@GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/get")
//    public String getPatient() {
//        final ArrayList<String> rows = new ArrayList<String>();
//        FindIterable<Document> iterable = db.getCollection("Patient").find();
//        iterable.forEach(new Block<Document>() {
//            @Override
//            public void apply(final Document document) {
//                System.out.println(document);
//                rows.add(document.toJson());
//            }
//        });
//        return rows.toString();
//    }