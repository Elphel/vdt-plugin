VDT plugin
==========

Table of Contents                                                                                                                                                                                                                                                             
-----------------
                                                                                                                                                                                                                                                             
  * [VDT plugin documentation](#vdt-plugin-documentation)
  * [Instalation of VDT plugin and related programs](#instalation-of-vdt-plugin-and-related-programs)
    * [Installation of Xilinx tools](#installation-of-xilinx-tools)
    * [Programs and libraries installed from (K)ubuntu repositories](#programs-and-libraries-installed-from-kubuntu-repositories)
      * [GtkWave (3.3.58-1)](#gtkwave-3358-1)
      * [other programs needed for building Icarus Verilog](#other-programs-needed-for-building-icarus-verilog)
      * [Java JDK](#java-jdk)
    * [Icarus Verilog](#icarus-verilog)
    * [JavaCC](#javacc)
    * [Eclipse IDE](#eclipse-ide)
    * [Installation of VDT plugin itself](#installation-of-vdt-plugin-itself)
      * [Clone VDT plugin source code](#clone-vdt-plugin-source-code)
    * [Run VEditor installation/patch script from within the top directory of vdt-plugin](#run-veditor-installationpatch-script-from-within-the-top-directory-of-vdt-plugin)
      * [Import the VDT plugin project into the Eclipse workspace.](#import-the-vdt-plugin-project-into-the-eclipse-workspace)
      * [Configuring JavaCC (optional)](#configuring-javacc-optional)
      * [Building and running VDT](#building-and-running-vdt)
  * [Import and configuration of the sample project in VDT](#import-and-configuration-of-the-sample-project-in-vdt)
    * [Import x393 project](#import-x393-project)
    * [Configuration of VDT for x393 project](#configuration-of-vdt-for-x393-project)
      * [Configure access to the server with Xilinx tools](#configure-access-to-the-server-with-xilinx-tools)
      * [Copy unisims library to the local directory](#copy-unisims-library-to-the-local-directory)
      * [Patch primitive(s) to work with Icarus Verilog](#patch-primitives-to-work-with-icarus-verilog)
  * [Simulating x393 project with Icarus Verilog](#simulating-x393-project-with-icarus-verilog)
      
##VDT plugin documentation
Documentation is available in a separate [vdt_docs](https://github.com/Elphel/vdt-docs) repository.
Here are some [screenshots](https://github.com/Elphel/vdt-docs/blob/master/VDT-UserManualAddendum.pdf)
##Instalation of VDT plugin and related programs
VDT plugin for Eclipse is designed to integrate different tools for Verilog-based FPGA design.
Currently it supports only GNU/Linux operating system and tool specification file support
Icarus Verilog simulator and Xilinx ISE and Vivado Webpack design suites. It also works with Altera
Quartus tools for synthesising FPGA code for Altera devices.
Below is detailed step-by-step installation procedure for Kubuntu 14.04 LTS (other GNU/Linux systems
should not require significantly different installation). Latest tested versions of the programs
are also specified. 
### Installation of Xilinx tools
Xilinx Webpack software (free for download) needs to be installed on a GNU/Linux computer according
to the manufacturer recommendations on the same computer as the rest of the software or (preferably)
on a separate system. VDT uses ssh and rsync access to the server running tools, the server can be
in the same LAN or accessed over the WAN. Both ISE and Vivado tools are supported. 
### Programs and libraries installed from (K)ubuntu repositories
#### GtkWave (3.3.58-1)
```
sudo apt-get install gtkwave
```
#### other programs needed for building Icarus Verilog
```
sudo apt-get install autoconf gperf flex bison g++ zlib1g-dev libbz2-dev git
```
#### Java JDK
For most tasks JRE is sufficient, but if you would like to be able to modify and recompile Verilog
language parsing you will need java compiler that comes with the full JDK
```
sudo apt-get install default-jdk
```
### Icarus Verilog
Icarus Verilog has to be compiled from the source code that is available in git repository:
```
git clone git://github.com/steveicarus/iverilog.git
cd iverilog
sh autoconf.sh
./configure
```
Update: Removed instructions to modify Icarus code - current version does not need
them anymore, so just proceed with
```
make
sudo make install
```
Default VDT configuration assumes Icarus is installed to the /usr/local/bin

### JavaCC
Java compiler compiler is optional, as VDT provide both Verilog grammar source files
and generated Java files. It is needed only if you need to change and recompile these
files (for example adding support to not yet covered Verilog features).

[JavaCC]( https://java.net/projects/javacc) can be downloaded as
[javacc-6.0.zip]( https://java.net/projects/javacc/downloads/download/javacc-6.0.zip)
Unzip it in any convenient location - this location will be provided to Eclipse later.

### Eclipse IDE

You will need _Eclipse IDE for Java EE Developers_  (capable of plugin development),
latest tested version is Mars (eclipse-jee-mars-2-linux-gtk-x86_64.tar.gz). VDT plugin
is experimental and I would recommend to have a separate (from your other development)
installation. VDT uses modified version of [VEditor](http://sourceforge.net/projects/veditor/)
and any other versions of VEditor installed will conflict with VDT.

There are still issues with GTK3 (refresh of the windows content, animated icons), all seems
to work if Gtk3 is disabled (Gtk2 used instead) by adding the following 2 lines in the eclipse.ini file. 
```
--launcher.GTK_version
2
```
are added just before line
```
--launcher.appendVmargs.
```

Additionally a fix is required to make menu tooltips visible (https://github.com/dirruk1/gnome-breeze/issues/7#issuecomment-109325330) :
«go to system settings > color > options and make sure "apply colors to non-Qt colors" is switched off, then log out and back in and see if the colors are normal. The tooltips are not supposed to have a light background.» 

### Installation of VDT plugin itself

VDT plugin uses modified VEditor plugin for Eclipse and because of the license incompatibility
(Eclipse Public License v1.0 for VEditor and GNU General Public License v3.0+ for VDT plugin)
it is not possible to distribute a pre-compiled version (.jar file), so the plugin code has to be
merged (using provided script) and compiled/built as Eclipse plugin project.
####Clone VDT plugin source code
```
git clone git@github.com:Elphel/vdt-plugin.git
```
###Run VEditor installation/patch script from within the top directory of vdt-plugin
```
./install_and_patch_veditor.sh
```
This will clone the original VEditor source tree as tmp/unmodified_veditor_1_2_0_clone,
re-organize files to match VDT plugin code tree, apply patch and copy the produced files
to VDT project locations (most under src/com/elphel/vdt/veditor and _generated), these
files/directories are listed in .gitignore . When VEditor-related part of the VDT code
will be changed (and so the vdt-veditor.patch) you will need to run
./install_and_patch_veditor.sh again

####Import the VDT plugin project into the Eclipse workspace.
At this stage I hit GTK bug that caused Eclipse to crash, working solution is described
in https://bugs.kde.org/show_bug.cgi?id=339174 :
 For oxygen, edit the normally already existing file
 **/usr/share/themes/oxygen-gtk/gtk-2.0/gtkrc** and change **GtkComboBox::appears-as-list = 1**
 into **GtkComboBox::appears-as-list = 0**
```
File->Import->Git->Projects from Git->Existing local repository-> Select directory where you cloned VDT
Import Existing Projects (wizard selection)
```
Keep both **parsers** and **vdt** checked and press **Finish**

####Configuring JavaCC (optional)
In the **Project Explorer** window, expand the **vdt** project folder, right-click
the **buildjavacc.xml** file and select **Properties**.
In the new dialog window select **Run/Debug Settings**, press **New** and agree to
the only suggested option: **Ant Build**.

Then Select **Environment** tab and enter two variables by pressing **New** and then

Name: **ECLIPSE_HOME**

Value: Folder path that contains Eclipse executable

Name: **JAVACC_HOME**

Value: Folder path where **javacc-6.0.zip** was unpacked (ending with /javacc-6.0).

####Building and running VDT
In the "Project Explorer" window, expand the 'vdt' project folder and double-click (open)
the **plugin.xml** file.

Select the **Overview** tab at the bottom of the appeared window.

Under the **Testing** label, click the **Launch an Eclipe application** link.

You may also use "Run Eclipse Application" (green triangle) or "Debug Eclipse Application"
(green bug) buttons on Eclipse toolbar to launch
A new instance of Eclipse will open, this new Eclipse will have VDT plugin activated.
You may minimize the original Eclipse window at this point (it can be used to monitor
and fix plugin errors). Next you may create a new FPGA development project or import
an existing one. We will use DDR3 memory interface project as an example.
## Import and configuration of the sample project in VDT
Sample project is a DDR3 memory interface for Xilinx Zynq SOC that does not depend on
undocumented featuers and encrypted modules and can be simulated with the Free Software
tools.
### Import x393 project
You may already have it if you installed other software for Elphel NC393 camera development, if that
is the case you can skip the next step and use ~/git/elphel393/fpga-elphel/x393/
```
git clone https://github.com/Elphel/x393.git
cd x393
./INIT_PROJECT
```
The last command copies Eclipse .project and .pydevproject files to the working directory.

From the Eclipse instance that runs VDT plugin (not the one with the VDT source code)
use the same steps as for importing VDT plugin code (described above):  
```
File->Import->Git->Projects from Git->Existing local repository-> Select directory where you cloned x393
Import Existing Projects (wizard selection)
```
Keep both **x393** and **py393** checked and press **Finish**

### Configuration of VDT for x393 project
The cloned x393 project does not include Verilog modules of Xilinx primitives that are
required even for simulation of the design. The required library (unisims) is included
with the Xilinx Vivado software and the proprietary license does not allow to redistribute
it. VDT provides means to copy this library from your Vivado installation to the project,
So for the next step you need Xilinx software to be installed on the same or different
computer running GNU/Linux.

Open the top module (x393.v) in the Editor (or any othe Verilog file of the project)

Open "Verilog/VHDL' perspective:
```
Window->Open Perspective->Other->Verilog/VHDL
```
It should look as shown on screenshots in [VDT-UserManualAddendum.pdf](https://github.com/Elphel/vdt-docs/blob/master/VDT-UserManualAddendum.pdf?raw=true),
with bottom-left panel showind "Design Menu" and FPGA-related tools

#### Configure access to the server with Xilinx tools
In the "Design menu" panel select "Package" icon, it will open a dialog with "Xilinx server setup"
tab active.

If you have Xilinx tools installed on the same computer as VDT, leave the default value for
*Remote Host IP* (localhost) and *Remote user name* (your current login name).

If you are using phisically different computer - change the both fields as needed.

You may also change Vivado Release to the current one, installation directory (if different from the
default) and configure same parameters for Xilinx ISE if you plan to use it (VDT supports both)

Next you need to setup password-less access to the tools server based on the key pairs:

Generate ssh key (if you do not have it already). Use command line tool or expand
*Vivado Tools* in the design menu, right-click *Start remote Vivado session* and select
*Generate public key* (or use a key icon on the Design menu toolbar)

Send this key to the server - you may either use a command-line program *ssh-copy-id* or right-click
*Start remote Vivado session* and select *Setup connection to user@server* (tools icon on the toolbar).
This operation requires you to enter the password for the server and this requires a separate program
to be installed, you can do this with
```
sudo apt-get install ssh-askpass
```
Update: In Kubuntu 16.04 ssh-askpass is not required, there is a similar program available in base
installation. Just pay attention that the first pop-up window will ask not for the password, but for
"yes".

If ssh will not find *ssh-askpass* or a similar program, it will fail and Eclipse console output will
output the resolution suggestions.

With ssh-askpass a separate dialog window will open, likely the first question will be not the password
itself, but your permission to connect to an unknown host, so just enter *yes* there.

If everything was configured correctly you may try opening remote Vivado session (later it will
happen automatically when needed):

Right-click *Start remote Vivado session* and select *Launch Vivado* (door with entering green arrow icon
on the toolbar)

If everything is correct, in Eclipse console you will see
``` 
puts "@@FINISH@@"
```
and a few secods later server response ending with
```
@@FINISH@@
```
@@FINISH@@ sequence is just a marker to know server successfully finished the requested command
*Start remote Vivado session* shold now show pulsating green dot to the right of it and the console is
open for both VDT communication and you can also manually enter TCL commands as covered in Xilinx Vivado
manuals.
#### Copy unisims library to the local directory
```
Vivado Tools -> Vivado utilities -> Copy Vivado primitives library to the local project
```
#### Patch primitive(s) to work with Icarus Verilog
Some of the Xilinx primitives can not be simulated correctly with Icarus Verilog, we will add more patches
when we'll hit particular problems, for x393 only one file needs to be patched - OSERDESE1.v

Run patch command from the unisms subdirectory of the x393 project :  
```bash
patch -p1 < ../unisims_patches/OSERDESE1.diff
```
In x393_sata project this command will be
```bash
patch -p1 < ../x393/unisims_patches/OSERDESE1.diff
```
After new files are added the project needs to be refreshed - you may click on the project name in the navigator window
and press F5 key or right-click -> Refresh

There are many files in the library so refreshing make take a while.

## Simulating x393 project with Icarus Verilog
```
Design Menu -> Verilog Development tools -> Icarus Verilog simulator
```
If you get many errors "Unknown module type", you may need to refresh the project (press F5) after adding unisims library
files.

If everything will work correctly, Icarus will compile and simulate the design (some warnings in the beginning are not fixed yet). After that GTKWave will open the simulation results.

In the case of problems you may get more verbose output in the console if you right-click on the 
*Icarus Verilog Simulator*, select *Tool parameters*, open *Options tab*  and check *Show output
with no errors/warnings*
