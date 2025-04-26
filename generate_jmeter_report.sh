#!/bin/bash

# $1 - Input file (.jmx)

jmeterBinPath="D:/Universitetas/apache-jmeter-5.6.3/bin"
outputDir="./Output"

$jmeterBinPath/jmeter -n -t $1 -l $1_data.csv -e -o $outputDir
