#!/bin/sh
# Checks the available translations:

for PO in *_*.po ; do
    echo "Checking $PO:"
    msgfmt --statistics --check $PO
    echo "----------"
    echo
done

