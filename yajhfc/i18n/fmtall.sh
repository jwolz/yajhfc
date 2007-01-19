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
	echo 'msgfmt not found. Please make sure you have gettext installed and that it can be found in your PATH.'
	exit 1 ;
fi

# Make sure we compile for JDK 1.5
export JAVAC="javac -target 1.5"

if [ -z "$1" ]; then
  echo 'Compiling language files ...'
  
  for PO in messages_*.po ; do
  	LANG=${PO##*messages_}
  	LANG=${LANG%.po}
  	echo "Using $PO for language $LANG"
  	msgfmt --java2 -d../.. --resource=yajhfc.i18n.Messages --locale=$LANG $PO ;
  done ; 
else 
  LANGFILE="messages_$1.po"
  if [ -f $LANGFILE ]; then
    echo "Compiling $LANGFILE..."
    msgfmt --java2 -d../.. --resource=yajhfc.i18n.Messages --locale=$1 $LANGFILE ;
  else
    echo "No PO file for language $1 found." ;
  fi ;
fi

