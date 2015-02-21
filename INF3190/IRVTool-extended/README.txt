Author:       Christian Sternagel (csac3692@uibk.ac.at)
Program:      IRV-Tool version 1.0
LincenseInfo: Open Source (GPL)
Date:         2004-01-12

Copyright (C)

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston,
MA 02111-1307 USA

TABLE OF CONTENTS:
1. Introduction
2. Installation
 2.1 Getting Started
3. Description
 3.1 A Simple Example
 3.2 The User Interface
 3.3 Editing Options
  3.3.1 Hosts
  3.3.2 Routers
  3.3.3 Connections
 3.4 Saving A Topology As A Bitmap
 3.5 Options
  3.5.1 RIP - Routing Information Protocol
   3.5.1.1 Settings
   3.5.1.2 RIP Variables
  3.5.2 OSPF - Open Shortest Path First
   3.5.2.1 Settings
   3.5.2.2 OSPF Variables
 3.6 Simulating With The IRV-Tool
 3.7 The Terminal
  3.7.1 Running Script Files
4. Bibliography


The IRV (Internet Routing Visualization) Tool:

1.      Introduction
======================

The IRV-Tool allows to visualize routing-settings which could occur
e.g. in the internet. Via the tool it is possible to setup network
topologies with arbitrary complexity in a simple way. Furthermore it
approves to accomplish various simulations of routing-behavior with
two different types of routing-protocols (RIP of the distance vector
protocol family and OSPF of the link state protocol family). 

The use of the IRV-Tool is easily acquired - thus with minimal effort
the basics of routing can be learned by students.

The following sections describe how to install and use the IRV-Tool
(developed by me for my baccalaureate-project).


2.      Installation
======================

The IRV-Tool can be downloaded as a tar-archive labeled
'irvtool.tar.gz' ore as a zip-archive 'IRVTool.zip'. To extract the
archive-file use:

 $ tar -xzf irvtool.tar.gz
 resp.
 $ unzip IRVTool.zip
 
Using windows only double-click on the zip-file to extract its
contents.

Now a jar-archive ('irvtool.jar'), a documentation (irvt-doku.pdf), a
simple example ('simpleexample.irv'), a batch-file ('irvtool.bat')
for Windows as well as a shell-script ('irvtool.sh') for UNIX, a
directory ('license') containing license information ('gpl.txt'), a
directory ('src') in what the sourcecode (*.java-files) can be found
and this README-file are created in a new directory called 'IRVTool'.

To start the tool type:

 UNIX
 $ chmod u+x irvtool.sh   # to make 'irvtool.sh' executable
 $ irvtool.sh             # to start the IRV-Tool
 or
 $ java -jar irvtool.jar  # if the above command does not work

 Windows
 > irvtool.bat            REM on older versions
 or
 > irvtool.jar            REM WindowsXP
 or
 > java -jar irvtool.jar  REM if none of the above commands works
 
In windows it is also possible to execute the Tool by clicking on
'irvtool.bat' (older versions) or 'irvtool.jar' (WindowsXP).


3.      Description
=====================


3.1     A Simple Example
==========================

Let us assume a network-topology like

     5
    / \
 1-3   4-2
    \ /
     6

where 1 and 2 are hosts; 3, 4, 5 and 6 are routers and all
connections except [4,6] (the link connecting router 4 to router 6)
have a cost of 1. The connection [4,6] has a cost of 2. By means of
this simple topology the basics of RIP should be shown. At first the
routing tables contain the following data:
 ________________________________________________________________
| Router-3:                     |  Router-4:                     |
| destination  cost  connection |  destination  cost  connection |  
|   3            0     local    |    4            0     local    |    
|----------------------------------------------------------------  
| Router-5:                     |  Router-6                      |
| destination  cost  connection |  destination  cost  connection |
|   5            0     local    |    6            0     local    |
 ----------------------------------------------------------------
 
As we see at this stage the routers only know about themselves. After
the first RIP-Update (in the order: 3, 4, 5, 6) the tables have
changed to:
 ________________________________________________________________
| Router-3:                     |  Router-4:                     |
| destination  cost  connection |  destination  cost  connection |  
|   3            0     local    |    4            0     local    |    
|   1            1     [1,3]    |    2            1     [2,4]    |
|   5            1     [3,5]    |    5            1     [4,5]    |
|   6            1     [3,6]    |    6            2     [4,6]    |
|----------------------------------------------------------------  
| Router-5:                     |  Router-6                      |
| destination  cost  connection |  destination  cost  connection |
|   5            0     local    |    6            0     local    |
|   1            2     [3,5]    |    1            2     [3,6]    |
|   2            2     [4,5]    |    2            3     [4,6]    |
|   3            1     [3,5]    |    3            1     [3,6]    |
|   4            1     [4,5]    |    4            2     [4,6]    |
 ----------------------------------------------------------------

