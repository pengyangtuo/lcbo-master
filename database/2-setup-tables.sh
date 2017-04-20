#!/usr/bin/env bash
echo "Creating keyspace and tables ..."
./apache-cassandra-3.10/bin/cqlsh -f ./dev-setup.cql
echo "Done"