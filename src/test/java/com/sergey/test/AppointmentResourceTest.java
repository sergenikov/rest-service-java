package com.sergey.test;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.sergey.restclinic.database.DatabaseConnection;
import com.sergey.restclinic.models.Appointment;
import com.sergey.restclinic.models.Doctor;
import com.sergey.restclinic.models.Patient;
import com.sergey.restclinic.resources.AppointmentResource;
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
import javax.ws.rs.core.Response;
import org.bson.Document;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class AppointmentResourceTest {
    
    final String TESTDOC1 = "testDoc1";
    final String TESTDOC2 = "testDoc2";
    final String TESTPAT1 = "testPat1";
    final String TESTPAT2 = "testPat2";
    
    final String INIT_DATE_START = "2016-08-05T14:00:00Z";
    final String INIT_DATE_END = "2016-08-05T14:30:00Z";
    
    final String datesample = "2016-08-05T14:00:00Z";
    
    final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    DatabaseConnection db = DatabaseConnection.getInstance();
    MongoCollection<Document> docCollection = db.mongodb.getCollection("Doctor");
    MongoCollection<Document> patCollection = db.mongodb.getCollection("Patient");
    MongoCollection<Document> aptCollection = db.mongodb.getCollection("Appointment");
    
    @Before
    public void setup() throws ParseException {
        insertDoctors();
        insertPatients();
        createAppointment(TESTDOC1, TESTPAT1, INIT_DATE_START, INIT_DATE_END);
        createAppointment(TESTDOC1, TESTPAT1, "2010-08-05T14:10:00Z", "2010-08-05T14:40:00Z");
    }
    
    @After
    public void teardown() {
        BasicDBObject searchQuery = new BasicDBObject();
        docCollection.deleteMany(searchQuery);
        patCollection.deleteMany(searchQuery);
        aptCollection.deleteMany(searchQuery);
    }
    
    @Test
    public void lookupAppointmentTest() throws ParseException {
        String start = "2016-08-05T14:10:00Z";
        String end = "2016-08-05T14:40:00Z";
        createAppointment(TESTDOC1, TESTPAT1, 
                start, end);
        AppointmentResource a = new AppointmentResource();
        Doctor doctor = AppointmentResource.lookupDoctor(TESTDOC1);
        Patient patient = AppointmentResource.lookupPatient(TESTPAT1);
        List<Appointment> apts = a.lookupAppointment(doctor, patient, start, end);
        assertEquals(1, apts.size());
    }
    
    @Test
    public void lookupAppointmentTestExact() throws ParseException {
        String start = INIT_DATE_START;
        String end = INIT_DATE_END;
        createAppointment(TESTDOC1, TESTPAT1, 
                start, end);
        AppointmentResource a = new AppointmentResource();
        Doctor doctor = new Doctor();
        Patient patient = new Patient();
        List<Appointment> apts = a.lookupAppointment(doctor, patient, start, end);
        assertEquals(2, apts.size());
    }
    
    /**
     * Insert two doctors in the db
     */
    public void insertDoctors() {
        // insert 2 test doctors
        List<Document> testDoctors = new ArrayList<>();
        
        Document testDoc1 = new Document();
        testDoc1.append("name", TESTDOC1);
        Document testDoc2 = new Document();
        testDoc2.append("name", TESTDOC2);
        
        testDoctors.add(testDoc1);
        testDoctors.add(testDoc2);
        
        docCollection.insertMany(testDoctors);
    }
    
    /**
     * Insert two doctors in the db
     */
    public void insertPatients() {
        // insert 2 test doctors
        List<Document> testPatients = new ArrayList<>();
        
        Document testPat1 = new Document();
        testPat1.append("name", TESTPAT1);
        Document testPat2 = new Document();
        testPat2.append("name", TESTPAT2);
        
        testPatients.add(testPat1);
        testPatients.add(testPat2);
        
        patCollection.insertMany(testPatients);
    }
    
    public void createAppointment(String doc, String pat, String start, String end)
            throws ParseException {
        Appointment a = new Appointment(start, end, 
                new Doctor(doc), new Patient(pat));
        
        DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        Date startDate = null;
        Date endDate = null;
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        startDate = format.parse(start);
        endDate = format.parse(end);
        
        Document newDoc = new Document();
        newDoc.append("doc_id", AppointmentResource.lookupDoctor(doc).getId());
        newDoc.append("pat_id", AppointmentResource.lookupPatient(pat).getId());
        newDoc.append("start", startDate);
        newDoc.append("end", endDate);
        
        aptCollection.insertOne(newDoc);
    }
}