After the second RIP-Update at last, the tables contain following
data:
 ________________________________________________________________
| Router-3:                     |  Router-4:                     |
| destination  cost  connection |  destination  cost  connection |  
|   3            0     local    |    4            0     local    |    
|   1            1     [1,3]    |   !2            1     [2,4]    |
|   5            1     [3,5]    |    5            1     [4,5]    |
|   6            1     [3,6]    |    6            2     [4,6]    |
|  !2            3     [3,5]    |    1            3     [4,6]    |
|   4            2     [3,5]    |    3            2     [4,5]    |
|----------------------------------------------------------------  
| Router-5:                     |  Router-6                      |
| destination  cost  connection |  destination  cost  connection |
|   5            0     local    |    6            0     local    |
|   1            2     [3,5]    |    1            2     [3,6]    |
|  !2            2     [4,5]    |    2            3     [4,6]    |
|   3            1     [3,5]    |    3            1     [3,6]    |
|   4            1     [4,5]    |    4            2     [4,6]    |
|   6            2     [3,5]    |    5            2     [3,6]    |
 ----------------------------------------------------------------

Now there exists a path from Host-1 to Host-2 via 3-5-4 (as
highlighted by exclamation marks). This path is chosen at Router-3
because of its cumulative cost of 3 whereas the path 3-6-4 has a
cumulative cost of 4 thus it is too expensive. At equal cost the
order of updates determines the result.
 The above example can be visualized with the IRV-Tool. For that
purpose the file 'simpleexample.irv' is needed (if you don't want to
design the given topology by yourself). By simulating that file you
can also observe how RIP responds to a modification in the topology.
In 'simpleexample.irv' the connection [3,5] goes down from second 50
to second 70.


3.2     The User Interface
============================

In terms of the IRV-Tool a network consists of hosts (symbol:
computer with screen), routers (symbol: tower) and connections
(symbol: network cable) which connect two nodes respectively. Here,
the term node denotes hosts as well as routers. Hosts can only
possess a single connection to a single router and cannot be
connected among each other. Routers can possess arbitrary connections
to arbitrary nodes.
 The toolbar on the left hand side serves to design and edit a
topology quikly. There are following control elements (top down):

 - Marker-tool:     Used to select (and afterwards delete with DEL) a
                    single node or connection or to move several 
                    nodes together with all connections attached to
                    them.
 - Remove-tool:     Used to remove several nodes and all connections
                    attached to them by enframing them with pressed
                    left mouse button and releasing the button when
                    ready.
 - Host-tool:       Used to place a host on the drawing area.
 - Router-tool:     Used to place a router on the drawing area.
 - Connection-tool: Used to connect two nodes to each other.
 - Edit-tool:       Used to look at and/or modify properties of any
                    entity (node or connection). Instead of the edit-
                    tool the right mouse button can be used as well.

The toolbar below controls all facets of simulations within the
IRV-Tool. By modifying 'speed' the execution speed can be adjusted.
At 'timeline' the current point in time is recorded (with seconds as
unit). By modifying this value and clicking 'apply' the simulation
can be moved to the given point in time (WARNING: going backwards in
time is not possible, because the simulation is not deterministic
concerning user input). Clicking 'play' starts a simulation. Within a
simulation RIP-packets are red and standard IP-packets are green.
'pause' pauses a simulation and 'stop' aborts it while newly 
initializing all routing-tables (thus it is necessary to reopen a
used *.irv-file for receiving table-entries as stored to the file).


3.3     Edit Options
======================


3.3.1   Hosts
===============

The host-edit-dialog allows to modify the address (only unambigous
addresses are valid), the destination to which packets are sent (0
means 'no destination'), the content of messages from this host
(default: 'hallo'), the acceleration time for sending IP-packets
(preconditioned that a destination was set) and the periodicity
(every 'cylce' seconds) of these sendings. Moreover the connection to
which this host is connected is listed.


3.3.2   Routers
=================

The router-edit-dialog allows to modify the address, the acceleration
time for updates (-1 means 'never') and their periodicity (only for
RIP; RIP-default is 30 seconds). Also the routing-table of this
router can be observed and modified. 


3.3.3   Connections
=====================

The connection-edit-dialog allows to modify the address and the cost
(default value: 1). Via the cost the distance between to nodes is
calculated. If there are two nodes connected by a single connection
with cost 5, then these nodes are 5 units away from each other
(unless there is another path between these two nodes with minor
cost). Additionally there is a list of tuples of points in time in the
manner (MIN, MAX) for each connection. Within this list any number of
entries can be stored, indicating from which point in time (MIN) to
which other (MAX) this connection should be down (interrupted
connections are displayed dashed). Connections that are down behave
in a manner as if they do not exist.


