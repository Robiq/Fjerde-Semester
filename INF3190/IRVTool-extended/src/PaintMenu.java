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

public class PaintMenu extends JToolBar{
  private JToggleButton button1;
  private JToggleButton button2;
  private JToggleButton button3;
  private JToggleButton button4;
  private JToggleButton button5;
  private JToggleButton button6;

  public PaintMenu(ActionListener al){
    super(VERTICAL);
    //this.setLayout(new GridLayout(3,2));
    this.add(new JLabel("     "));
    this.putClientProperty("JToolBar.isRollover", Boolean.TRUE); /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
    initToolBar(al);
    /* we can move the ToolBar */
    this.setFloatable(true);
  }

  public void unselectAll(){
    if(button1.isSelected())
      button1.doClick();
    if(button2.isSelected())
      button2.doClick();
    if(button3.isSelected())
      button3.doClick();
    if(button4.isSelected())
      button4.doClick();
    if(button5.isSelected())
      button5.doClick();
    if(button6.isSelected())
      button6.doClick();
  }

  private void initToolBar(ActionListener al){
    ButtonGroup bg = new ButtonGroup();
    button1 = new JToggleButton(new ImageIcon(this.getClass().getResource("icons/MarkerTool02.png")));
    button1.setRolloverIcon(new ImageIcon(this.getClass().getResource("icons/MarkerTool01.png")));
    button1.setMargin(new Insets(1,1,1,1));
    button1.addActionListener(al);
    button1.setActionCommand("Mark");
    button1.setToolTipText("Marker-Tool");
    this.add(button1);
    bg.add(button1);
    button2 = new JToggleButton(new ImageIcon(this.getClass().getResource("icons/RemoveTool02.png")));
    button2.setRolloverIcon(new ImageIcon(this.getClass().getResource("icons/RemoveTool01.png")));
    button2.setMargin(new Insets(1,1,1,1));
    button2.addActionListener(al);
    button2.setActionCommand("Remove");
    button2.setToolTipText("Remove-Tool");
    this.add(button2);
    bg.add(button2);
    button3 = new JToggleButton(new ImageIcon(this.getClass().getResource("icons/HostsTool02.png")));
    button3.setRolloverIcon(new ImageIcon(this.getClass().getResource("icons/HostsTool01.png")));
    button3.setMargin(new Insets(1,1,1,1));
    button3.addActionListener(al);
    button3.setActionCommand("Host");
    button3.setToolTipText("Host-Tool");
    this.add(button3);
    bg.add(button3);
    button4 = new JToggleButton(new ImageIcon(this.getClass().getResource("icons/RouterTool02.png")));
    button4.setRolloverIcon(new ImageIcon(this.getClass().getResource("icons/RouterTool01.png")));
    button4.setMargin(new Insets(1,1,1,1));
    button4.addActionListener(al);
    button4.setActionCommand("Router");
    button4.setToolTipText("Router-Tool");
    this.add(button4);
    bg.add(button4);
    button5 = new JToggleButton(new ImageIcon(this.getClass().getResource("icons/ConnectionTool02.png")));
    button5.setRolloverIcon(new ImageIcon(this.getClass().getResource("icons/ConnectionTool01.png")));
    button5.setMargin(new Insets(1,1,1,1));
    button5.addActionListener(al);
    button5.setActionCommand("Connection");
    button5.setToolTipText("Connection-Tool");
    this.add(button5);
    bg.add(button5);
    button6 = new JToggleButton(new ImageIcon(this.getClass().getResource("icons/EditTool02.png")));
    button6.setRolloverIcon(new ImageIcon(this.getClass().getResource("icons/EditTool01.png")));
    button6.setMargin(new Insets(1,1,1,1));
    button6.addActionListener(al);
    button6.setActionCommand("Edit");
    button6.setToolTipText("Edit-Tool");
    this.add(button6);
    bg.add(button6);
  }
}