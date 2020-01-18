#!/bin/bash

set +e

rm bin/*.class

set -e

javac -Xlint -cp bin:lib:lib/* src/*.java -d bin
