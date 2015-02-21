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

import java.util.*;

public class TerminalHistory{
  private ArrayList hist = new ArrayList(); 
  private int pos = -1;
  private String prompt;
  public TerminalHistory(String p){
    this.prompt = p;
  }
  
  public void push(String cmd){
    int index;
    cmd = cmd.substring(cmd.indexOf(" ")+1);
    if((index = hist.indexOf(cmd)) != -1)
      hist.remove(index);
    hist.add(cmd);
    pos = hist.size()-1;
  }
  
  public void setPrompt(String p){
    this.prompt = p;
  }
  
  public String moveBackward(){
    String result = null;
    if(hist.isEmpty() || pos == -1)
      return this.prompt;
    result = (String)hist.get(pos);
    if(pos == 0)
      return this.prompt+result;
    pos--;
    return this.prompt+result;
  }
  
  public String moveForward(){
    String result = null;
    if(hist.isEmpty() || pos >= hist.size()-1)
      return this.prompt;
    pos++;
    result = (String)hist.get(pos);
    return this.prompt+result;
  }
}
