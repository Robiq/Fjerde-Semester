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
import java.awt.event.*;

public class InfoPane extends JDialog{
  private JTextArea area = null;
  private JScrollPane scroll = null;
  
  public InfoPane(Frame owner, String title, String text){
    super(owner, title, true);
    this.area = new JTextArea(text, 40, 80);
    this.area.setEditable(false);
    this.area.setLineWrap(true);
    this.area.setWrapStyleWord(true);
    this.scroll = new JScrollPane(this.area, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    this.getContentPane().add(scroll);
    JButton close = new JButton("close");
    close.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        InfoPane.this.dispose();
      }
    });
    this.getContentPane().add(close, BorderLayout.SOUTH);
    this.setLocation(owner.getWidth()/2-200, 50);
    this.setSize(600, 600);
  }
}