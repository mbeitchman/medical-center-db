-- Import the Voter table for the synthetic database.

-- This script assumes that the Patient table from previous parts 
-- of this mini-project already exists.  It also assumes the existence of file
-- tableHealthyVoter.txt, which has the same schema as the Voter table below.  
-- The Voter table is formed by merging the two tables together.

-- Note that the Voter table may have groups of registered voters distinguished
-- only by the ID number.  In a real voter registration database there is more
-- information about each voter, so this is less likely to be the case.  


CREATE TABLE Voter (
       vid serial PRIMARY KEY,
       fname varchar(30),
       lname varchar(20),
       age integer,
       zipcode varchar(5)
);

\copy Voter(fname, lname, age, zipcode) from 'tableHealthyVoter.txt'
INSERT INTO Voter (fname, lname, age, zipcode)
	   SELECT fname, lname, age, zipcode
	   FROM Patient
	   WHERE random() <= 0.4;
