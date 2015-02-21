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

public class OSPFHelloPackage extends irvtool.Package{
  private static Image image = new ImageIcon(OSPFHelloPackage.class.getResource("icons/hellopack.png")).getImage();
  //private int[] neighbors = new int[0];

  public OSPFHelloPackage(int from, int to){
    super(from, to, T_OSPF_HELLO, M_UNUSED, "OSPF_HELLO-Package");
  }

  public Image getImage(){
    return image;
  }

  public Object clone(){
    return new OSPFHelloPackage(this.from(), this.to());
  }

/*
  public void setNeighbors(ArrayList n){
    this.neighbors = new int[n.size()];
    for(int i = 0; i < n.size(); ++i){
      this.neighbors[i] = ((Integer)n.get(i)).intValue();
    }
  }

  public int[] getNeighbors(){
    return this.neighbors;
  }
  */
}
