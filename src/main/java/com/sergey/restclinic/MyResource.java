package com.sergey.restclinic;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import java.util.ArrayList;
import static java.util.Arrays.asList;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("patient")
public class MyResource {

    MongoClient mongoClient = new MongoClient();
    MongoDatabase db = mongoClient.getDatabase("clinic");
    
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/helloworld")
    public String helloworld() {
        return "Hello world!";
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/get")
    public String getPatient() {
        final ArrayList<String> rows = new ArrayList<String>();
        FindIterable<Document> iterable = db.getCollection("Patient").find();
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
