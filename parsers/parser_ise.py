#!/usr/bin/env python
##/usr/bin/python -u
import sys
tool="EXTERNAL_TOOL"
if len(sys.argv)>1:
    tool=sys.argv[1]

def isProblem(string):
    if string.startswith("ERROR:") or string.startswith("WARNING:") or string.startswith("INFO:"):
        return True
    return False
def hasLine(string):
    if '" Line ' in string:
        return True
    return False
def addTool(string,tool):
    if hasLine(string):
        return string
    else:
        index=string.find(" - ")+3
        return string[:index]+(" \"%s\" Line 0000:"%tool)+string[index:]
#        return string[:len(string)-1]+(" \"%s\" Line 0000:"%tool)+"\n"

  
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
