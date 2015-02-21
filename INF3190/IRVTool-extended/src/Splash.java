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
import java.util.*;

/**
* A splashscreen that displays a graphic and a short text.
* @author Christian Sternagel
*/
public  class Splash extends JWindow implements Runnable{
  private Image img;
  private ArrayList text = new ArrayList();
  private int millis;

  public Splash(String image, String text){
    super();
    ImageIcon ii = new ImageIcon(this.getClass().getResource(image));
    this.img = ii.getImage();
    StringTokenizer st = new StringTokenizer(text);
    while(st.hasMoreTokens()){
      this.text.add(st.nextToken("\n"));
    }
    this.setSize(ii.getIconWidth(), ii.getIconHeight());
  }

  public void paint(Graphics g){
    super.paint(g);
    g.drawImage(this.img, 0, 0, this);
    g.setColor(Color.WHITE);
    for(int i = this.text.size()-1; i >= 0; --i){
      g.drawString(this.text.get(i).toString(), 5, this.getSize().height-(this.text.size()-i)*15);
    }
  }

  public void run(){
    this.showFor(this.millis);
  }

  public void start(){
    Thread t = new Thread(this);
    t.start();
  }

  public void setMillis(int millis){
    this.millis = millis;
  }

  /**
  * The splashscreen is shown for 'millis' milliseconds.
  */
  public void showFor(int millis){
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation((dim.width-this.getSize().width)/2, (dim.height-this.getSize().height)/2);
    setVisible(true);
    try{
      Thread.sleep(millis);
    } catch(InterruptedException ie){}
    this.setVisible(false);
    this.dispose();
  }
}
