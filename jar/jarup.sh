#!/bin/sh

DIR=jar
GNU_HYLAFAX_DIR=../../gnu-hylafax

JARFILE=$PWD/yajhfc.jar

rm -f $JARFILE
cd ..

#jar cvfm $DIR/yajhfc.jar $DIR/manifest.txt *.class i18n/*.class *.png -C $DIR @$DIR/classlist -C $DIR toolbarButtonGraphics
jar cvfm $JARFILE $DIR/manifest.txt yajhfc/*.class yajhfc/i18n/*.class yajhfc/*.png COPYING README*.txt doc/*.html doc/*.css yajhfc/faxcover/*.class yajhfc/faxcover/*.ps yajhfc/filters/*.class yajhfc/phonebook/*.class

cd $DIR
find info -follow -name '*.class' > classlist
find toolbarButtonGraphics -name '*.gif' >> classlist
jar uvf $JARFILE @classlist 
rm -f classlist

cd $GNU_HYLAFAX_DIR
find gnu -name '*.class' > classlist
jar uvf $JARFILE @classlist 
rm -f classlist

