#!/bin/bash

newVersion=$1
echo "raise version to ${newVersion}"
mvn --batch-mode release:update-versions org.eclipse.tycho:tycho-versions-plugin:update-eclipse-metadata -DdevelopmentVersion=${newVersion}
