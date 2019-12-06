#!/bin/bash

set -e

javac -cp .:./lib/ Server.java
java -cp .:./lib/ Server
