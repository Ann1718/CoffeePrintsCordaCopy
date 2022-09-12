#!/usr/bin/bash

echo "======== START: Building Nodes ========"
if ./gradlew clean deployNodes; then
  echo "======== END: Building Nodes ========"
else
  exit
fi

echo "======== START: Running Nodes ========"
if [ -d "build" ]; then
    cd build/nodes
fi

if ./runnodes; then
  echo "======== END: Running Nodes ========"
else
  exit
fi