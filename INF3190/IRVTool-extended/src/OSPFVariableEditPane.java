/*
ITV-Tool
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

public class OSPFVariableEditPane extends EditPane{
  private JTextField helloInt = new JTextField(7);
  private JTextField inf = new JTextField(7);
  private JTextField maxage = new JTextField(7);
  private JTextField maxdiff = new JTextField(7);
  
  public OSPFVariableEditPane(){
    this.title = "OSPF-Variables";
    this.setLayout(new GridLayout(4, 2));
    this.add(new JLabel("HELLO_INTERVAL ", JLabel.RIGHT));
    this.add(helloInt);
    this.add(new JLabel("INFINITY ", JLabel.RIGHT));
    this.add(inf);
    this.add(new JLabel("MAXIMAL_AGE ", JLabel.RIGHT));
    this.add(maxage);
    this.add(new JLabel("MAX_AGE_DIFF ", JLabel.RIGHT));
    this.add(maxdiff);
    helloInt.setText(Consts.HELLO_INTERVAL+"");
    inf.setText(Consts.OSPF_INF+"");
    maxage.setText(Consts.MAXAGE+"");
    maxdiff.setText(Consts.MAXAGEDIFF+"");
  }
  
  public boolean save(){
    if(validateInput()){
      Consts.HELLO_INTERVAL = new Integer(helloInt.getText()).intValue();
      Consts.OSPF_INF = new Integer(inf.getText()).intValue();
      Consts.MAXAGE = new Integer(maxage.getText()).intValue();
      Consts.MAXAGEDIFF = new Integer(maxdiff.getText()).intValue();
      return true;
    }
    return false;
  }
  
  private boolean validateInput(){
    try{
      int i = new Integer(helloInt.getText()).intValue();
      if(i < 1 || i >= Integer.MAX_VALUE)
        return false;
      i = new Integer(inf.getText()).intValue();
      if(i < 1)
        return false;
      i = new Integer(maxage.getText()).intValue();
      if(i < 1)
        return false;
      i = new Integer(maxdiff.getText()).intValue();
      if(i < 1)
        return false;
    } catch(NumberFormatException nfe){
      return false;
    }
    return true;
  }
}