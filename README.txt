README for YajHFC 0.3.4
=======================

ABOUT
-----

YajHFC (Yet Another Java HylaFAX Client) is a client for the HylaFAX fax server
(http://www.hylafax.org/) written in Java.

Features:
* Faxing documents in PostScript or PDF format
* Polling faxes
* Support for generating cover pages from templates
* Viewing sent and received faxes
* Phone book
* Visible table columns may be selected in a dialog
* Supports four languages: English, French, German and Spanish

Homepage: http://www.yajhfc.de.vu/
email:    Jonas Wolz <jwolz@freenet.de>

INSTALLATION
------------

Requirements:
* JRE 5.0 or higher
* A running hylafax server in your network (of course ;-) )
* Some program to view TIFF files (also see the FAQ)
* Optional: A PostScript viewer (e.g. Ghostview)

To install YajHFC just download the yajhfc-0_3_4.jar file to some folder on your
file system.
To execute it, use: "java -jar yajhfc-0_3_4.jar"
(In Windows you usually can just double click the jar file as well.)

LICENSE
-------

YajHFC is free software licensed under the GPL.
See the file COPYING for details.

SOURCE CODE
-----------

For instructions describing how to build YajHFC from source
please see the file BUILDING.txt in this directory.

YajHFC uses the following packages:
(1) The gnu.hylafax library (core and inet-ftp package) 
    from http://gnu-hylafax.sourceforge.net/
(2) TableLayout.jar 
    from https://tablelayout.dev.java.net/
(3) "Java look and feel Graphics Repository" (jlfgr-1_0.jar) 
    from http://java.sun.com/developer/techDocs/hi/repository/

Copies of the required files from (2) and (3) can also be found in the jar 
subdirectory in the source archive.

YajHFC was originally written as a first more complicated
(than advanced "Hello world" programs) Java project using the Eclipse IDE.

CHANGES
-------

0.3.4:
- Support to specify recipients on the command line
- Suspend/Resume for fax jobs
- French translation added
- Initially displayed tab can be set by a command line argument
- The "stock" gnu.hylafax library is now used by default (which fixes some bugs)

0.3.3:
- Support for resending faxes
- Printing the tables with received/sent faxes supported
- Colored background for failed fax jobs
- Read only LDAP phone book support added
- Support for multiple open phone books
- Windows setup program with automatic "fax printer" installation

0.3.2:
- Bugfix for sporadical errors while sending faxes
- Support for launching a new instance in the background (useful for "fax printers")
- Some additional new commandline parameters
- Refresh button
- ant build file included in the source distribution
- documentation updates

0.3.1:
- Changeable Look&Feel
- Option to show new faxes in a viewer automatically
- UI option to change polling intervals of the HylaFAX server added
- A simple search dialog for the phone book has been added
- YajHFC should now also run with the "stock" version of the gnu.hylafax library (not very well tested!)

0.3.0:
- Added support to convert pictures to PostScript before submitting a fax job
- Added a "preview" button to the Send dialog
- Added support for phone books in SQL databases (using JDBC)

0.2.7a:
- Bugfix for a exception that occured when displaying "only own faxes"

0.2.7:
- Added Spanish translation
- Added a option to select the language 
- Support to add an offset to date values
- Fixed a bug causing wrong numbers to be displayed when sending
  multiple faxes

0.2.6:
- Small workaround for Windows 9x

0.2.5:
- Small bugfixes

0.2.4:
- Support for filters
- Support for admin mode

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

* More Translations? (see below)

YajHFC uses GNU gettext for translations, so it is easy to integrate
additional ones into the program.
For the translator creating a new translation (without translating the 
documentation) will require a few hours for the initial work and about
an hour for every new release. You don't need "advanced" technical
knowledge to do it (basically you only need to know how to work with a 
text editor, by installing specialized software this gets even easier).

So, if you would like to have YajHFC translated to your native language,
volunteers are always welcome. ;-)

