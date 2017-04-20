# LCBO master

Simple RESTful service built with [Play Framwork](https://www.playframework.com/)

This service allow users to:
- create account
- log in
- reset password
- search for nearby LCBO stores (using [LCBO API](https://lcboapi.com/docs/v1/stores))

## Install and run
This application uses [Cassandra](http://cassandra.apache.org/download/) as its data store. The `database` folder contains two script that will help you setup local Cassandra.

Open up a terminal, go to the database folder and run the `1-install-cassandra.sh` script, this script will download a copy of cassandra and start it, so make sure port `9042` if available in your computer:
```bash
> cd <project_folder>/database
> ./1-install-cassandra.sh
```

Once Cassandra is up and running, open up another terminal and run the second script `2-setup-table.sh`, this script will create keyspace and tables that will be used by our application:
```bash
> cd <project_folder>/database
> ./2-setup-table.sh
```

Now we can spine up the application by:
```bash
> cd <project_folder>
> sbt run
```
