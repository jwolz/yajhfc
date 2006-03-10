#!/bin/sh

DIR=jar

rm -f yajhfc.jar
cd ..

#jar cvfm $DIR/yajhfc.jar $DIR/manifest.txt *.class i18n/*.class *.png -C $DIR @$DIR/classlist -C $DIR toolbarButtonGraphics
jar cvfm $DIR/yajhfc.jar $DIR/manifest.txt yajhfc/*.class yajhfc/i18n/*.class yajhfc/*.png COPYING README.txt FAQ.txt yajhfc/faxcover/*.class yajhfc/faxcover/*.ps yajhfc/filters/*.class

cd $DIR
find gnu info -follow -name '*.class' > classlist
find toolbarButtonGraphics -name '*.gif' >> classlist
jar uvf yajhfc.jar @classlist 
rm -f classlist

