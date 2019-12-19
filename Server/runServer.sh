#!/bin/bash

set +e

rm *.class

set -e

javac -cp .:lib:lib/* Server.java -Xlint -d .

java -cp .:lib:lib/* Server
