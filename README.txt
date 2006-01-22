README for YajHFC 0.2.3
=======================

ABOUT
-----

YajHFC (Yet Another Java HylaFAX Client) is a client for the HylaFAX fax server
(http://www.hylafax.org/) written in Java.

Features:
* Faxing documents in Postscript or PDF format
* Polling faxes
* Support for generating cover pages from templates
* Viewing sent and received faxes
* Phone book
* Visible table columns may be selected in a dialog
* Supports two languages: English and German

Homepage: http://www.yajhfc.de.vu/
email:    Jonas Wolz <jwolz@freenet.de>

INSTALLATION
------------

Requirements:
* JRE 5.0 or higher
* A running hylafax server in your network (of course ;-) )
* Some program to view TIFF files (also see FAQ.txt for this)
* Optional: A Postscript viewer (e.g. Ghostview)

To install YajHFC just download the yajhfc-0_2_3.jar file to some folder on your
file system.
To execute it, use: "java -jar yajhfc-0_2_3.jar"
(In Windows you usually can just double click the jar file as well.)

LICENSE
-------

YajHFC is free software licensed under the GPL.
See the file COPYING for details.

SOURCE CODE
-----------

YajHFC uses the following packages:
(1) A slightly modified version of the gnu.hylafax package 
    (originally from http://www.net-foundry.com/java/gnu/hylafax/)
    The source code of the modified version can be downloaded
    from the YajHFC home page.
(2) TableLayout.jar 
    from https://tablelayout.dev.java.net/
(3) "Java look and feel Graphics Repository" (jlfgr-1_0.jar) 
    from http://java.sun.com/developer/techDocs/hi/repository/

Copies of the required files can also be found in the jar 
subdirectory in the source archive.

YajHFC was originally written as a first more complicated
(than advanced "Hello world" programs) Java project using the Eclipse IDE.

CHANGES
-------
0.2.3:
- Support for multiple numbers/files for sent faxes
- Forwarding/saving faxes
- Internal changes

0.2.2:
- Added support for "window to front"/beep on new faxes

0.2.1:
- Added read/unread status for received faxes
- Phonebooks are now sorted
- Some internal changes

0.2:
- Added fax cover support
- Added support for polling

TODO
----

The following features might be added in the future:

* Support for phone books in SQL databases (via JDBC)

