package com.sergey.test;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.sergey.restclinic.database.DatabaseConnection;
import com.sergey.restclinic.models.Doctor;
import com.sergey.restclinic.resources.DoctorResource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import org.bson.Document;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DoctorResourceTest extends JerseyTest {
    
    final String TESTDOC1 = "testDoc1";
    final String TESTDOC2 = "testDoc2";
    final String TESTDOC3 = "testDoc3";
    final String DOESNOTEXIST = "doesnotexist";
    
    DatabaseConnection db = DatabaseConnection.getInstance();
    MongoCollection<Document> docCollection = db.mongodb.getCollection("Doctor");
    
    @Before
    public void setup() {
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
    
    @After
    public void teardown() {
        BasicDBObject searchQuery = new BasicDBObject();
        docCollection.deleteMany(searchQuery);
    }
    
    @Override
    protected Application configure() {
        return new ResourceConfig(DoctorResource.class);
    }
    
    @Test
    public void testGetAllDoctors()
            throws SAXException, ParserConfigurationException, IOException {
        GenericType<List<Doctor>> doctors = new GenericType<List<Doctor>>(){};
        List<Doctor> responseDoctors = target("doctors/getall")
                .request(MediaType.APPLICATION_XML)
                .get(doctors);
        assertEquals(responseDoctors.size(), 2);
    }
    
    @Test
    public void testGetDoctorByName()
            throws SAXException, ParserConfigurationException, IOException {
        GenericType<List<Doctor>> doctors = new GenericType<List<Doctor>>(){};
        List<Doctor> responseDoctors = target("doctors/getbyname")
                .queryParam("name", TESTDOC1)
                .request(MediaType.APPLICATION_XML)
                .get(doctors);
        assertEquals(responseDoctors.size(), 1);
    }
    
    @Test
    public void testGetDoctorByNameDoesNotExist()
            throws SAXException, ParserConfigurationException, IOException {
        GenericType<List<Doctor>> doctors = new GenericType<List<Doctor>>(){};
        List<Doctor> responseDoctors = target("doctors/getbyname")
                .queryParam("name", DOESNOTEXIST)
                .request(MediaType.APPLICATION_XML)
                .get(doctors);
        assertEquals(responseDoctors.size(), 0);
    }
    
    /**
     * Get size of doctors list
     * NOT USED - hopefully won't used.
     * @param allDoctorsXML
     * @return
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException 
     */
    private int getListSize(String allDoctorsXML) 
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = dBuilder.parse(allDoctorsXML);
        doc.getDocumentElement().normalize();
        
        NodeList docList = doc.getElementsByTagName("doctors");
        
        return docList.getLength();
    }
}
