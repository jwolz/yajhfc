#!/bin/bash
# Builds po files
# Usage: 
# - With no parameters: build all files
# - With a single parameter: Build the po file for the requested language

if [ -z `which javac` ]; then
	echo 'javac not found. Please make sure javac can be found in your PATH.'
	exit 1 ;
fi

if [ -z `which msgfmt` ]; then
	echo 'msgfmt not found. Please make sure you have GNU gettext installed and that it can be found in your PATH.'
	exit 1 ;
fi

# Usage compile FILE CLASSNAME
compile() {
	PO=$1
  	LANG=${PO##*_}
  	LANG=${LANG%.po}
	if [ $LANG != ${PO%.po} ]; then
		CLASSOUT="$2_$LANG.class"
		CLASSOUT1="$2_$LANG\$1.class"
		LOCALEARG="--locale=$LANG" 
  		echo -n "Using $PO for language $LANG: "
	else
		CLASSOUT="$2.class"
		CLASSOUT1="$2\$1.class"
		LOCALEARG="" 
  		echo -n "Using $PO for default language: "
	fi
	
	# Check if recompilation is necessary
	if [ $CLASSOUT -nt $PO -a $CLASSOUT1 -nt $PO ]; then
		echo "Output is up to date." ;
	else
		echo "Compiling..."
	  	msgfmt --java2 -d../.. --resource=yajhfc.i18n.$2 $LOCALEARG $PO ;
	fi ;
}

# Usage compile FILE PROPERTYNAME
catprops() {
	PO=$1
  	LANG=${PO##*_}
  	LANG=${LANG%.po}
	if [ $LANG != ${PO%.po} ]; then
		PROPOUT="$2_$LANG.properties"
  		echo -n "Using $PO for language $LANG: "
	else
		PROPOUT="$2.properties"
  		echo -n "Using $PO for default language: "
	fi
	
	# Check if recompilation is necessary
	if [ $PROPOUT -nt $PO ]; then
		echo "Output is up to date." ;
	else
		echo "Creating .properties..."
		# Convert to properties and strip comments
		msgcat --properties-output $PO | ( echo '# This file is auto-generated. Do not edit' ; grep -v '^#\|^$' ) > $PROPOUT
	fi ;
}

# Make sure we compile for JDK 1.5
export JAVAC="javac -target 1.5"

if [ -z "$1" ]; then
  echo 'Compiling language files ...'
  
  for PO in messages_*.po ; do
	compile $PO Messages
  done ;

  # Create .properties as this gives smaller files here
  for PO in CommandLineOpts*.po ; do
	catprops $PO CommandLineOpts
  done ;

  for PO in UIDefaults_*.po ; do
	catprops $PO UIDefaults
  done ;

else 
  LANGFILE="messages_$1.po"
  if [ -f $LANGFILE ]; then
    echo "Compiling $LANGFILE..." 
    # Always compile if the language is explicitely given
    msgfmt --java2 -d../.. --resource=yajhfc.i18n.Messages --locale=$1 $LANGFILE ;
  else
    echo "No PO file for language $1 found." ;
  fi ;
fi

