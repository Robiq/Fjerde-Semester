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

public class OSPFExchangePackage extends irvtool.Package{
  private static Image image = new ImageIcon(OSPFExchangePackage.class.getResource("icons/expack.png")).getImage();
  private boolean initialize = false;
  private boolean master = false;
  private int[][][] des = null;

  public OSPFExchangePackage(int from, int to, int area){
    super(from, to, T_OSPF_EXCHANGE, M_UNUSED, "OSPF_EXCHANGE-Package");
  }

  public Image getImage(){
    return image;
  }

  public void setMaster(boolean master){
    this.master = master;
  }

  public boolean isMaster(){
    return this.master;
  }

  public void setInit(boolean init){
    this.initialize = init;
  }

  public boolean isInit(){
    return this.initialize;
  }

  public void setDescription(int[][][] des){
    this.des = des;
  }

  public int[][][] getDescription(){
    return this.des;
  }

  public Object clone(){
    OSPFExchangePackage p = new OSPFExchangePackage(this.from(), this.to(), -1);
    p.setMaster(this.isMaster());
    p.setInit(this.isInit());
    p.setDescription((int[][][])this.getDescription().clone());
    return p;
  }

  public String toString(){
    String result = "source = "+this.from()+"; destination = "+this.to()+"; master = "+this.master+
      "; init = "+this.initialize+"\n";
    for(int i = 0; i < des.length; ++i){
      for(int j = 0; j < des[0].length; ++j){
        result += des[i][j]+"\t";
      }
      result += "\n";
    }
    return result;
  }

 /* public static void main(String[] argv){
    OSPFExchangePackage p = new OSPFExchangePackage(1, 2);
    int[][] test = {{1, 2, 3, 4, 5}, {2, 3, 4, 5, 6}, {3, 4, 5, 6, 7}};
    p.setDescription(test);
    System.out.println(p);
    System.out.println(p.clone());
  } */
}
