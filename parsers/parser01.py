#!/usr/bin/env python
##/usr/bin/python -u
import sys
print "started"
n=1;
for line in iter(sys.stdin.readline,''):
    sys.stdout.write ("%04d: %s"%(n,line))
#    sys.stdout.flush()
    n+=1
