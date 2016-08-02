# REST API Using Java and MongoDB
Learning building REST services using Java, Jersey and MongoDB. 

## Description
XML Output

## /restclinic/webapi/doctors

###Get
/getall			gets all doctors
/getbyname?name=tom	gets single doctor by name

###Post
/add			adds a new doctor

`<doctor>
    <name>Gregory House</name>
</doctor>`

###Delete
/delete
<doctor>
	<name>Gregory House</name>
</doctor>

## /restclinic/webapi/patients
Same commands as doctor

## /restclinic/webapi/appointments

###Get
/restclinic/webapi/appointment/get?doc_name=david&pat_name=chris&date=2016-05-12T23:00:00Z

Will get list of appointments for `Doctor=david, Patient=chris, Date=2016-05-12T23:00:00Z`.

###Post
Will add new appointment to the database.
`
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
`