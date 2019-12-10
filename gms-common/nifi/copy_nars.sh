#!/bin/bash

#find the nar files
nars=(`find ./ -name "*.nar"`)
 
#find the nifi containers
ps=`docker ps | grep nifi: | awk '{print $1}'`
echo $ps "\n"
nifis=($ps)
 
dir=/grid/persistence/gms_processors
echo $dir
 
for nifi in "${nifis[@]}"
do echo "nifi container: " $nifi
  for i in "${nars[@]}"
  do echo "copying nar:"$i
    docker cp $i $nifi:$dir
  done
  docker exec -it -u root $nifi chown -R nifi:root $dir
done
