#!/bin/bash

echo "Please run mvn mvn plx:run (using the build_environments mvn) to start webdav..."
echo "Hit enter when ready"
read

cd target
tar -zxvf ../litmus-0.11.tar.gz 

cd litmus-0.11

if [ ! -f Makefile ]; then
  ./configure
fi

make

for i in http basic locks copymove props; do
  ./$i http://localhost:8080/webdav/ andy williams
done

cd ../../
