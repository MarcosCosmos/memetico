# Instructions

If using the LKH you will need to ensure that an LKH executable (http://www.akira.ruc.dk/~keld/research/LKH/) is available via the PATH environment

# TODO:
Add acknowledgements for Memetico etc

# misc links
- https://github.com/onlylemi/GeneticTSP (may be useful for some potential JS/web port of the display aspects in future)
- https://stackoverflow.com/questions/30679025/graph-visualisation-like-yfiles-in-javafx & https://github.com/sirolf2009/fxgraph (Note: this was previously but is no longer actively used, as a custom, canvas-based solution proved preferable)
- http://people.csail.mit.edu/fredette/tme/sun3-150-nbsd.html (relates to attempts at getting stilton/cheddar running)
- https://www.cs.bgu.ac.il/~benmoshe/DT/ - may be useful as a java implementation of the DT
# dependancies and acknowledgements
https://github.com/coin-or/jorlib - used currently for TSPLIB parsing; CPlex is listed as a dependancy for it, but it is not required for the parsing component; May replace with own implementation to detailed debug information.


# Misc Notes
In the tsplib parser: org.jorlib.io.tspLibReader.fieldTypesAndFormats.DisplayDataType.COORD_DISPLAY indicates that the display should be taken from the distanceTable, which should be converted to NodeCoordinates