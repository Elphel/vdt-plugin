#!/usr/bin/env python
##/usr/bin/python -u
import sys
tool="EXTERNAL_TOOL"
if len(sys.argv)>1:
    tool=sys.argv[1]

def isProblem(str):
    if str.startswith("ERROR:") or str.startswith("WARNING:") or str.startswith("INFO:"):
        return True
    return False
def hasLine(str):
    if '" Line ' in str:
        return True
    return False
def addTool(str,tool):
    if hasLine(str):
        return str
    else:
        index=str.find(" - ")+3
        return str[:index]+(" \"%s\" Line 0000:"%tool)+str[index:]
#        return str[:len(str)-1]+(" \"%s\" Line 0000:"%tool)+"\n"

  
pline=""
for line in iter(sys.stdin.readline,''):
    if isProblem(pline):
        if line.startswith("   ") :
            pline = pline[:len(pline)-1]+line[2:]
        else:
            sys.stdout.write(addTool(pline,tool))
            pline = line
    else:
        pline = line
if isProblem(pline):
    sys.stdout.write(addTool(pline,tool))
