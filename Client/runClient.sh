#!/bin/bash

set +e

rm *.class

set -e

javac -cp .:lib:lib/* Login.java -Xlint

java -cp .:lib:lib/* Login
