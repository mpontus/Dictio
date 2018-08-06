#!/usr/bin/env python3
# Interpose files by taking one line from each file serially
import sys
from itertools import zip_longest

fp = map(open, sys.argv[1:])

for lines in zip_longest(*fp):
    for line in lines:
        if line is not None:
            sys.stdout.write(line)
