package com.sergey.restclinic.resources;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.sergey.restclinic.database.DatabaseConnection;
import com.sergey.restclinic.models.Appointment;
import com.sergey.restclinic.models.Doctor;
import com.sergey.restclinic.models.Patient;
import com.sergey.restclinic.utils.DateTimeParser;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        
        Date start;
        Date end;
        DateTimeParser dtp = new DateTimeParser();
        try {
            start = dtp.parseDate(param_start);
            end = dtp.parseDate(param_end);
        } catch (ParseException ex) {
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
        MongoCollection<Document> waitlistCollection = db.mongodb.getCollection("Waitlist");
        
        // Get dates
//        DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);

        
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
        
        List<Appointment> apts;
        DateTimeParser dtp = new DateTimeParser();
        Date start;
        Date end;
        try {
            start = dtp.parseDate(apt.getStart());
            end = dtp.parseDate(apt.getEnd());
            apts = lookupAppointment(doctor, patient, start, end);
        } catch (ParseException ex) {
            // TODO needs some error handling for server not to freak out
            Logger.getLogger(AppointmentResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500)
                    .entity("Appointment creating failed. Error " + ex)
                    .build();
        }
        
        if (apts.size() > 0) {
            // add to waitlist
            addToWaitlist(start, end, doctor.getId(), patient.getId());
            return Response.status(400)
                    .entity("Appointment overlap. Added to waitlist").build();
        }
        
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
        
        Date start;
        Date end;
        DateTimeParser dtp = new DateTimeParser();
        try {
            start = dtp.parseDate(param_start);
            end = dtp.parseDate(param_end);
        } catch (ParseException ex) {
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
        
        // check if there is anything in waitlist
        List<Appointment> waitlistApts = getFromWaitlist(d.getId(), p.getId(), start, end);
        
        for (Appointment a : waitlistApts) {
            // try to insert them into appointments table
            boolean result;
            try {
                result = scheduleWaitlistedAppointment(a, start, end);
            } catch (ParseException e) {
                System.out.println("Parse exception " + e);
                break;
            }
            if (result == true) {
                // remove from waitlist
                try {
                    removeFromWaitlist(a);
                } catch (ParseException e) {
                System.out.println("Parse exception " + e);
                break;
            } 
                break;
            }
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
     * 
     * @param doctor
     * @param patient
     * @param start
     * @param end
     * @return all overlapping appointments found or null if not found
     * @throws java.text.ParseException
     */
    public List<Appointment> lookupAppointment(Doctor doctor, Patient patient, 
            Date start, Date end) throws ParseException {
        
        List<Appointment> apts = new ArrayList<>();
        
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> aptCollection = db.mongodb.getCollection("Appointment");
        
        Date[] dates = {start, end};
    
        // find overlapping start and end dates for a given doctor
        BasicDBObject query1 = new BasicDBObject();
        query1.put("start", new BasicDBObject("$lte", dates[0]));
        query1.put("end", new BasicDBObject("$gte", dates[0]));
        query1.put("doc_id", doctor.getId());
        
        BasicDBObject query2 = new BasicDBObject();
        query2.put("start", new BasicDBObject("$lte", dates[1]));
        query2.put("end", new BasicDBObject("$gte", dates[1]));
        query2.put("doc_id", doctor.getId());
        
        BasicDBObject query3 = new BasicDBObject();
        query3.put("start", new BasicDBObject("$lte", dates[0]));
        query3.put("end", new BasicDBObject("$gte", dates[1]));
        query3.put("doc_id", doctor.getId());
        
        BasicDBList or = new BasicDBList();
        or.add(query1);
        or.add(query2);
        or.add(query2);
        BasicDBObject orQuery = new BasicDBObject("$or", or);

        FindIterable<Document> appointments = aptCollection.find(orQuery);
        
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
     * 
     * @param doctor
     * @param patient
     * @param start
     * @param end
     * @return exact appointment
     * @throws java.text.ParseException
     */
    public List<Appointment> lookupExactAppointment(Doctor doctor, Patient patient, 
            Date start, Date end) throws ParseException {
        
        List<Appointment> apts = new ArrayList<>();
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> aptCollection = db.mongodb.getCollection("Appointment");
        
        Date[] dates = {start, end};
    
        // find overlapping start and end dates for a given doctor
        BasicDBObject query1 = new BasicDBObject();
        query1.put("start", dates[0]);
        query1.put("end", dates[1]);

        FindIterable<Document> appointments = aptCollection.find(query1);
        
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
     * @throws java.text.ParseException
     */
    public Date[] parseDates(String startDate, String endDate) throws ParseException {
        DateTimeParser dtp = new DateTimeParser();
        Date[] dates = new Date[2];
        dates[0] = dtp.parseDate(startDate);
        dates[1] = dtp.parseDate(endDate);
        return dates;
    }
    
    /**
     * Parse single date into Date object.
     * @param date
     * @return Date array
     * @throws java.text.ParseException
     */
    public Date parseDate(String date) throws ParseException {
        DateTimeParser dtp = new DateTimeParser();
        return dtp.parseDate(date);
    }
    
    /**
     * Add appointment to waitlist
     *
     * @param start
     * @param end
     * @param did
     * @param pid
     * @return Response
     */
    public Response addToWaitlist(Date start, Date end, String did, String pid) {
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> waitlistCollection = db.mongodb.getCollection("Waitlist");

        Document waitlistEntry = new Document();
        Document aptEntry = new Document();
        aptEntry.append("doc_id", did);
        aptEntry.append("pat_id", pid);
        aptEntry.append("start", start);
        aptEntry.append("end", end);
        waitlistEntry.append("appointment", aptEntry);

        try {
            waitlistCollection.insertOne(waitlistEntry);
        } catch (MongoException e) {
            return Response.status(500)
                    .entity("Failed to creat appointment").build();
        }

        return Response.status(400)
                .entity("Appointment overlap. Added to waitlist").build();
    }

    /**
     *
     * @param did
     * @param pid used to find and remove the waitlist entry
     * @param start
     * @param end
     * @return Appointment pulled from the Waitlist or null
     */
    public List<Appointment> getFromWaitlist(String did, String pid, Date start, Date end) {
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> waitlistCollection = db.mongodb.getCollection("Waitlist");
//        MongoCollection<Document> aptCollection = db.mongodb.getCollection("Appointment");
        
        // find all waitlist entries for this doctor
        Document query = new Document();
        query.append("appointment.doc_id", did);
        FindIterable<Document> iterable = waitlistCollection.find(query);
        
        List<Appointment> apts = new ArrayList<>();
        for (Document doc : iterable) {
            Document apt = (Document) doc.get("appointment");
            
            Doctor tempDoc = new Doctor();
            tempDoc.setId((String)apt.get("doc_id"));
            
            Patient tempPat = new Patient();
            tempPat.setId((String)apt.get("pat_id"));
            
            // reformat dates
            Date aptStart = apt.getDate("start");
            Date aptEnd = apt.getDate("end");
            DateFormat newFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String newStart = newFormatter.format(aptStart);
            String newEnd = newFormatter.format(aptEnd);
            
            Appointment a = new Appointment(
                    newStart, 
                    newEnd,
                    tempDoc,
                    tempPat);
            apts.add(a);
        }
        
        return apts;
    }

    public boolean scheduleWaitlistedAppointment(
            Appointment a, Date start, Date end) 
            throws ParseException {
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> aptCollection = db.mongodb.getCollection("Appointment");
        DateTimeParser dtp = new DateTimeParser();
        
        Date startDate = dtp.parseDate(a.getStart()); // <<<< failing here
        Date endDate = dtp.parseDate(a.getEnd());
        
        // check for overlapping appointments
        List<Appointment> apts = lookupAppointment(
                a.getDoctor(), a.getPatient(), startDate, endDate);
        
        if (apts.size() > 0) {
            return false;
        }
        
        Document newDoc = new Document();
        newDoc.append("doc_id", a.getDoctor().getId());
        newDoc.append("pat_id", a.getPatient().getId());
        newDoc.append("start", startDate);
        newDoc.append("end", endDate);
        
        aptCollection.insertOne(newDoc);
        return true;
    }

    /**
     * Remove appointment from the waitlist
     * @param a
     * @return 
     */
    private DeleteResult removeFromWaitlist(Appointment a) throws ParseException {
        DatabaseConnection db = DatabaseConnection.getInstance();
        MongoCollection<Document> waitlistCollection = db.mongodb.getCollection("Waitlist");
        Document query = new Document();
        DateTimeParser dtp = new DateTimeParser();
        
        query.append("appointment.doc_id", a.getDoctor().getId());
        query.append("appointment.pat_id", a.getPatient().getId());
        query.append("appointment.start", dtp.parseDate(a.getStart()));
        query.append("appointment.end", dtp.parseDate(a.getEnd()));
        
       
        return waitlistCollection.deleteOne(query);
    }
}