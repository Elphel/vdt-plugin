#!/usr/bin/env python
# -*- coding: utf-8 -*-
# Copyright (C) 2013, Elphel.inc.
# configuration of the DDR-related registers
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#  
#  Additional permission under GNU GPL version 3 section 7:
#  If you modify this Program, or any covered work, by linking or combining it
#  with Eclipse or Eclipse plugins (or a modified version of those libraries),
#  containing parts covered by the terms of EPL/CPL, the licensors of this
#  Program grant you additional permission to convey the resulting work.
#  {Corresponding Source for a non-source form of such a combination shall
#  include the source code for the parts of Eclipse or Eclipse plugins used
#  as well as that of the covered work.}
 
__author__ = "Andrey Filippov"
__copyright__ = "Copyright 2016, Elphel, Inc."
__license__ = "GPL"
__version__ = "3.0+"
__maintainer__ = "Andrey Filippov"
__email__ = "andrey@elphel.com"
__status__ = "Development"
"""
Mimics cocotb/Python problems to those of Icarus, passing icarus through
<Filepath>:<line>: [warning|error|info]: <description>
"""


import sys
import re
RAISED_EXCEPTION="raised exception:"
COMMA_LINE=", line"
state = None # None - pass, cocotb - collecting Cocotb problem, python- just raw python problem
lines = []
PATTERN_PS =        re.compile(r"(\d+\.\d+) ps")
PATTERN_FILE_LINE = re.compile('([\w/-]*\.[v|V][hH]?):\s*([0-9]+)')
TRUNCATE_PATH =     True
NS_FORMAT =         "%10.3fns"
def report_python(lines):
    err_msg=lines[0]
    for i in range((len(lines)-1)//2):
        fline=lines[2*i+1]
        fileStart= fline.find('"')+1
        fileEnd=   fline.find('"',fileStart)
        fpath = fline[fileStart:fileEnd]
        flineNumStart=fline.find(COMMA_LINE,fileEnd)+len(COMMA_LINE)
        flineNumEnd=  fline.find(',',flineNumStart)
        flineNum = fline[flineNumStart:flineNumEnd].strip()
        infunction = fline[flineNumEnd+1:].strip()
        lineTxt = lines[2*i+2]
        sys.stdout.write('%s:%s: error: %s, %s "%s"\n'%(fpath, flineNum, err_msg, infunction, lineTxt))

for line in iter(sys.stdin.readline,''):
    sline = line.strip()
    #Replace picoseconds with nanoseconds
    ps=PATTERN_PS.match(sline)
    while ps:
        try:
            ns=NS_FORMAT%(0.001*float(ps.group(1)))
        except:
            break
        sline = sline[:ps.start(0)]+ns+sline[ps.end(0):]
        ps = PATTERN_PS.match(sline)
    #Recognize verilog file:line and get rid of spaces between them
    
    fl=PATTERN_FILE_LINE.search(sline)
    if fl:
        if fl.start(0)!=0: #Icarus compiler output, keep as is (controlled by warnings/errors/sorry
            
            #lsof -i TCP| fgrep LISTEN
            fname=fl.group(1)
            if TRUNCATE_PATH:
                try:
                    lastSlashIndex=fname.rindex('/')
                    fname=fname[lastSlashIndex+1:]
                except:
                    pass
            subl="SIM: %20s:%4d in simulator "%(fname,int(fl.group(2)))
            sline = sline[:fl.start(0)]+subl+sline[fl.end(0):]
            
            
            
    if state == "cocotb":
        if (len(lines)%2 == 0) or sline.startswith('File "'):
            lines.append(sline)
        else:
            report_python(lines)
            state = None
            sys.stdout.write(line)    
    elif state == "python":
        if (len(lines)%2 == 0) or sline.startswith('File "'):
            lines.append(sline)
        else:
            lines[0] = sline # instead of "Traceback (most recent call last):"
            report_python(lines)
            state = None
    else:
        if sline.startswith("Traceback (most"):
            #print("***Got Traceback***")
            state = "python"
            lines =[sline]
            continue
        elif (line.find("ERROR")>=0) and (line.find(RAISED_EXCEPTION)>0):
            #print("***Got Cocotb error***")
            index=line.find(RAISED_EXCEPTION)+len(RAISED_EXCEPTION)
            sline=  line[index:].strip()
            state = "cocotb"
            lines =[sline]
            sys.stdout.write(line)
            
            continue
        else:
            sys.stdout.write(sline+"\n")    
            
    
        
"""        
'
    if isProblem(pline):
        if line.startswith("   ") :
            pline = pline[:len(pline)-1]+line[2:]
        else:
            pline=addTool(pline,tool)
#            sys.stdout.write("*"+str(debugSize())+pline)
#            sys.stdout.write(pline)
            if REMOVE_DUPS:
                lineHash=hash(pline)
                if not lineHash in dupLines:
                    dupLines.add(lineHash)
                    sys.stdout.write(pline)
#                    sys.stdout.write(": "+str(lineHash)+" : " +count(dupLines))
            else:
                sys.stdout.write(pline)    
            
            
            pline = line
    else:
        pline = line
"""