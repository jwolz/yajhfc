#!/bin/sh
# Format .po file $1 for locale $2 correctly

if [ $# -lt 2 ]; then
	echo 'Usage: jmsgfmt file locale'
	exit 1 ;
fi

msgfmt --java2 -d../.. --resource=yajhfc.i18n.Messages --locale=$2 $1

