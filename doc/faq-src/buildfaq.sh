#!/bin/sh
# Builds the faq document from the LaTeX source

buildtex() {

	PREFIX=${1%%.tex}
	
	if [ ! -f $PREFIX.toc -o $PREFIX.toc -ot $1 ]; then
		pdflatex $1 ;
	fi
	pdflatex $1

	if grep -c '[[]utf8x]{inputenc}' $1; then
		FILEENC=unicode ;
	else
		FILEENC=latin1,unicode,utf8 ;
	fi

	latex2html -no_subdir -split 0 -html_version 3.2,$FILEENC -no_navigation $1
	
	if grep -c '\usepackage{ngerman}' $1; then # Fix for German quotes
		mv $PREFIX.html $PREFIX.html.orig
		sed -e 's/"`/„/g' -e "s/\"'/“/g" $PREFIX.html.orig > $PREFIX.html
	fi


	cp $PREFIX.pdf $PREFIX.html $PREFIX.css ..	;
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

