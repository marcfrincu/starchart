# Starchart

Interactive starchart written in **Java** and using **JOGL** as graphics engine. While teaching at West University of Timisoara, Romania, I delivered a course on Computer Graphics (2007-2011, 2016-2020) focusing on OpenGL applications by using Java. This project grew over the years driven by my interest in simulations of the Universe and I have incorporated some of it in the practical work I asked students to do as part of their 2D graphics labs. Some of the material can still be found on [wikiversity](https://beta.wikiversity.org/wiki/Computer_graphics_--_2008-2009_--_info.uvt.ro/Laboratory_agenda).
The current version allows users to move around, zoom in/out, click objects and visualize data (2D for deep sky, and 3D for solar system objects).

- To move around use arrow keys
- To zoom in/out press 'i' and 'o'
- To show/hide constellation lines press 'l'
- To show/hide object names press 'h'
- To reset the scene press 'r'
- To move forward in time press '>'
- To move backward in time press '<'
- To exit the program press 'ESC'
- To show information about the planets click on them
- To show information on deep sky objects click in the left-bottom area of where their name appears on the map


# Disclaimer

The text in the application is in Romanian, but I think some of the planet names and astronomical units are similar to their English counterparts.
Over time I took code fragments (in some cases written in other languages like Basic or C) and datasets from various public sources. In some cases I forgot from where so I apologies in advance from that, no harm intended. In addition some websites are no longer accessible. Where I did wrote my source it can be found in the files.

# Requirements

The current working version uses the following:

- Java 1.6
- JOGL (see jars in the lib/ folder)
- jlibeps (see jar in the lib/ folder)

# Running

Compile the code. I used Eclipse for developing and maintaning it.

There are two main applications.

1. The 2D interactive polar projection map, basically a starchard. Run it by executing:

*java PolarProjectionMap*

2. The eps starmap generator (also for the entire sky in polar projection). Run it by executing:

*java starmap.StarMapGenerator*

**NOTE** in the StarMapGenerator's case see the corresponding java class for details as the actual line used to generate the eps file is commented out (see main method).
