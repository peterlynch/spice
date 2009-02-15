#!/bin/bash

LITMUS_VERSION=0.12.1

echo "Please run mvn mvn plx:run (using the build_environments mvn) to start webdav..."
echo "Hit enter when ready"
read

cd target
tar -zxvf ../litmus-${LITMUS_VERSION}.tar.gz 

cd litmus-${LITMUS_VERSION}

if [ ! -f Makefile ]; then
  ./configure
fi

make

for i in http basic locks copymove props; do
  ./$i http://localhost:9000/webdav/ andy williams
done

cd ../../
