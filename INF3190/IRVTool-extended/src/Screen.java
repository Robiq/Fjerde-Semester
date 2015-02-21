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
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.event.*;

public class Screen extends JPanel{
  protected ArrayList objects = new ArrayList();
  public Point start = null;
  public Point end = null;
  public Point old = null;
  public Paintable marked = null;
  public int extendedOSPFDisplay = 0;
  protected IRVFrame parent;

  // screen needs extendedOSPFDisplay, to display more ospf-info 
  // (e.g. Multiple Metrics) if necessary
  public Screen(IRVFrame parent, int extendedospf){
    super(true); // double buffering
    this.parent = parent;
    this.extendedOSPFDisplay = extendedospf;
    Dimension size = this.getToolkit().getScreenSize();
    this.setBackground(Color.WHITE);
    this.setSize(size);
    //this.setMinimumSize(size);
    this.setPreferredSize(size);
    this.setMaximumSize(size);
  }

  public void reset(){
    ArrayList help = (ArrayList)this.objects.clone();
    for(int i = 0; i < help.size(); ++i){
      ((Paintable)help.get(i)).delete();
    }
    objects = new ArrayList();
    start = null;
    end = null;
    old = null;
    marked = null;
  }

  public int getMaxAddress(){
    int max = 0;
    for(int i = 0; i < this.objects.size(); ++i){
      Paintable p = (Paintable)objects.get(i);
      if(p.getAddress() > max)
        max = p.getAddress();
    }
    return max;
  }

  public void setObjects(){
    for(int i = 0; i < this.objects.size(); ++i){
      ((Paintable)this.objects.get(i)).setScreen(this);
    }
  }

  public void paintComponent(Graphics g){
    super.paintComponent(g);
  }

  public ArrayList getObjects(){ return this.objects;}

  public void paintChildren(Graphics g){
    super.paintChildren(g);
    this.paintObjects(g);
  }

  public Paintable getPaintable(int a){
    for(int i = 0; i < this.objects.size(); ++i){
      Paintable p = (Paintable)this.objects.get(i);
      if(p.getAddress() == a){
        return p;
      }
    }
    return null;
  }


  protected void paintObjects(Graphics g){
    Object[] olist = objects.toArray();
    for(int i = 0; i < olist.length; ++i)
      ((Paintable)olist[i]).paint(g);
  }

  public void unmark(){
    if(marked != null) marked.mark(false);
  }

  public void unmarkAll(){
    for(int i = 0; i < this.objects.size(); ++i){
      ((Paintable)this.objects.get(i)).mark(false);
    }
  }

  public void insert(Paintable p){
    objects.add(p);
    this.unmark();
    marked = p;
    marked.mark(true);
    this.update(this.getGraphics());
  }

  public void remove(Paintable p){
    int i = objects.indexOf(p);
    if(i != -1)
      this.objects.remove(i);
  }

  protected Paintable getObjectAt(Point p){
    Object[] arr = objects.toArray();
    Paintable ob;
    for(int i = arr.length-1; i >= 0; --i){
      ob = (Paintable)arr[i];
      if(ob.contains(p)) return ob;
    }
    return null;
  }

  public boolean getAddressExists(int addr){
    for(int i = 0; i < this.objects.size(); ++i)
      if(((Paintable)this.objects.get(i)).getAddress() == addr) return true;
    return false;
  }

  public final void paintLine(Color c){
    if(start == null || old == null)
      return;
    Graphics g = this.getGraphics();
    g.setColor(c);
    g.drawLine(start.x, start.y, old.x, old.y);
    this.paintObjects(g);
  }

  public final void paintRect(Color c){
    if(start == null || end == null)
      return;
    Graphics g = this.getGraphics();
    g.setColor(c);

    int x = (start.x < end.x) ? start.x : end.x;
    int y = (start.y < end.y) ? start.y : end.y;
    int height = Math.abs(start.y - end.y);
    int width = Math.abs(start.x - end.x);
    g.drawRect(x, y, width, height);

    this.paintObjects(g);
  }

  public void update(Graphics g){
    //super.update(g); do not repaint Background
    this.paintObjects(g);
  }
}

