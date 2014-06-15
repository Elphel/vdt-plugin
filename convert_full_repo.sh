#!/bin/bash
################################################################################
# Copyright (c) 2014 Elphel, Inc.
# This file is a part of VDT plug-in.
# VDT plug-in is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# VDT plug-in is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
#  Additional permission under GNU GPL version 3 section 7:
# If you modify this Program, or any covered work, by linking or combining it
# with Eclipse or Eclipse plugins (or a modified version of those libraries),
# containing parts covered by the terms of EPL/CPL, the licensors of this
# Program grant you additional permission to convey the resulting work.
# {Corresponding Source for a non-source form of such a combination shall
# include the source code for the parts of Eclipse or Eclipse plugins used
# as well as that of the covered work.}
################################################################################
#TMPDIR="tmp"
#GITURL="https://github.com/Elphel/unmodified_veditor_1_2_0_clone.git"
#GITREPO="unmodified_veditor_1_2_0_clone"
#PATCH_FILE="vdt-veditor.patch"
INITIAL_DIRECTORY=`pwd`
FULL_REPO_URL="git@github.com:Elphel/vdt.git"
DERIVATIVE_REPO_NAME="vdt-plugin"
#create derivative repo at the same level as current
cd ../
cp -v -r "$INITIAL_DIRECTORY" "$DERIVATIVE_REPO_NAME"
cd "$DERIVATIVE_REPO_NAME"
echo "Removing git remote origin to preven accidental corruption of the original repository"
echo "You may set it late with \"git remote set-url origin <new-url>\""
git remote set-url origin ""

git filter-branch --force --index-filter 'git rm --cached --ignore-unmatch src/com/elphel/vdt/veditor/*' --prune-empty --tag-name-filter cat -- --all
git filter-branch --force --index-filter 'git rm --cached --ignore-unmatch -r _generated' --prune-empty --tag-name-filter cat -- --all
git filter-branch --force --index-filter 'git rm --cached --ignore-unmatch about_veditor.html' --prune-empty --tag-name-filter cat -- --all
git filter-branch --force --index-filter 'git rm --cached --ignore-unmatch src/com/elphel/vdt/core/launching/VDTErrorParser.java' --prune-empty --tag-name-filter cat -- --all
git filter-branch --force --index-filter 'git rm --cached --ignore-unmatch ChangeLogVeditor.txt' --prune-empty --tag-name-filter cat -- --all
git filter-branch --force --index-filter 'git rm --cached --ignore-unmatch CONTRIBUTORS_VEDITOR.txt' --prune-empty --tag-name-filter cat -- --all

echo "Modifying gitignore to include removed files/directories"
cat << EOF >> .gitignore
src/com/elphel/vdt/veditor
_generated
src/com/elphel/vdt/core/launching/VDTErrorParser.java
about_veditor.html
ChangeLogVeditor.txt
CONTRIBUTORS_VEDITOR.txt
EOF
cd "$INITIAL_DIRECTORY"
pwd
exit 0
