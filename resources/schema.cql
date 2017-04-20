CREATE KEYSPACE lcbo_master WITH replication = {'class':'SimpleStrategy', 'replication_factor' : '1'};

CREATE TABLE lcbo_master.users (
    id uuid,
    firstname text,
    lastname text,
    email text,
    created_date timestamp,
    PRIMARY KEY (id)
);

CREATE TABLE lcbo_master.usercredentials (
    email text,
    password text,
    id uuid,
    PRIMARY KEY (email)
);