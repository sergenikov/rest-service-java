var db = connect('127.0.0.1:27017/clinic');
db.createCollection("Doctor")
db.createCollection("Patient")
db.createCollection("Appointment")
db.createCollection("Waitlist")
