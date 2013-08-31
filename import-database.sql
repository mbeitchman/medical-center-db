-- Import the synthetic database.

-- This script assumes the data for table X is in the file tableX.txt .
-- If that is not true, feel free to modify the script in the appropriate places.

-- Note that Postgres automatically creates indices on primary key columns.
-- The existence of the index affects a few of the queries in the test query set.

CREATE TABLE Patient (
	   pid int PRIMARY KEY,
	   fname varchar(30),
	   lname varchar(20),
	   age int,
	   street varchar(20), 
	   city varchar(10), 
	   zipcode varchar(5)
);
\copy Patient from 'tablePatient.txt'

CREATE TABLE Disease(
       pid int REFERENCES Patient(pid) ON DELETE CASCADE, 
       disease varchar(20),
       PRIMARY KEY(pid,disease)
);
\copy Disease from 'tableDisease.txt'

CREATE TABLE Doctor(
       did int PRIMARY KEY, 
       fname varchar(30), 
       lname varchar(20), 
       specialty varchar(20)
);
\copy Doctor from 'tableDoctor.txt'

CREATE TABLE Sees(
       pid int REFERENCES Patient(pid) ON DELETE CASCADE,
       did int REFERENCES Doctor(did) ON DELETE NO ACTION,
       PRIMARY KEY(pid,did)
);
\copy Sees from 'tableSees.txt'

CREATE TABLE Product(
       eid int PRIMARY KEY, 
       description varchar(20)
);
\copy Product from 'tableProduct.txt'

CREATE TABLE Stock(
       eid int PRIMARY KEY REFERENCES Product(eid) ON DELETE CASCADE, 
       quantity int
);
\copy Stock from 'tableStock.txt'

CREATE TABLE Supplier(
       sid int PRIMARY KEY, 
       name varchar(20), 
       street varchar(20), 
       city varchar(10), 
       zipcode varchar(5)
);
\copy Supplier from 'tableSupplier.txt'

CREATE TABLE Supplies(
       eid int REFERENCES Product(eid) ON DELETE CASCADE,
       sid int REFERENCES Supplier(sid) ON DELETE SET NULL,
       UNIQUE (eid,sid)
);
\copy Supplies from 'tableSupplies.txt'
