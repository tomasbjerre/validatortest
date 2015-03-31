#!/bin/bash
mkdir -p src/main/resources

cd src/main/resources

wget https://sideshowbarker.net/releases/jar/vnu.jar

wget https://sideshowbarker.net/releases/war/vnu.war

cd ../../..

mvn eclipse:eclipse
mvn test
