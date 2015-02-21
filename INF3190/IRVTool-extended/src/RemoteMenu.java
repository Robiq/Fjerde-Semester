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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class RemoteMenu extends JPanel implements ChangeListener{
  protected Clock clock = new Clock(new Color(153, 153, 204), Color.BLACK);
  protected JSpinner spin = new JSpinner();
  protected JButton apply = new JButton("Apply");
  protected JToggleButton button1 = new JToggleButton();
  protected JToggleButton button2 = new JToggleButton();
  protected JToggleButton button3 = new JToggleButton();
  private JSlider slider = new JSlider(1, 100);

  public RemoteMenu(IRVController c){
    super();
    initRemoteMenu(c);
  }

  public void stateChanged(ChangeEvent ce){
    Object o = ce.getSource();
    if(o == spin && spin.getEditor().hasFocus()){
      //System.out.println("APPLY");
      this.apply.doClick();
    }
    else if(o == slider){
      if(!slider.getValueIsAdjusting()){
        Consts.setDelay(slider.getValue());
      }
    }
  }

  private void initRemoteMenu(IRVController c){
    this.add(new JLabel("Speed: "));
    this.slider.setMajorTickSpacing(10);
    this.slider.setMinorTickSpacing(5);
    this.slider.setPaintTicks(true);
    this.slider.setValue(50);
    this.slider.setInverted(true);
    this.slider.addChangeListener(this);
    this.add(slider);
    this.add(new JLabel("Timeline: "));
    //this.spin.setMinimumSize(new Dimension(100, 10));
    this.spin.setPreferredSize(new Dimension(75, this.spin.getPreferredSize().height));
    this.spin.addChangeListener(this);

    this.add(this.spin);
    apply = new JButton(new ImageIcon(this.getClass().getResource("icons/apply01.png")));
    apply.setMargin(new Insets(0,0,0,0));
    apply.setContentAreaFilled(false);
    apply.setBorderPainted(false);
    apply.setFocusPainted(false);
    apply.setPressedIcon(new ImageIcon(this.getClass().getResource("icons/apply02.png")));
    //apply.setSelectedIcon(new ImageIcon("icons/playBut02.png"));
    apply.setActionCommand("Apply");
    apply.addActionListener(c);
    this.add(apply);
    this.add(new JLabel("         "));
    ButtonGroup bg = new ButtonGroup();
    button1 = new JToggleButton(new ImageIcon(this.getClass().getResource("icons/playBut01.png")));
    /* margin around Icon */
    button1.setMargin(new Insets(0,0,0,0));
    button1.setContentAreaFilled(false);
    button1.setBorderPainted(false);
    button1.setFocusPainted(false);
    button1.setPressedIcon(new ImageIcon(this.getClass().getResource("icons/playBut02.png")));
    button1.setSelectedIcon(new ImageIcon(this.getClass().getResource("icons/playBut02.png")));
    button1.addActionListener(c);
    button1.setActionCommand("Play");
    bg.add(button1);
    this.add(button1);
    button2 = new JToggleButton(new ImageIcon(this.getClass().getResource("icons/pauseBut01.png")));
    button2.setMargin(new Insets(0,0,0,0));
    button2.setContentAreaFilled(false);
    button2.setBorderPainted(false);
    button2.setFocusPainted(false);
    button2.setPressedIcon(new ImageIcon(this.getClass().getResource("icons/pauseBut02.png")));
    button2.setSelectedIcon(new ImageIcon(this.getClass().getResource("icons/pauseBut02.png")));
    button2.addActionListener(c);
    button2.setActionCommand("Pause");
    bg.add(button2);
    this.add(button2);
    button3 = new JToggleButton(new ImageIcon(this.getClass().getResource("icons/stopBut01.png")));
    button3.setMargin(new Insets(0,0,0,0));
    button3.setContentAreaFilled(false);
    button3.setBorderPainted(false);
    button3.setFocusPainted(false);
    button3.setPressedIcon(new ImageIcon(this.getClass().getResource("icons/stopBut02.png")));
    button3.setSelectedIcon(new ImageIcon(this.getClass().getResource("icons/stopBut02.png")));
    button3.setActionCommand("Stop");
    button3.setSelected(true);
    button3.addActionListener(c);
    bg.add(button3);
    this.add(button3);
    this.add(new JLabel("         "));
    this.add(clock);
  }
}