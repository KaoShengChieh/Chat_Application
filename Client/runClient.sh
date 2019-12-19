#!/bin/bash

set +e

rm *.class

set -e

javac -cp .:lib:lib/* GUI.java -Xlint -d .

java -cp .:lib:lib/* GUI
