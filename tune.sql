-- Marc Beitchman
-- csep 544
-- hw 5

-- initial measurements with default indexes (running java TestQueries 2): 195303ms

CREATE INDEX disease_name on disease(disease);
CLUSTER disease_name on disease;
-- The above index was created to speed up the diseasecount query. A search key is created on the disease attribute 
-- since the where clause contains an exact match on K. I also clusted the table on disease name since there are 
-- no other queries on the disease table this is safe. This index resulted in a 20 second improvement overall.
--
-- after creating the above index and cluster: 160602ms 

CREATE INDEX patient_zipcode on Patient(zipcode);
-- The above index was created to speed up the query that selects all patients with a specific zipcode.
-- after creating the above index: 111790ms

CREATE INDEX patient_age on Patient(age);
-- The above index was created to speed up the query that selets patients within an age range.
-- after creating the above index: 79295ms

CREATE INDEX patient_fnamelname on Patient(fname,lname);
-- The above index was created to speed up the selection of patient in the docs for patient query.
-- after creating the above index: 49016ms

CREATE INDEX Sees_did on Sees(did);
-- The above index is needed to speed up the join on Sees.did since the primary key for the Sees table
-- consists of did and pid and pid comes first. The default index is unusable for a join on Sees.did.
-- after creating the above index: 15178ms

CLUSTER patient_age on Patient;
-- Speed up the range query using patient age.
-- after creating the above cluster: 14648ms

CREATE INDEX doctor_specialty on Doctor(specialty);
-- The above index was created to speed up the query that selects all doctors with a specific specialty.
-- after creating the above index: 13323ms

CREATE INDEX doctor_fnamelname on Doctor(fname,lname);
-- The above index was created to speed up the selection of docs in the patient for docs query
---after creating the above index 11824ms

CLUSTER Sees_did on sees;
-- The above cluster speeds up the join on Sees.did.
-- after creating the above cluster: 11590ms

-- Resulting speed-up is 195303/11590 = 16.85x
