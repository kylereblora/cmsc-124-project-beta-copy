create database demo;

create table bugs {
	bug_id int(3) AUTO_INCREMENT,
	date_found datetime,
	status varchar(3),
	description varchar(256),
	CONSTRAINT bugs_bugid_pk PRIMARY KEY(bug_id)
};

delimiter //
create procedure addBug{
	a varchar(256)
}
BEGIN
	insert into bugs(date_found,status,description) values(now(), "INC", a);
END //

create procedure fixedBug{
	a int(3)
}
BEGIN
	update bugs set status="FIN" where a=bug_id;
END