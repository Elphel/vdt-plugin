#!/usr/bin/env python
##/usr/bin/python -u
import sys
import re
pattern=re.compile("\[[^[:]*:\d*]")
tool="EXTERNAL_TOOL"
if len(sys.argv)>1:
    tool=sys.argv[1]

def isProblem(string):
    if string.startswith("ERROR:") or string.startswith("WARNING:") or string.startswith("INFO:"):
        return True
    return False
#def hasLine(string):
#    if '" Line ' in string:
#        return True
#    return False
#Problem string has "[filename:line_number]"
def hasFileVivado(string): # [*:*]
    if pattern.findall(string):
        return True
    return False
#add [tool_name:0000] if there is no {file:line_no] to generate Eclipse problem marker
def addTool(string,tool):
    if hasFileVivado(string):
        return string
    else:
        return string[:len(string)-1]+"[%s:0000]"%tool+string[len(string)-1]
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

