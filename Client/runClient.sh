#!/bin/bash

set -e

javac -cp .:./lib/ Client.java
java -cp .:./lib/ Client 
