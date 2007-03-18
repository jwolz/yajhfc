#!/bin/sh
# Creates a source archive in the parent directory of the yajhfc tree

cd ../..
rm -f yajhfc.zip
zip -r yajhfc.zip yajhfc/  -x 'yajhfc/build/*' -x 'yajhfc/yajhfc/*.class' -x 'yajhfc/jar/yajhfc.jar' -x 'yajhfc/doc/faq-src/*'  -x '*/CVS/*' -x '*~'
zip yajhfc.zip yajhfc/doc/faq-src/.[!.]* yajhfc/doc/faq-src/*.tex yajhfc/doc/faq-src/*.sh yajhfc/yajhfc/i18n/*.class

