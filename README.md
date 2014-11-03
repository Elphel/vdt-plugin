##VDT plugin documentation
Documentation is available in a separate [vdt_docs](https://github.com/Elphel/vdt-docs) repository.
##Instalation of VDT plugin and related programs
VDT plugin for Eclipse is designed to integrate different tools for Verilog-based FPGA design.
Currently it supports only GNU/Linux operating system and tool specification file support
Icarus Verilog simulator and Xilinx ISE and Vivado Webpack design suites.
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
sudo apt-get install autoconf gperf flex bison g++ zlib1g-dev libbz2-dev
```
#### Java JDK
For most tasks JRE is sufficient, but if you would like to be able to modify and recompile Verilog
language parsing you will need java compiler that comes with the full JDK
```
sudo apt-get install openjdk-7-jdk
```
### Icarus Verilog
Icarus Verilog has to be compiled from the source code that is available in git repository:
```
git clone git://github.com/steveicarus/iverilog.git
cd iverilog
sh autoconf.sh
./configure
```
If you run ```make``` with unmodified code it will not be able to simulate the test project -
[DDR3 Memory Interface](http://blog.elphel.com/2014/06/ddr3-memory-interface-on-xilinx-zynq-soc-free-software-compatible/)
assert in vvp is triggered by the Micron DDR3 memory model. I do not understand what exactly
is wrong but just disabling these assert statements in
[vpi_vthr_vector.cc](https://github.com/steveicarus/iverilog/blob/master/vvp/vpi_vthr_vector.cc)
allow vvp to proceed without any visible problems:

```c++
vpiHandle vpip_make_vthr_vector(unsigned base, unsigned wid, bool signed_flag)
{
    struct __vpiVThrVec*obj = new __vpiVThrVec;
    if (base >= 65536)
//      assert(base < 65536);
        fprintf(stderr, "vvp error: base > 65535, base= 0x%x\n",base);
    obj->bas = base;
    if (wid >= 65536)
//      assert(wid < 65536);
        fprintf(stderr, "vvp error: wid > 65535, wid= 0x%x\n",wid);
    obj->wid = wid;
    obj->signed_flag = signed_flag? 1 : 0;
    obj->name = vpip_name_string("T<>");
    return obj;
}
```
Then with the modified code
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
latest tested version is Luna 
(eclipse-jee-luna-SR1-linux-gtk-x86_64.tar.gz). VDT plugin is experimental and I would
recommend to have a separate (from your other development) installation. VDT uses
modified version of [VEditor](http://sourceforge.net/projects/veditor/) and any other
versions of VEditor installed will conflict with VDT.

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

A new instance of Eclipse will open, this new Eclipse will have VDT plugin activated.
You may minimize the original Eclipse window at this point (it can be used to monitor
and fix plugin errors). Next you may create a new FPGA development project or import
an existing one. We will use DDR3 memory interface project as an example.


  
