#!/bin/sh
# Builds the faq document from the LaTeX source

buildtex() {

	PREFIX=${1%%.tex}
	
	pdflatex $1

	if grep -c '[[]utf8x]{inputenc}' $1; then
		FILEENC=unicode ;
	else
		FILEENC=latin1,unicode,utf8 ;
	fi

	latex2html -no_subdir -split 0 -html_version 3.2,$FILEENC -no_navigation $1

	cp $PREFIX.pdf $PREFIX.html $PREFIX.css ..	;
}

if [ -z $1 ]; then
	for F in *.tex; do
		buildtex $F ;
	done ;
else
	buildtex $1 ;
fi

