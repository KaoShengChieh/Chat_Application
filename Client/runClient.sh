#!/bin/bash

set +e

rm *.class

set -e

javac -cp .:lib:lib/* Login.java -Xlint
javac FileSender.java
javac FileReceiver.java

java -cp .:lib:lib/* Login
