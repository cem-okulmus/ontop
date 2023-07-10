
Create Table dep (
   id  		VARCHAR(100), 
   type 	VARCHAR(100) NOT NULL,
   location	VARCHAR(100) NOT NULL,
   tstart	DATE, 
   tend		DATE,
   PRIMARY KEY(id, tstart, tend)
);



CREATE TABLE emp (
    id           VARCHAR(100),
    name         VARCHAR(100) NOT NULL,
    department   VARCHAR(100),
    tstart    DATE,
    tend      DATE,
    PRIMARY KEY(id,tstart,tend)
);

INSERT INTO emp(id,name,department,tstart,tend) VALUES
	('e1', 'john','d1','1998-01-01','2000-01-01'),
	('e1', 'john','d3','2000-01-01','2003-01-01'),
	('e2', 'mark','d2','1999-01-01','2002-01-01');


INSERT INTO dep(id,type,location,tstart,tend) VALUES
	('d1','financial','madrid','1998-01-01','1999-01-01'),
	('d1','financial','barcelona','1999-01-01','2003-01-01'),
	('d2','hr','barcelona','2000-01-01','2003-01-01'),
	('d3','hq','london','2000-01-01','2003-01-01');
