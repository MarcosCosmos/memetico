# NOTE
Check permission to use the libraries with src distribution enforcing licenses

# misc links
https://github.com/onlylemi/GeneticTSP
https://stackoverflow.com/questions/30679025/graph-visualisation-like-yfiles-in-javafx & https://github.com/sirolf2009/fxgraph

# dependancies and acknowledgements
https://github.com/coin-or/jorlib - used as a starting point for TSPLIB parsing; CPlex is listed as a dependancy for it, but it is not for just parsing; May replace with own implementation to detailed debug information.


# Misc Notes

In the tsplib parser: org.jorlib.io.tspLibReader.fieldTypesAndFormats.DisplayDataType.COORD_DISPLAY indicates that the display should be taken from the distanceTable, which should be converted to NodeCoordinates