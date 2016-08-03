package com.sergey.restclinic.resources;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.Block;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.QueryBuilder;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.lt;
import com.mongodb.client.result.DeleteResult;
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
import javax.ws.rs.DELETE;
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
     * @param param_start     date and time in local time
     * @return 
     */
    @GET
    @Path("get")
    @Produces(MediaType.APPLICATION_XML)
    public List<Appointment> getAppointment(
            @QueryParam("doc_name") String param_doc_name,
            @QueryParam("pat_name") String param_pat_name,
            @QueryParam("start") String param_start,
            @QueryParam("end") String param_end) {
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> docCollection = db.mongodb.getCollection(CURRENT_COLLECTION);
        
        // lookup in db for ids
        Doctor doctor = lookupDoctor(param_doc_name);
        Patient patient = lookupPatient(param_pat_name);
        
        if (doctor == null) {
            documentNotFoundError(param_doc_name);
        } else if (patient == null) {
            documentNotFoundError(param_pat_name);
        }
        
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        // get date from request
        Date start;
        Date end;
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            start = format.parse(param_start);
            end = format.parse(param_end);
        } catch (ParseException ex) {
            // TODO needs some error handling for server not to freak out
            Logger.getLogger(AppointmentResource.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("in date error");
            return null;
        }
        
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("doc_id", doctor.getId());
        searchQuery.put("pat_id", patient.getId());
        searchQuery.put("start", start);
        searchQuery.put("end", end);
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
                    start.toString(),
                    end.toString(),
                    lookupDoctor(param_doc_name),
                    lookupPatient(param_pat_name)
            );
            appts.add(appointment);
        }
        
//        System.out.println("before returning");
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
                + " startDateTime "  + apt.getStart()
                + " endDateTime "      + apt.getEnd());
        
        // lookup in db for ids
        Doctor doctor = lookupDoctor(apt.getDoctor().getName());
        Patient patient = lookupPatient(apt.getPatient().getName());
        
        // Error if can't find either patient or doctor
        if (doctor == null) {
            return Response.status(400)
                    .entity("Appointment creating failed. Can't find doctor " 
                            + apt.getDoctor().getName())
                    .build();
        } else if (patient == null) {
            return Response.status(400)
                    .entity("Appointment creating failed. Can't find patient " 
                            + apt.getPatient().getName())
                    .build();
        }
        
        // get date from request
        Date start = null;
        Date end = null;
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            start = format.parse(apt.getStart());
            end = format.parse(apt.getEnd());
        } catch (ParseException ex) {
            // TODO needs some error handling for server not to freak out
            Logger.getLogger(AppointmentResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500)
                    .entity("Appointment creating failed. Error " + ex)
                    .build();
        }
        
        //do lookup and check here
        
        // create new appointment document for mongo
        Document newDoc = new Document();
        newDoc.append("doc_id", doctor.getId());
        newDoc.append("pat_id", patient.getId());
        newDoc.append("start", start);
        newDoc.append("end", end);
        
//        System.out.println("AppointmentResource.createAppointment(): Created document" 
//                + newDoc.toString());

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
     * Return all appointments that match all of these requirements
     * @param param_doc_name doctor name
     * @param param_pat_name patient name
     * @param param_date     date and time in local time
     * @return 
     */
    @DELETE
    @Path("remove")
    public Response deleteAppointment(
            @QueryParam("doc_name") String param_doc_name,
            @QueryParam("pat_name") String param_pat_name,
            @QueryParam("start") String param_start,
            @QueryParam("end") String param_end) {
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> docCollection = db.mongodb.getCollection(CURRENT_COLLECTION);
        
        // lookup in db for ids
        Doctor d = lookupDoctor(param_doc_name);
        Patient p = lookupPatient(param_pat_name);
        
        if (d == null) {
            documentNotFoundError(param_doc_name);
        } else if (p == null) {
            documentNotFoundError(param_pat_name);
        }
        
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        // get date from request
        Date start = null;
        Date end = null;
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            start = format.parse(param_start);
            end = format.parse(param_end);
        } catch (ParseException ex) {
            // TODO needs some error handling for server not to freak out
            Logger.getLogger(AppointmentResource.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("in date error");
            return null;
        }
        
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("doc_id", d.getId());
        searchQuery.put("pat_id", p.getId());
        searchQuery.put("start", start);
        searchQuery.put("end", end);
        
        DeleteResult deleteResult;
        try {
            deleteResult = docCollection.deleteOne(searchQuery);
        } catch (MongoWriteConcernException e) {
            System.err.println(e);
            return Response.status(400)
                    .entity("Removing appointment failed; error " + e)
                    .build();
        } catch (MongoException e) {
            System.err.println(e);
            return Response.status(400)
                    .entity("Removing appointment failed; error " + e)
                    .build();
        }
        
        if (deleteResult.getDeletedCount() == 0) {
            return Response.status(400).entity("Failed to remove.").build();
        }
        
        return Response.status(200).entity("Success. Removed appointment").build();
    }
    
    /**
     * Return doctor by name from the db
     * @param name doctor's name
     * @return Doctor object or null if did not find anything
     * When iterating over doctors found assumes there is only one doctor
     * with that name - return first document found.
     */
    static public Doctor lookupDoctor(String name) {
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
    static public Patient lookupPatient(String name) {
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
     * 
     * @param apt appointment to lookup in the db
     * @return appointment found or null if not found
     */
    public List<Appointment> lookupAppointment(Doctor doctor, Patient patient, 
            String start, String end) throws ParseException {
        
        List<Appointment> apts = new ArrayList<>();
        
        Appointment apt = new Appointment(start, end, doctor, patient);
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> aptCollection = db.mongodb.getCollection("Appointment");
        
        Date[] dates = parseDates(start, end);
    
        // find overlapping start and end dates for a given doctor
        BasicDBObject query = new BasicDBObject();
        query.put("start", new BasicDBObject("$lte", dates[0]));
        query.put("end", new BasicDBObject("$gte", dates[0]));
        String queryString = query.toJson();
        
        FindIterable<Document> appointments = aptCollection.find(query);
        
        for (Document d : appointments) {
            apts.add(new Appointment(
                    d.getDate("start").toString(),
                    d.getDate("end").toString(),
                    doctor,
                    patient));
        }
        return apts;
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
    
    /**
     * Parse two input dates into Date object.
     * @param startDate
     * @param endDate
     * @return Date array
     */
    public Date[] parseDates(String startDate, String endDate) throws ParseException {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Date[] dates = new Date[2];
        dates[0] = format.parse(startDate);;
        dates[1] = format.parse(endDate);;
        return dates;
    }
    
    /**
     * Parse two input dates into Date object.
     * @param startDate
     * @param endDate
     * @return Date array
     */
    public Date parseDate(String date) throws ParseException {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        return format.parse(date);
    }
}


/*
<appointment>
    <doctor>
        <name>david</name>
    </doctor>
    <patient>
        <name>chris</name>
    </patient>
    <start>2016-08-05T16:00:00Z</start>
    <end>2016-08-05T16:30:00Z</end>
</appointment>
*/