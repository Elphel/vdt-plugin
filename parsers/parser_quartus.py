#!/usr/bin/env python
# -*- coding: utf-8 -*-
# Copyright (C) 2015, Elphel, Inc.
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

import sys
import argparse
import re

# Global variables
g_topModule = ""
g_toolName = ""
g_mode = ""
g_includeMsgId = True
# Lines starting with these markers are of lines of interest
g_msgTypeMarkers = ("Info", "Warning", "Error", "Critical Warning", "Extra Info")
# Command line paramerters, these names should be in sync with those that are passed to the script in XML files (quartus_proto.xml probably)
g_optNames = ["top_module", "tool_name", "parser_mode", "include_msg_id"]

# Search patterns
patternMsgType = re.compile(".*?:")
patternMsgId = re.compile("[0-9]+")
patternFileLine = re.compile("([^ ]+)[ ]?\(([0-9]+)")
patternFileNameFull = re.compile("File:(.*)Line:")

class MessageContainer:
	"""Helper container for parsed message line"""
	msgType = ""
	msgId = 0
	msgText = ""
	fileName = ""
	fileNameFull = ""
	lineNumber = 0

	#Private parameters
	__discardId = True

	def discardId(self, value = None):
		if value == None:
			return self.__discardId
		else:
			self.__discardId = value

def getParameters():
	"""Extract parameters from command line arguments"""
	global g_topModule
	global g_toolName
	global g_mode
	global g_includeMsgId

	parser = argparse.ArgumentParser()
	for opt in g_optNames:
		parser.add_argument("--{}".format(opt))
	args = parser.parse_args()
	if args.top_module:
		g_topModule = args.top_module
	if args.tool_name:
		g_toolName = args.tool_name
	if args.parser_mode:
		g_mode = args.parser_mode
	if args.include_msg_id:
		if args.include_msg_id == "true":
			g_includeMsgId = True
		else:
			g_includeMsgId = False

def isProblem(line):
	"""Check if the line contains meaningful information"""
	global g_msgTypeMarkers
	retVal = False
	strippedLine = line.strip()
	for msgType in g_msgTypeMarkers:
		if strippedLine.startswith(msgType):
			retVal = True
			break
	return retVal

def getMsgId(line, msg):
	"""Extract message ID from log line an set coresponding msg field
	line - text string to be parsed
	msg - instance of MessageContainer class
	return: instance of MessageContainer class
	"""
	matchedText = patternMsgId.search(line)
	if matchedText:
		msgId = line[matchedText.start() : matchedText.end()]
		msg.msgId = int(msgId)
	return msg

def getMsgType(line, msg):
	"""Extract message type from log line an set coresponding msg field
	line - text string to be parsed
	msg - instance of MessageContainer class
	return: log line without message type and instance of MessageContainer class
	"""
	strippedLine = line.strip()
	matchedText = patternMsgType.search(strippedLine)
	if matchedText:
		msgType = strippedLine[matchedText.start() : matchedText.end() - 1]
		msg = getMsgId(msgType, msg)
		if msg.msgId != 0:
			#remove message ID
			msg.msgType = msgType.split('(')[0].strip()
			msg.discardId(False)
		else:
			msg.msgType = msgType
	return (strippedLine[matchedText.end() + 1:], msg)

def getFileLine(line, msg):
	"""Extract file name and line number from log line an set coresponding msg fields
	line - text string to be parsed
	msg - instance of MessageContainer class
	return: log line without extracted information
	"""
	matchedLine = patternFileLine.search(line)
	if matchedLine and len(matchedLine.groups()) == 2:
		msg.lineNumber = int(matchedLine.group(2))
		msg.fileName = matchedLine.group(1)
		#remove part of the string preceding file name as well as trailing braket and colon
		#line = line[matchedLine.end(2) + 2:]
	return (line, msg)

def getFilePath(line, msg):
	"""Extract full file name and line number from log line an set coresponding msg fields
	line - text string to be parsed
	msg - instance of MessageContainer class
	return: log line without extracted information
	"""
	matchedLine = patternFileNameFull.search(line)
	if matchedLine:
		msg.fileNameFull = matchedLine.group(1).strip()
		line = line[:matchedLine.start() - 1]
	return (line, msg)

def getMsgText(line, msg):
	msg.msgText = line.strip()
	return ("", msg)

def filterMessage(msg):
	"""Decide whether this message should be redirected to output or filtered out"""
	#just a stub
	return True

def assembleLine(msg):
	"""Assemble and return output line"""
	if not g_includeMsgId or msg.discardId():
		formatStr = "{0}: {2} [{3}:{4:04d}]\n"
	else:
		formatStr = "{0}: [{1}] {2} [{3}:{4:04d}]\n"
	if msg.fileName =="":
		problemMarker = g_toolName
	else:
		problemMarker = msg.fileName
	line = formatStr.format(msg.msgType,
			msg.msgId,
			msg.msgText,
			problemMarker,
			msg.lineNumber
			)
	return line

if __name__ == "__main__":
	getParameters()
	for line in iter(sys.stdin.readline, ''):
		if isProblem(line):
			msg = MessageContainer()
			processedLine, msg = getMsgType(line, msg)
			processedLine, msg = getFilePath(processedLine, msg)
			processedLine, msg = getFileLine(processedLine, msg)
			processedLine, msg = getMsgText(processedLine, msg)
			if filterMessage(msg):
				logLine = assembleLine(msg)
				sys.stdout.write(logLine)
