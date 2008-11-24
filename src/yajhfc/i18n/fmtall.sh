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

# Make sure we compile for JDK 1.5
export JAVAC="javac -target 1.5"

if [ -z "$1" ]; then
  echo 'Compiling language files ...'
  
  for PO in messages_*.po ; do
  	LANG=${PO##*messages_}
  	LANG=${LANG%.po}
	CLASSOUT="Messages_$LANG.class"
	CLASSOUT1="Messages_$LANG\$1.class"

  	echo -n "Using $PO for language $LANG: "
	
	# Check if recompilation is necessary
	if [ $CLASSOUT -nt $PO -a $CLASSOUT1 -nt $PO ]; then
		echo "Output is up to date." ;
	else
		echo "Compiling..."
	  	msgfmt --java2 -d../.. --resource=yajhfc.i18n.Messages --locale=$LANG $PO ;
	fi ;
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

