#!/bin/bash
# Builds all po files

echo 'Extracting Strings ...'

. ./jxgettext.sh

echo 'Merging language files ...'

for PO in messages_*.po ; do
	echo $PO
	msgmerge -N -U $PO messages.po
done

