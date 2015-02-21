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

import javax.swing.*;
import java.awt.*;

public class RIPVariableEditPane extends EditPane{
  private JTextField inf = new JTextField(7);
  private JTextField gc = new JTextField(7);
  private JTextField timer = new JTextField(7);
  
  
  public RIPVariableEditPane(){
    this.title = "RIP-Variables";
    this.setLayout(new GridLayout(3, 2));    
    this.add(new JLabel("GARBACE_COLLECTION_TIMER ", JLabel.RIGHT));
    this.add(gc);
    this.add(new JLabel("INFINITY ", JLabel.RIGHT));
    this.add(inf);
    this.add(new JLabel("TIMER ", JLabel.RIGHT));
    this.add(timer);
    gc.setText(Consts.GC_TIMER+"");
    inf.setText(Consts.INF+"");
    timer.setText(Consts.TIMER+"");
  }
  
  public boolean save(){
    if(validateInput()){
      Consts.GC_TIMER = new Integer(gc.getText()).intValue();
      Consts.INF = new Integer(inf.getText()).intValue();
      Consts.TIMER = new Integer(timer.getText()).intValue();
      return true;
    }
    return false;
  }
  
  private boolean validateInput(){
    try{
      int i = new Integer(inf.getText()).intValue();
      if(i < 1 || i >= Integer.MAX_VALUE)
        return false;
      i = new Integer(gc.getText()).intValue();
      if(i < 1)
        return false;
      i = new Integer(timer.getText()).intValue();
            if(i < 1)
        return false;
    } catch(NumberFormatException nfe){
      return false;
    }
    return true;
  }
}