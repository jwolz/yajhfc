#!/bin/bash
# Builds all po files

echo 'Extracting Strings ...'

. ./jxgettext.sh

echo 'Merging language files ...'

for PO in messages_*.po ; do
	echo $PO
	msgmerge -N -U $PO messages.po
done

# Create a template without translations:
CLO_TMP=CommandLineOpts.po.tmp
perl maketemplate.pl < CommandLineOpts.po > $CLO_TMP
for PO in CommandLineOpts_*.po ; do
	echo $PO
	msgmerge -N -U $PO $CLO_TMP
done
rm -f $CLO_TMP

