#!/bin/bash
# Builds the YajHFC jar file

if [ -z `which javac` ]; then
	echo 'javac not found. Please make sure javac can be found in your PATH.'
	exit 1 ;
fi

if [ -z `which msgfmt` ]; then
	echo 'msgfmt not found. Please make sure you have gettext installed and that it can be found in your PATH.'
	exit 1 ;
fi

export CLASSPATH=$CLASSPATH:$PWD/jar:$PWD/../gnu-hylafax
export JAVAC="javac -target 1.5"

echo 'Compiling *.java files ...'
find yajhfc -name '*.java' -print0 | xargs --null $JAVAC

echo 'Compiling language files ...'

for PO in yajhfc/i18n/messages_*.po ; do
	LANG=${PO##*/messages_}
	LANG=${LANG%.po}
	echo "Using $PO for language $LANG"
	msgfmt --java2 -d. --resource=yajhfc.i18n.Messages --locale=$LANG $PO ;
done

echo 'Creating jar file ...'
cd jar
. ./jarup.sh > /dev/null

echo
echo 'Created yajhfc.jar in directory jar.'

