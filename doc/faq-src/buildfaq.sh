#!/bin/sh
# Builds the faq document from the LaTeX source

buildtex() {

	PREFIX=${1%%.tex}
	
	if [ ! -f $PREFIX.toc -o $PREFIX.toc -ot $1 ]; then
		pdflatex $1 ;
	fi
	pdflatex $1

	# Some fixes for latex2html:
        TEXNAME=$1
	if [ $PREFIX == faq_tr ]; then
		FILEENC=latin5
		TEXNAME="$1.orig"
		mv $1 $TEXNAME
		iconv -f utf-8 -t iso8859-9 $TEXNAME | sed 's/[[]utf8x]{inputenc}/[latin5]{inputenc}/' > $1 ;
	elif grep -c '[[]utf8x]{inputenc}' $1; then
		FILEENC=unicode ;
	elif grep -c '[[]cp1251]{inputenc}' $1; then
		FILEENC=koi8-r
		TEXNAME="$1.orig"
		mv $1 $TEXNAME
		iconv -f cp1251 -t koi8-r $TEXNAME | sed 's/[[]cp1251]{inputenc}/[koi8-r]{inputenc}/' > $1 ;
	else
		FILEENC=latin1,unicode,utf8 ;
	fi

	latex2html -no_subdir -split 0 -html_version 3.2,$FILEENC -no_navigation -prefix $PREFIX $1

	if [ $1 != $TEXNAME ]; then
		mv $TEXNAME $1
	fi
		

	if grep -c '\usepackage{ngerman}' $1; then # Fix for German quotes
		mv $PREFIX.html $PREFIX.html.orig
		sed -e 's/"`/„/g' -e "s/\"'/“/g" $PREFIX.html.orig > $PREFIX.html
	fi

#	Does not work as desired...
#	htlatex $1 "html,fn-in,unicode,3.2" 'unicode/!' '-p' 
	
	sed 's-/usr/share/latex2html/icons/footnote.png-footnote.png-' $PREFIX.html  > ../$PREFIX.html
	cp $PREFIX.pdf $PREFIX.css ..	
}

if [ -z $1 ]; then
	for F in *.tex; do
		PREFIX=${F%%.tex}
		if [ ! -f ../$PREFIX.pdf -o ../$PREFIX.pdf -ot $F -o ! -f ../$PREFIX.html -o ../$PREFIX.html -ot $F ]; then
			echo "Building PDF and HTML from $F ..."
			buildtex $F > $PREFIX-output.log 2>&1;
		else
			echo "Output of $F is up to date." ;
		fi ;
	done ;
else
	buildtex $1 ;
fi

