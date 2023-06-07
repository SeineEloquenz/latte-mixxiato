#!/bin/sh
for i in $(seq $1)
do
  port=$(expr 10000 + $i)
  seed=$RANDOM
  java -jar client.jar $port $seed &
done
sleep infinity
