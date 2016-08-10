# REST API Using Java and MongoDB
Learning building REST services using Java, Jersey and MongoDB. 

## Description
Returns XML output for GET commands and XML for POST commands. Currently not implementing entire CRUD for all documents in the db.

### Lacking
1. Updating of Appointments
2. Updating of Doctors and Patients

### Docker image for App + Glassfish
`https://hub.docker.com/r/sergenikov/glassfish/`


## /restclinic/webapi/doctors

###Get
`/getall`		gets all doctors
`/getbyname?name=tom`	gets single doctor by name

###Post
`/add`			adds a new doctor

```
<doctor>
    <name>Gregory House</name>
</doctor>
```

###Delete
`/remove/{doctor name}`



## /restclinic/webapi/patients
###Get
`/getall`		        gets all patients 
`/getbyname?name=tom`	gets single patient by name

###Post
`/add`			        adds a new patient
```
<patient>
    <name>John Doe</name>
</patient>
```

###Delete
`/remove/{patient name}`



## /restclinic/webapi/appointments

###Get
`/restclinic/webapi/appointment/get?doc_name=david&pat_name=chris&date=2016-05-12T23:00:00Z`

Will get list of appointments for `Doctor=david, Patient=chris, Date=2016-05-12T23:00:00Z`.

###Post
Will add new appointment to the database. Add the following XML to request.
This operation will fail if either patient or doctor does not exist.
```
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
```


### Test case for testing waitlist function
POST `/restclinic/webapi/doctors/add`
```
<doctor>
    <name>david</name>
</doctor>
```

POST `/restclinic/webapi/patients/add`
```
<patient>
    <name>chris</name>
</patient>
```

POST `/restclinic/webapi/appointment/add`
```
<appointment>
    <doctor>
        <name>david</name>
    </doctor>
    <patient>
        <name>chris</name>
    </patient>
    <start>2016-08-05T10:00:00Z</start>
    <end>2016-08-05T11:00:00Z</end>
</appointment>
```

POST `/restclinic/webapi/appointment/add`  
Overlapping appointment with the one above.
```
<appointment>
    <doctor>
        <name>david</name>
    </doctor>
    <patient>
        <name>chris</name>
    </patient>
    <start>2016-08-05T09:00:00Z</start>
    <end>2016-08-05T11:00:00Z</end>
</appointment>
```

DELETE `/restclinic/webapi/appointment/remove?doc_name=david&pat_name=chris&start=2016-08-05T10:00:00Z&end=2016-08-05T11:00:00Z`  
Delete first appointment, which will pull waitlisted appointment from the db.

Do a get to verify that it's there now in the Appointment table.  
GET `/restclinic/webapi/appointment/get?doc_name=david&pat_name=chris&start=2016-08-05T09:00:00Z&end=2016-08-05T11:00:00Z`
