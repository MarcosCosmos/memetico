#!/bin/python3
import sys
import os
try: 
    os.mkdir("fixed")
except e: 
    pass
with open(sys.argv[1], 'r') as src, open('fixed/' + sys.argv[1], 'w') as dst:
    on_coords = False
    for line in src.readlines():
        if line == "NODE_COORD_SECTION\n":
            on_coords = True
        elif on_coords:
            if line[0] in '0123456789':
                parts = line.split(' ')
                parts[0] = str(int(parts[0])+1)
                line = ' '.join(parts)
            else:
                on_coords = False
        dst.write(line)