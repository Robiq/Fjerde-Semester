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

import java.awt.*;
import java.util.*;

public interface Computer extends Paintable{
  public static int num = 0;
  public static final int DIAM = 40;
  public Point getPosition();
  public boolean connect(Connection c);
  public void disconnect(Connection c);
  public void setAddress(int addr);
  public int getAddress();
  public int getDiam();
  public void receive(Package p);
  public void shape(Graphics g, boolean delete);
  public ArrayList getConnections();
  public long getStart();
  public int getCycle();
  public void send();
  //public Queue getInQueue();
  public void send(int to, String msg);
  public void send(int to, String msg, int ttl);
  public String toString();
}
