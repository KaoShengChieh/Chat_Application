#!/bin/bash

set +e

rm *.class

set -e

javac -cp .:lib:lib/* Server.java -d .

java -cp .:lib:lib/* Server
