#!/bin/bash

set -e

java -cp .:bin:cache:data:image:lib:lib/* ChatApp
