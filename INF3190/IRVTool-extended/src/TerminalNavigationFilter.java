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

import javax.swing.text.*;
import javax.swing.*;

public class TerminalNavigationFilter extends NavigationFilter{
  private String prompt = null;
  
  public TerminalNavigationFilter(String prompt){
    super();
    this.prompt = prompt;
  }
  
  public void setDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias){
    if(dot < prompt.length()+1)
      dot = prompt.length()+1;
    //System.err.println("setDot");
    //System.err.println("dot = "+dot);
    fb.setDot(dot, bias);
  }
  
  public void moveDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias){
    if(dot < prompt.length()+1)
      dot = prompt.length()+1;
    //System.err.println("moveDot");
    fb.moveDot(dot, bias);
  }
  
  /*public int getNextVisualPositionFrom(JTextComponent text, int pos, Position.Bias bias, int direction, Position.Bias[] biasRet) throws BadLocationException{
    if(direction == SwingConstants.WEST && bias == Position.Bias.Backward){
      if(pos <= this.prompt.length()+1)
        pos = this.prompt.length()+2;
    }
    //System.err.println("getNextVisualPositionFrom");
    return super.getNextVisualPositionFrom(text, pos, bias, direction, biasRet);
  }*/
  
  public void setPrompt(String p){
    //System.err.println("setPrompt");
    this.prompt = p;
  }
}
