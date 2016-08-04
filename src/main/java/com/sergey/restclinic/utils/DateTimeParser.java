/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sergey.restclinic.utils;

import com.sergey.restclinic.resources.AppointmentResource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DateTimeParser {
    
    final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    public DateTimeParser() {
        
    }
    
    /**
     * Parse string date input into Date object.
     * @param dateString
     * @return Date
     */
    public Date parseDate(String dateString) throws ParseException {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Date date;
        date = format.parse(dateString);
        return date;
    }
    
}
