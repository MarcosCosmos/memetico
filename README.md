# About This Repository
A modified version of Memetico, with a GUI tool for visualisation and configuration. 

Modifications include:
- Refactoring
- The inclusion of a GUI which permits configuration, saving of performance and state logs which can be loaded for visualisation later, and displays candidate tours found by Memetico at intervals during runs.  
- An optional (configurable) extension which makes use of an external heuristic solver called Lin-Kernighan-Helsgaun [http://akira.ruc.dk/~keld/research/LKH](http://akira.ruc.dk/~keld/research/LKH). If enabled LKH will run, at the end of each generation, on the 'current' solution of every agent to try and find an improvement.
- An optional (configurable) extension which randomises the root agent's solutions every n-th generation in an attempt to avoid being stuck on local optima.

Note that although Memetico itself can be run without the GUI, the command line interface to Memetico is not up to date and does not correctly permit argument-based configured.

# About Memetico
Memetico is a Memetic Algorithm which searches for good solutions to ATSP (Asymmetric Travelling Salesman Problem) problems. It was originally developed by Luciana Buriol, Paulo M. França, & Pablo Moscato and is discussed in greater detailed in their paper:

A New Memetic Algorithm for the Asymmetric Traveling Salesman Problem
Journal of Heuristics, 2004, Volume 10, Number 5, Page 483
Luciana Buriol, Paulo M. França, Pablo Moscato

[https://doi.org/10.1023/B:HEUR.0000045321.59202.52](https://doi.org/10.1023/B:HEUR.0000045321.59202.52)

# Usage
This project makes extension use of gradle. The main (useful) gradle tasks are those for GUI module. E.g. Assuming gradle is configured correctly etc, you the 'run' task should open the application. This can be done via the the command line by typing:  
`./gradlew run` (or, on windows, `./gradle.bat`)

Once in the application, two buttons on right top of the main screen will enable configuration of the Memetico search ("Configuration"), and of the graphical display shown on the main screen ('Display Options').

## With LKH
As is explained in [About This Repository](#about-this-repository), this project's version of Memetico can make use of the Lin-Kernighan-Helsgaun solver as an external executable. This can only be used if the LKH executable can be found on one of the paths in the PATH environment variable, and a configuration option will appear if it is found.

## Creating a JAR
It is possible to create a jar for this application (and gradle already has a jar task). However, this will not include javafx by default, which will need to be configured accordingly (That is, javafx libs will need to be on the classpath (or modulepath(?))). Note that whilst it may be possible to create a "fat" jar which does include javafx, that is outside of the scope of these instructions.

# Dependencies
Dependancies gradle is already configured for:
- Gson, version ^2.8.5
- JavaFX (gradle is configured to include this via the plugin 'org.openjfx.javafxplugin', 0.0.8)

# License Information
The code in this project is licensed under MIT. A copy of this license can by found in the `LICENSE` file in the root directory of this project.

# Acknowledgements
- [Memetico](#about-memetico) (see the "About Memetico" section)
- [TSPLIB4J](https://github.com/dhadka/TSPLIB4J) - Original sourcefor the code included as the `tsplib4j` java package in this project, authored by David Hadka.

# TODO
- Create a CLI interface for fully configuring and executing Memetico from the command line (including logging).
- Possibly expand instructions to include screenshot examples.