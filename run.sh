#!/bin/bash

cd client
npm install pm2 -g
pm2 start npm -- start
cd ../server
sbt run 