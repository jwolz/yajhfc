#!/bin/sh
# Builds the faq document from the LaTeX source

for F in faq*.tex; do

	PREFIX=${F%%.tex}
	
	pdflatex $F

	latex2html -no_subdir -split 0 -html_version 3.2,latin1,unicode,utf8 -no_navigation $F

	cp $PREFIX.pdf $PREFIX.html $PREFIX.css ..	;
done

