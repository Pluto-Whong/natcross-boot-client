#!/bin/bash
  
rm -rf ./source_code

git clone https://github.com/Pluto-Whong/natcross-boot-client.git ./source_code

mvn clean compile package -Dmaven.test.skip=true -f ./source_code/

. ./stop.sh

rm -rf natcross-booa-client.jar
cp ./source_code/target/natcross-boot-client.jar ./
