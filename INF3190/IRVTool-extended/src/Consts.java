/*
IRV-Tool
An Internet Routing Visualization Tool.

Copyright (C) 

This program is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software 
Foundation; either version 2 of the License, or (at your option) any later 
version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY 
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this 
program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, 
Suite 330, Boston, MA 02111-1307 USA

author: Christian Sternagel
mail:   csac3692@uibk.ac.at

*/
package irvtool;

public class Consts{
  public static final int STEPS = 10;
  private static int delay = 50;
  public static int INF = 16;
  public static int GC_TIMER = 120;
  public static int TIMER = 180; /* max time in seconds without update */
  public static int OSPF_INF = Integer.MAX_VALUE;
  public static int HELLO_INTERVAL = 9;
  public static int MAXAGE = 60*60;
  public static int MAXAGEDIFF = 15*60;
  public static int INVERSEMETRICMAX = 65535; // needed for Multiple Metrics, because throughput
  // and reliability: the greater, the better! (unlike delay and cost)
  public static int OSPFNUMBEROFMETRICS = 4; // number of metrics, if using Multiple Metrics feature
  public static final int OSPFMULTIPLEMETRICS = 1;
  public static int OSPFMULTIPLEAREAS = 2;

  public static synchronized int delay(){
    return delay;
  }

  public static synchronized void setDelay(int d){
    delay = d;
  }
}
