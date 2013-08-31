-- Marc Beitchman
-- csep544
-- HW5

-- 3.1.1
CREATE ROLE doctor;
CREATE ROLE nurse;
CREATE ROLE uwmcadmin;

CREATE USER doctor1 WITH PASSWORD '12345' IN ROLE doctor;
CREATE USER nurse1 WITH PASSWORD '12345' IN ROLE nurse;
CREATE USER admin1 WITH PASSWORD '12345' IN ROLE uwmcadmin;
CREATE USER guest1 WITH PASSWORD '12345';

-- 3.1.2
CREATE VIEW PublicDoctorInfo AS
SELECT fname, lname, specialty
from Doctor;

CREATE VIEW PublicTreatedDiseaseInfo AS 
SELECT DISTINCT disease 
FROM Disease;

CREATE VIEW PublicSupplierInfo as
SELECT DISTINCT name
FROM Supplier;

GRANT SELECT ON PublicDoctorInfo TO PUBLIC;
GRANT SELECT ON PublicTreatedDiseaseInfo TO PUBLIC;
GRANT SELECT ON PublicSupplierInfo TO PUBLIC;

-- 3.1.3
GRANT SELECT,UPDATE ON Doctor TO DOCTOR;
GRANT SELECT,UPDATE ON Disease TO DOCTOR;
GRANT SELECT,UPDATE ON Patient TO DOCTOR;
GRANT SELECT,UPDATE ON Sees TO DOCTOR;

GRANT SELECT ON Doctor TO NURSE;
GRANT SELECT,UPDATE,INSERT,DELETE ON Disease TO NURSE;
GRANT SELECT,UPDATE,INSERT,DELETE ON Patient TO NURSE;
GRANT SELECT,UPDATE,INSERT,DELETE ON Sees TO NURSE;

GRANT SELECT,UPDATE,INSERT,DELETE ON Product TO UWMCADMIN;
GRANT SELECT,UPDATE,INSERT,DELETE ON Stock TO UWMCADMIN;
GRANT SELECT,UPDATE,INSERT,DELETE ON Supplier TO UWMCADMIN;
GRANT SELECT,UPDATE,INSERT,DELETE ON Supplies TO UWMCADMIN;
GRANT SELECT,UPDATE,INSERT,DELETE ON Doctor TO UWMCADMIN;

-- 3.2

-- create role researcher
CREATE ROLE researcher;

-- create user researcher1
CREATE USER researcher1 WITH PASSWORD '12345' IN ROLE researcher;

-- create view 
CREATE VIEW DiseaseResearch AS 
SELECT p.zipcode, p.age, d.disease 
FROM Patient p, Disease d 
WHERE p.pid = d.pid;

GRANT SELECT ON DiseaseResearch TO researcher;
GRANT SELECT ON Voter TO PUBLIC;

-- Show how a researcher can now uncover with high likelihood the disease(s) that certain people have.
-- join on age and zipcode
 SELECT * 
 FROM DiseaseResearch dr, Voter v 
 WHERE v.age = dr.age AND v.zipcode = dr.zipcode;

-- The fact that a researcher can identify the name of a person to the disease they have with
-- high confidence by joining the DiseaseResearch view with the voter table is a problem
-- because the exposure of personal medical records is considered a privacy breach.

-- In order to avoid this privacy breach, views of all possible aggregate queries on the DiseaseResearch 
-- view could be provided to researchers. This would prevent researchers from accessing individual
-- rows and prevent them from joining the DiseaseResearch view with the voter table.

-- Another option is to add functionality to the DBMS to only allow aggregrate queries on
-- the DiseaseResearch view which would also prevent individual row access on the DiseaseResearch
-- view. This would have to be implemented in the query parser and could be a new privelege 
-- class called select-aggregate.

-- Another option is to allow new patients to opt-in to having their info provided in the
-- DiseaseResearch view. The patient would be given a full disclosure of what would be done with their
-- data. This could be implemented by adding a new column of type bool to the Patient table. The create view statement 
-- would be modified as follows:

CREATE VIEW DiseaseResearch AS 
SELECT p.zipcode, p.age, d.disease 
FROM Patient p, Disease d 
WHERE p.pid = d.pid and p.researchviewaccessible = 'true';