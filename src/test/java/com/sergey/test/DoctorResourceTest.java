package com.sergey.test;

import com.sergey.restclinic.resources.DoctorResource;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

public class DoctorResourceTest extends JerseyTest {
    
    @Override
    protected Application configure() {
        return new ResourceConfig(DoctorResource.class);
    }
    
    @Test
    public void testGetAllDoctors() {
        final String allDocs = target("doctors/getall").request()
                .get(String.class);
        System.out.println(allDocs);
    }
}
