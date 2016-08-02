# REST API Using Java and MongoDB
Learning building REST services using Java, Jersey and MongoDB. 

## Description
Returns XML output for GET commands and XML for POST commands. Currently not implementing entire CRUD for all documents in the db.

### Lacking
1. Updating of Appointments
2. Removing of Appointments
3. Updating of Doctors and Patients



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
```
<appointment>
	<doctor>
		<name>david</name>
	</doctor>
	<patient>
		<name>chris</name>
	</patient>
	<date>2016-05-12T23:00:00Z</date>
	<duration>1600</duration>
</appointment>
```