3.4     Saving A Topology As A Bitmap
=======================================

By choosing 'Save As ...' from the file-menu and adjusting the
combobox 'Files of Type:' to '*.jpg files' it is possible, to save a
topology as a bitmap (JPEG-format). It must be pointed out that the
size (width and height in pixels) of the bitmap depends on the
portion of the network visible on the screen. In other words the
whole visible canvas (the widget on which the painting is done) is
saved as bitmap. Thus by changing the size of the main-window it is
possible to control the size of the resulting bitmap.


3.5     Options
=================

There are several options intended for controlling the exact
behavior of routing in a given network (topology).
 First of all there are to routing-protocols that can be chosen.


3.5.1   RIP - Routing Information Protocol
============================================

This is the first and simpler protocol that can be chosen. More
exhaustive information on RIP can be achieved from [1] and [2] or in
minor complexity from the documentation of the IRV-Tool. At this
point it is worth mentioning that RIP is a member of the
distance-vector-protocol-family (DVP). Thus a distributed algorithm
is used to calculate information about 'best' paths. Information is
spread across the network by sending distance-vectors (DV) to direct
neighbors. These DVs consist of triples [to, cost, via], one for each
destination node.

3.5.1.1 Settings
==================

Currently implemented are three optional features as extension of RIP
all based on existing features described in [1] and [2]. Thereby it
concerns notably:

 - Triggered Updates
 - Split Horizon
 - Split Horizon with poisonous Reverse


3.5.1.2 RIP Variables
=======================

There are also three variables that can be set for the RIP. By name:

 - The Garbage-Collection-Timer: Number of seconds an entry of a
   routing table should be kept after it has exceeded its validity.
 - Infinity: The number that should be treated as infinity.
 - Timer: Number of seconds a newly registered entry is valid without
   further acknowledgements.
   
After starting the IRV-Tool all these variables are set to default
values like mentioned in [1] and [2].


3.5.2   OSPF - Open Shortest Path First
=========================================

The second protocol is the OSPF protocol. As OSPF is a member of the
link-stat-protocol-family (LSP) every node holds a table (the so
called 'link state table') containing information about the whole
topology. That is the reason why every node can calculate the best
path locally. OSPF is much more complex than RIP. Changes in the
topology are advanced by a flooding-protocol that is part of OSPF
and sends link-state-advertisments (LSA) across the network.


3.5.2.1 Settings
==================

There is only one feature currently implemented:

 - Equal-cost Multipath (see [3]): This feature makes it possible
   to use more than one path if there are multiple paths between
   source and destination that have the same cost.


3.5.2.2 OSPF Variables
========================

Four variables can be set for OSPF. By default they have values like
in [3]. The variables are:

 - Hello-Interval: Number of seconds between checks for new or lost
   Neighbors.
 - Maximal-Age: The maximal age in seconds of a table entry befor it
   is discarded.
 - Maximal-Age-Difference: If an LSA with same sequencenumbers as the
   existing LSA arrives at a node it is always accepted in the case
   its age equals Maximal-Age. If the ages of the LSAs differ by more
   than Maximal-Age-Difference the LSA with the smaller age is
   considered as most recent otherwise the old one is kept.
 - Infinity: This number is used in the lollipop-algorithm (see [2]).
 
 
3.6     Simulating With The IRV-Tool
======================================

After designing a topology by arranging nodes and connection on the
'Canvas' a simulation can be started by pressing the 'Play' button.
With 'Pause' it is possible to break a simulation and later resume
it. And 'Stop' at last discards a simulation by setting all routing
tables to their default values and the time to zero. To make a
simulation faster it is possible to go forward in time by choosing
the desired point in time and clicking 'Apply'.
 The clock (in the lower right corner of the IRV-Tool) can be
switched between two modes by clicking on it. By default it shows the
elapsed time in seconds. But it is also possible to show the time in
the format 'hh:mm:ss'.

ATTENTION: After clicking 'Stop' all routing tables are empty, so it
is necessary to reload a *.irv-file (by opening it from File->Open)
to get the assignments of the routing tables that were saved to the
file.


3.7     The Terminal
======================

The Terminal is more in its test stage than completed nevertheless
all described commands are fully functional (besides 'sleep' which
only works correct if you let run the simulator with animation but
not if you jump to a point of time by using 'Apply'). Some things
that are very tedious without the Terminal (e.g. configuration of
multiple nodes and connections) can be easily achieved by using the
Terminal.
 First of all the Terminal only responds to user input if a
