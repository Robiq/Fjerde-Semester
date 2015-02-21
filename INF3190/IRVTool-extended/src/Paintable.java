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

public interface Paintable extends java.io.Serializable{
  public static final Color markc = Color.BLACK;
  public void paint(Graphics g);
  public boolean isMarked();
  public void setPosition(Point p);
  public void mark(boolean m);
  public boolean contains(Point p);
  public void delete();
  public void setScreen(Screen s);
  public int getAddress();
  public String toString();
}