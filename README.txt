README for YajHFC 0.5.6
=======================

ABOUT
-----

YajHFC (Yet Another Java HylaFAX Client) is a client for the HylaFAX fax server
(http://www.hylafax.org/) written in Java.

Features:
* Faxing documents in PostScript, PDF and various other formats
* Polling faxes
* Support for generating cover pages from templates
* Viewing sent and received faxes
* Phone book (entries can optionally be read from SQL databases or LDAP directories)
* Visible table columns may be selected in a dialog
* Supports ten languages: Chinese, English, French, German, Greek, Italian, Polish, Russian, Spanish and Turkish

Homepage: http://www.yajhfc.de/
email:    Jonas Wolz <info@yajhfc.de>

INSTALLATION
------------

Requirements:
* JRE 5.0 or higher (Java 6 or 7 recommended)
* A running HylaFAX server in your network (of course ;-) )
* Some program to view TIFF files (also see the FAQ)
* Optional: A PostScript viewer (e.g. Ghostview), GhostScript
     and tiff2pdf (from libtiff-utils)

To install YajHFC just download the yajhfc-0_5_6.jar file to some folder on your
file system.
To execute it, use: "java -jar yajhfc-0_5_6.jar"
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
(4) Apache Commons Logging from http://commons.apache.org/logging/
    as a dependency of (1)

Copies of the required files from (2), (3) and (4) can also be found in the jar 
subdirectory in the source archive.

YajHFC was originally written as a first more complicated
(than advanced "Hello world" programs) Java project using the Eclipse IDE 
(and has gone a long way since that... ;-) )

CHANGES
-------

0.5.7:
- Bug fixes for phone book: Can now handle identical items, multi-select no longer "kills" entries
- Changed behaviour of "Filter from fax number": Specified characters are now removed completely instead of being replaced by spaces
- Some parameters (e.g. number and time to send) of jobs in the sending queue can now be edited

0.5.6:
- Added @@subject@@-Tag for text extraction (i.e. you can now also set the fax's subject by using a tag)
- Several small bug fixes and enhancements

0.5.5:
- Support to use native libtiff in the PDF plugin (offers JBIG support and better compatibility with not 100% clean TIFFs)
- Support to read received faxes from AVM Fritz!Box routers (outgoing faxes are still sent over HylaFAX)
- Tag name for text extraction now is configurable
- New translation: Chinese simplified (zh_CN). 
- Several minor enhancements and bug fixes


0.5.4:
- New batch printer and mailer plugin
- Several minor enhancements and bug fixes

0.5.3:
- Added "Print report"-feature for sent and received faxes
- Windows launcher is now an exe instead of vbs (based on patched launch4j)
- Several minor enhancements and bug fixes


0.5.2:
- Added PDF plugin
- Custom sort order for phone books
- Custom display styles for phone books
- Named pipe printer support for Win32
- Support to extract recipients from documents
- Different behaviour for resend fax and multi select (send window called individually for each selected fax)
- Some code cleanup, minor enhancements and bug fixes


0.5.1:
- Added console only add on
- Changed URLs from yajhfc.berlios.de to www.yajhfc.de
- Some bugfixes and minor feature enhancements

0.5.0:
- Support for local caching of the fax lists, probably making the start-up of 
the application feel much quicker
- Experimental support for direct reading to the recvq and doneq directory, 
bypassing the HylaFAX server (and the HylaFAX user authentication). This 
might lead to a lower server load with large queues since only modified files 
are read to refresh the fax lists. This still needs some testing, however... 
(any feedback on this feature is very welcome!)
- Options dialog should open a bit faster than before
- "Test connection" button in the Options dialog
- Support for multiple servers
- Support for multiple identities
- Lists of faxes can be saved in CSV, HTML or XML format
- Support for configurable keyboard accelerators

0.4.4:
- Improved MAC OS support (mostly cosmetic changes)
- Support to enter custom file converters
- Access to advanced settings over UI
- Support to print phone books
- Improved printing of faxes
- Support for @@CCNameAndFax@@ Tag in HTML cover pages
- Resend fax for multiple recipients
- Log console to view the log live
- Separation of view and send format for faxes
- User-editable list of modems
- new "override-setting" command line parameter
- Several bug fixes

0.4.3:
- Chinese translation added
- "View log" feature for sent faxes
- Tray notification message can be turned off
- Filters for Phone book items
- "Extended resolution" support (i.e. USEXVRES=yes)
- New command line parameter: --modem
- Support for TCP/IP and named pipe virtual printer port
- Support for default/override configuration in /etc/yajhfc
- Several bug fixes
- RPM and DEB packages for YajHFC are available now

0.4.2a:
Fix for a bug which caused settings not being saved when no old settings file exists.

0.4.2:
- Polish translation added
- Support to read recipients from text files
- Desired window state can be specified on the command line
- "Send only mode" without display of main window (when a 
  document to send and neither --background nor --noclose is specified)
- Arbitrary HylaFAX options can be specified for new fax jobs
- Work around for a Java bug causing YajHFC not saving its settings when it is 
  still running while the user logs off on Windows 7, Vista and (sometimes) XP
- Default cover page is now HTML to avoid problems with non ISO-8859-1 characters
- The Windows setup will now optionally install tiff2pdf and GhostScript
- Various other bug fixes, improvements and code cleanup

0.4.1:
- Support for distribution lists in the phone book (XML+JDBC only)
- Support for CSV phone books (e.g. for import/export)
- Support to set the "archive flag" (doneop) for sent faxes
- Quick search bar for faxes in the main window
- Some bug fixes

0.4.0:
- Greek translation added
- Support to manually answer a phone call (like faxanswer)
- Update checker
- Support for archive directory
- Support to view and send faxes as a single file (in PDF, TIFF or PS format)
- Phone book includes most common fields now
- Support for a tray icon under Java 6 
- Improved options dialog
- Improved command line support (YajHFC can now be 
   used to send faxes without user confirmation)
- Some simple obfuscation for passwords
- Lots of internal code clean up

0.3.9:
- Italian and Turkish translations added
- "Quick search" for phone books
- Lots of bug fixes and small improvements 

0.3.8a:
- Bug fix for XML phone books
- Some minor UI improvements

0.3.8:
- Improved phone book window with better support for multiple phone books
- Support to save the read/unread state of faxes in a central database table
- Graphical panel to add JDBC drivers and plugins
- Russian translation of YajHFC added

0.3.7:
- New simplified send dialog
- Support for cover pages in HTML format
- Support for cover pages in XSL:FO and ODT format using a plugin
- Better plugin support
- Logging changed to use the Java logging APIs
- Now uses gnu.hylafax 1.0.1
- Some bug fixes

0.3.6:
- Some bug fixes
- Support for selection of multiple phone book entries for the send dialog
- New command line argument ("--logfile") to log to a file in debug mode
- Support for "disconnected mode" as a possible workaround to some HylaFAX bugs
- Support to select the modem to use

0.3.5:
- Some UI improvements/"polishing" (e.g. progress bar for the fax list and more icons)
- Removed some restrictions on visible columns 
  (e.g.: the job ID does no longer have to be visible)
- New fax actions now are executed only *after* the faxes are no longer "in progress"

0.3.4a:
- Bug fix release: 
  In 0.3.4 an extra character was appended when a fax was submitted by
  standard input to an already running instance

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

