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

public interface RoutingTable extends java.io.Serializable{
  public void check(long time);
  public int getGateTo(int to); 
  public Package getPackageTo(int to);
  public void init();
  public void update(int[][] dv, int from);
  public String toString();
  public boolean add(int dest, int cost, int gate);
//  public void setINF(int gate);
  public String[][] getTable();
  //public int[][] remove(int row); // Removed because of different implementations for OSPF Multiple Metrics feature
  public void add(int[] values);
  public void add(int[][] values);
  public void clear();
  public void setAddress(int addr);
  public void setStatus(int flag);
}
