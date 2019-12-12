#!/bin/bash

set +e

rm *.class

set -e

javac -cp .:lib:lib/* Client.java -d .

java -cp .:lib:lib/* Client
