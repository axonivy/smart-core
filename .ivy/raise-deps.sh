#!/bin/bash

newVersion=${1/-SNAPSHOT/}
echo "raise deps to ${newVersion}"
sed -i -E "s#(p2.ivyteam.io/core/).*(/\")#\1${newVersion}\2#g" targetplatform/smart.target
