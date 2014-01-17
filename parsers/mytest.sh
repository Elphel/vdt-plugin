#!/bin/bash
echo $0 $*
echo "Running: $0"
 for i in $*; do
   echo $i
 done
#/bin/bash -s '/data/vdt/workspace_01/veditor/parsers/slow.sh | /data/vdt/workspace_01/veditor/parsers/parser01.py;'
#/data/vdt/workspace_01/veditor/parsers/slow.sh | grep "[a-z]";
/data/vdt/workspace_01/veditor/parsers/slow.sh;
exit 0;
