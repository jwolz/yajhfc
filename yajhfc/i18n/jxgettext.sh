#!/bin/sh
# Extracts all translatable strings into messages.po

xgettext -k_ --from-code=utf-8 ../*.java ../filters/*.java ../phonebook/*.java ../phonebook/jdbc/*.java

