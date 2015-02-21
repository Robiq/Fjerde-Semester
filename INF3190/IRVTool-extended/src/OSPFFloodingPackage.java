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
import javax.swing.*;

public class OSPFFloodingPackage extends irvtool.Package{
  private int[][][] dv;
  private static Image image = new ImageIcon(OSPFFloodingPackage.class.getResource("icons/floodpack.png")).getImage();
  private int stamp;

  public OSPFFloodingPackage(int from, int to, int area, int[][][] dv, int number){
    super(from, to, T_OSPF_FLOODING, M_UNUSED, "OSPF_FLOODING-Package");
    this.dv = dv;
    this.stamp = number;
  }

  public int[][][] getDescription(){
    return this.dv;
  }

  public Image getImage(){
    return image;
  }

  public int getNumber(){
    return this.stamp;
  }

  public Object clone(){
    return new OSPFFloodingPackage(this.from(), this.to(), -1, (int[][][])this.getDescription().clone(), this.getNumber());
  }
}
