#!/bin/sh
# Extracts all translatable strings into messages.po

find .. -name '*.java' | xgettext -k_ --from-code=utf-8 -f-

