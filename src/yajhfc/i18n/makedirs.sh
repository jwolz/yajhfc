#!/bin/bash
# Moves the translation into directories named after the language code

if [ -z $1 ]; then
  TARGETDIR=/tmp/yajhfc-translation
else
  TARGETDIR=$1
fi

if [ ! -d $TARGETDIR ]; then
  mkdir -p $TARGETDIR
fi

# Usage copyfile FILE
copyfile() {
	PO=$1
  	LANG=${PO##*_}
  	LANG=${LANG%.po}
	
	if [ ! -d $TARGETDIR/$LANG ]; then
	  mkdir $TARGETDIR/$LANG 
	fi

	cp $PO $TARGETDIR/$LANG/
}

for PO in messages_*.po CommandLineOpts_*.po UIDefaults_*.po ; do
	copyfile $PO
done 

if [ ! -d $TARGETDIR/templates ]; then
  mkdir $TARGETDIR/templates
fi
cp messages.po $TARGETDIR/templates/messages.pot
cp UIDefaults.po $TARGETDIR/templates/UIDefaults.pot
perl maketemplate.pl < CommandLineOpts.po > $TARGETDIR/templates/CommandLineOpts.pot

if [ ! -d $TARGETDIR/en ]; then
  mkdir $TARGETDIR/en
fi
cp CommandLineOpts.po $TARGETDIR/en






