#!/bin/bash
# Builds all po files

if [ -z `which javac` ]; then
	echo 'javac not found. Please make sure javac can be found in your PATH.'
	exit 1 ;
fi

if [ -z `which msgfmt` ]; then
	echo 'msgfmt not found. Please make sure you have gettext installed and that it can be found in your PATH.'
	exit 1 ;
fi

echo 'Compiling language files ...'

# Make sure we compile for JDK 1.5
export JAVAC="javac -target 1.5"

for PO in messages_*.po ; do
	LANG=${PO##*messages_}
	LANG=${LANG%.po}
	echo "Using $PO for language $LANG"
	msgfmt --java2 -d../.. --resource=yajhfc.i18n.Messages --locale=$LANG $PO ;
done

