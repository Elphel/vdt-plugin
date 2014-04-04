#!/usr/bin/env python
##/usr/bin/python -u
import sys
import re
pattern=re.compile("\[[^[:]*:\d*]")
START_REF="(\\"
END_REF=" )"
#PREFIX_REF="@{"
#SUFFIX_REF="}@"
PREFIX_REF=""
SUFFIX_REF=""
MODE_IMMED=0
MODE_SINGLE=1
MODE_ONCE=2
MODE_POSTPONE=3
tool="EXTERNAL_TOOL"
if len(sys.argv)>1:
    tool=sys.argv[1]
try:
    global_top_module=sys.argv[2]
except:
    global_top_module=""
    
try:
    global_mode=int(sys.argv[3])
except:
    global_mode=MODE_POSTPONE # MODE_SINGLE

global_db={}
global_pRef=()        

sys.stdout.write("Running: %s %s %s %s\n" % (sys.argv[0],sys.argv[1],sys.argv[2],sys.argv[3]))


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

def getLineSignalBit(string):
#    sys.stdout.write(START_REF)
    if START_REF in string:
        start = string.find(START_REF)
        end = string.find(END_REF,start)
        if end <0:
            return None
        line=string[:start]+PREFIX_REF+"%s"+SUFFIX_REF+string[end+len(END_REF):]
        ref=string[start+len(START_REF):end]
        # replace "/" (used in Xilinx Vivado) with "." for Verilog
        ref=ref.replace("/",".")
        if "[" in ref:
            bitStart= ref.find("[")
            bitEnd=ref.find("]",bitStart)
            try:
                bit=int(ref[bitStart+1:bitEnd])
                ref=ref[:bitStart+1]+"%s"+ref[bitEnd:]
            except:
                bit=-1
        else:
            bit=-1
        return (line,ref,bit)
    else:
        return None

def addRef(pRef):
    global global_db
#TODO: put try on all    
    if pRef:
        if not pRef[0] in global_db:
            global_db[pRef[0]]={}
        line_type=global_db[pRef[0]]
        if not pRef[1] in line_type:
            line_type[pRef[1]]=set()
        line_type[pRef[1]].add(pRef[2])
def getRanges(pRef):
    global global_db
    try:
        s=global_db[pRef[0]][pRef[1]]
    except:
        s=set();
    ranges=[];
    while s:
        l=min(s)
        s.remove(l)
        h=l+1
        while h in s:
            s.remove(h)
            h=h+1
        ranges.append((l,h))
    return ranges        
            
def printLineRef(pRef):
    global global_pRef
    global global_mode
    global global_db
    global global_top_module
    if pRef:
        ranges=getRanges(pRef)
        if ranges:
#            line=pRef[0]
            ref=pRef[1];
            if global_top_module:
                ref= global_top_module+"."+ref
            for rng in ranges:
                if (rng[0]<0):
                    sys.stdout.write(pRef[0]%(ref))
                elif (rng[1]<=(rng[0]+1)):
                    sys.stdout.write(pRef[0]%(ref%(rng[0])))
                else:
                    sys.stdout.write(pRef[0]%(ref%("%d:%d"%(rng[1]-1,rng[0]))))
            if (global_mode == MODE_ONCE):
                for rng in ranges:
                    try:
                        global_db[pRef[0]][pRef[1]] -= set(range(rng[0],rng[1]))
                        if not global_db[pRef[0]][pRef[1]]:
                            global_db[pRef[0]].pop(pRef[1])
                            if not global_db[pRef[0]]:
                                global_db.pop(pRef[0])
                    except:
                        pass
                    
                
            

#add [tool_name:0000] if there is no {file:line_no] to generate Eclipse problem marker
def addTool(string,tool):
    global global_pRef
    global global_mode
    if hasFileVivado(string):
        if ((global_mode == MODE_SINGLE) or (global_mode == MODE_ONCE))  and global_pRef:
            printLineRef(global_pRef)
            global_pRef=None
        return string
    else:
#        return string[:len(string)-1]+"[%s:0000]"%tool+string[len(string)-1]
        if (string):
            string=string[:len(string)-1]+"[%s:0000]"%tool+string[len(string)-1]
        if global_mode != MODE_IMMED: 
            parseRef=getLineSignalBit(string)
        if (global_mode != MODE_IMMED) and parseRef:
            addRef(parseRef)
            if ((global_mode != MODE_POSTPONE) and global_pRef and 
             ((parseRef[0] != global_pRef[0]) or(parseRef[1] != global_pRef[1]))) :
                printLineRef(global_pRef)
            global_pRef=parseRef
            return ""
        else:
            if ((global_mode == MODE_SINGLE) or (global_mode == MODE_ONCE))  and global_pRef:
                printLineRef(global_pRef)
                global_pRef=None
            return string


def debugSize():
    l=0
    for line in global_db:
        for ref in global_db[line]:
            l+=len(global_db[line][ref])
    return l


#### Start
pline=""
for line in iter(sys.stdin.readline,''):
    if isProblem(pline):
        if line.startswith("   ") :
            pline = pline[:len(pline)-1]+line[2:]
        else:
            pline=addTool(pline,tool)
#            sys.stdout.write("*"+str(debugSize())+pline)
            sys.stdout.write(pline)
            pline = line
    else:
        pline = line
if isProblem(pline):
    sys.stdout.write(addTool(pline,tool))
    addTool("",tool)
    
if global_mode == MODE_POSTPONE:
    for line in global_db:
        for ref in global_db[line]:
            printLineRef((line,ref,0)) # will not add
#            printLineRef((line,ref,0)) # will not add

