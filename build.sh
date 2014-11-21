#!/bin/bash
mkdir -p src/main/resources

cd src/main/resources

wget https://github.com/validator/validator/releases/download/20141006/vnu-20141013.jar.zip
unzip *zip
mv vnu/*jar .
rm -rf vnu
rm -rf *zip

wget https://github.com/validator/validator/releases/download/20141006/vnu-20141013.war.zip
unzip *zip
mv vnu/*war .
rm -rf vnu
rm -rf *zip

cd ../../..

mvn eclipse:eclipse
mvn test
