package com.sergey.restclinic.resources;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.sergey.restclinic.database.DatabaseConnection;
import com.sergey.restclinic.models.Appointment;
import com.sergey.restclinic.models.Patient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
        String sampleDate = "2016-09-05 12:30";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDateTime dateTime = LocalDateTime.parse(sampleDate, formatter);
        
        return Response.status(200).entity("Success. Added appointment.").build();
    }
}
