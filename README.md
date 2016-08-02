# ${1:Project Name}

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
/http://localhost:8080/restclinic/webapi/appointment/get?doc_name=david&pat_name=chris&date=2016-05-12T23:00:00Z

Will get appointment for Doctor=david, Patient=chris, Date=2016-05-12T23:00:00Z