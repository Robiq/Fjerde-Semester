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

public class IRVDialog extends JDialog{
  EditPane ep;
  JFrame parent;

  public IRVDialog(JFrame parent, EditPane ep){
    super(parent, ep.getTitle(), true /*modal*/);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    Container cp = this.getContentPane();
    this.ep = ep;
    this.parent = parent;
    JButton ok = new JButton("OK");
    ok.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        if(IRVDialog.this.ep.save()){
          IRVDialog.this.setVisible(false);
          //IRVDialog.this.remove(IRVDialog.this.ep);
          IRVDialog.this.parent.repaint();
          IRVDialog.this.dispose();
        }
        else{
          JOptionPane.showMessageDialog(IRVDialog.this, "Invalid Input!", "ERROR", JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getClass().getResource("icons/iInput01.png")));
        }
      }
    }
    );
    /*
    JButton cancel = new JButton("Cancel");
    cancel.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        IRVDialog.this.setVisible(false);
        IRVDialog.this.parent.repaint();
        IRVDialog.this.dispose();
      }
    });
    */
    Dimension d = parent.getSize();
    Point p = parent.getLocation();
    this.setLocation(p.x+d.width/3, p.y+d.height/3);
    cp.setLayout(new BorderLayout());
    cp.add(ep, BorderLayout.CENTER);
    JPanel dummy = new JPanel();
    dummy.add(ok);
    //dummy.add(cancel);
    cp.add(dummy, BorderLayout.SOUTH);
    this.pack();
    this.setResizable(false);
    this.setVisible(true);
  }
}
