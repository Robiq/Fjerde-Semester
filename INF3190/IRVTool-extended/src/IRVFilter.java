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

import java.io.*;
import java.util.*;


/**
* A FilenameFilter that accepts all *.irv files. These files are used for the IRV-Tool.
*/
public class IRVFilter extends javax.swing.filechooser.FileFilter{
  private final static String ext = ".irv"; // accepted extention

  /**
  * Accepts all *.irv files.
  */
  public boolean accept(File file){
    if(file.isDirectory() || file.toString().endsWith(ext)) return true;
    return false;
  }

  public String getDescription(){
    return "*.irv files";
  }
}