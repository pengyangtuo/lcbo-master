#!/usr/bin/env bash

echo "---------------------"
echo "Downloading cassandra"
echo "---------------------"
curl -o cassandra.tar.gz http://apache.forsale.plus/cassandra/3.10/apache-cassandra-3.10-bin.tar.gz

echo "--------------------"
echo "Uncompress cassandra"
echo "--------------------"
tar -xvf ./cassandra.tar.gz

echo "---------------------------------"
echo "Removing downloaded compress file"
echo "---------------------------------"
rm ./cassandra.tar.gz

echo "---------------"
echo "Start cassandra"
echo "---------------"
./apache-cassandra-3.10/bin/cassandra