simulation has been started (The 'Play' button was pressed without
pressing 'Stop' afterwards).
 By using the Arrow-Keys (Arrow-Up and Arrow-Down) it is possible to
access the command-history of the Terminal (UNIX shell alike).
 Following commands are supported:
 
 - broadcast: you have to be logged in at a router
   Syntax: broadcast
   When using RIP the RIP-Update of the current router is sent to
   all adjacent nodes.
   When using OSPF an OSPF-Hello-packet is sent to each adjacent
   router.
 - clear:
   Syntax: clear
   Clears the output-TextField of the IRV-Tool.
 - echo: 
   Syntax: echo <text>
   Displays the specified text (terminated with a newline).
 - exit:
   Syntax: exit
   Closes the IRV-Tool.
 - help:
   Syntax: help
   Displays a list of all commands.
 - login:
   Syntax: login <address-of-a-node>
   Some commands can only be used if you are logged in at a node.
   When logged in a node, the prompt shows the address of this node.
   E.g. '[1]$'.
 - logout:
   Syntax: logout
   After logout, you are not logged in any node. The prompt shows
   '[none]$'.
 - ls (list):
   Syntax: ls <address-of-an-entity>
   All properties of an entity (node or connection) are listed.
 - ping: you have to be logged in
   Syntax: ping <address-of-a-node>
   A Ping-Packet is sent from the node you are logged in to the node
   specified by <address-of-a-node>. The destination returns a
   Pong-Packet to the sender and a message is displayed on the
   output-TextField.
 - route:
   Syntax: route <address-of-a-router>
   Displays the routing-table of the specified router.
 - run:
   Syntax: run <path-of-script-file>
   Executes the specified script-file (see 3.7.1).
 - send: you have to be logged in
   Syntax: send <address-of-a-node> [<message>]
   Sends a packet from the current node to the node specified. The
   optional parameter <message> specifies a text-string to send.
   This String is displayed when the packet arrives at the
   destination.
 - set: used for nodes when logged in, otherwise for connections
   Syntax for connections: 
    set <addr> {address | clear | cost | down | remove} [<value>]
     address ... set the address of connection <addr> to <value>
     clear   ... clear the list of link-failures from the connection
     cost    ... set the cost of the connection to <value>
     down    ... add a link-failure to the list where <value> is a
                 pair of timestamps (separated by a blank) that
                 specify from when to when the failure should occure
     remove  ... remove a link-failure specified by a pair of
                 timestamps
   Syntax for routers:
    set address <new-addr>
   Syntax for hosts:
    set {address | cycle | destination | message | start} <value>
      Sets the specified property of the current host to the value
      <value>.
 - sleep:
   Syntax: sleep <seconds>
   Should only be used in script-files.
 - traceroute: you have to be logged in
   Syntax: traceroute <address-of-a-node>
   Like the UNIX command 'traceroute'. A list of all nodes situated
   on the path between source and destination is displayed.
   

3.7.1   Running Script Files
==============================

With the 'run' command it is possible to execute ascii-files
holding Terminal-commands. In addition there are some more options
('#' starts a comment until the end of the line):

- repeat:
  Syntax: repeat <times>
          # arbitrary code
          ;;
  Repeats the code between <times> and ';;' <times> times.
- for:
  Syntax #1: for all {connections | hosts | nodes | routers}
             # arbitrary code
             ;
  Repeats the code between the line with the for-command and ';'
  for all nodes (or connections or hosts ...; as specified). Within
  the for-block, the keyword 'this' can be used to access the curent
  entity.
  Syntax #2: for {connections | hosts | nodes | routers} <address-list>
             # code
             ;
  Where <address-list> is a (blank separated) list of addresses.
  
For can be used within repeat, but not vice versa. To make the use of
script-files more clearly here is an example.

  01 # this is a comment
  02 repeat 10
  03   for all routers
  04     broadcast
  04   ;
  05   sleep 10
  06 ;;

The above example shows a script that causes all routers to 
broadcast 10 times in succession and make a pause of 10 seconds
between each time.
 Here another example:
 
 01 clear
 02 for all connections
 03   echo i am connection this
 04   ls this
 05 ;
 06 for routers 2 4 6
 07   echo i am router this
 08   route this
 09 ;

Here all connections display their names and their properties after
clearing the screen. Then the routers 2, 4 and 6 display their
routing tables.


4       Bibliography
=====================

[1] C. Hedrick, RFC 1058 - Routing Information Protocol. Rutgers
    University, 1988.

[2] C. Huitema, Routing in the Internet, 2nd ed. Prentice Hall, 2000.

[3] J. Moy, RFC 2328 - OSPF Version 2. Ascend Communications, Inc.,
    1998.
    