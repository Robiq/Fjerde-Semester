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

import java.awt.*;
import javax.swing.border.*;
import java.awt.event.*;
import javax.swing.*;

public class Clock extends JPanel implements java.io.Serializable, MouseListener{
  private long time = 0;
  private byte sec0 = 0;
  private byte sec1 = 0;
  private byte min0 = 0;
  private byte min1 = 0;
  private byte hour0 = 0;
  private byte hour1 = 0;
  private SevenSegmentDisplay ssd = new SevenSegmentDisplay();
  private Color dig;
  private Color back;
  private boolean mode = true; /* false = time in hh:mm:ss; true = time in sssssss*/
  
  public synchronized void changeMode(){
    mode = !mode;
  }

  public Clock(Color digits, Color background){
    super(true);
    this.dig = digits;
    this.back = background;
    Dimension d = new Dimension(63, 23);
    setMinimumSize(d);
    setMaximumSize(d);
    setPreferredSize(d);
    //setBackground(background);
    Border raisedbevel = BorderFactory.createRaisedBevelBorder();
    Border loweredbevel = BorderFactory.createLoweredBevelBorder();
    setBorder(BorderFactory.createCompoundBorder(raisedbevel, loweredbevel));
    this.addMouseListener(this);
  }
  
  public void mouseClicked(MouseEvent me){
    this.changeMode();
    this.repaint();
  }
  
  public void mouseEntered(MouseEvent me){
  }
  
  public void mouseExited(MouseEvent me){
  }
  
  public void mousePressed(MouseEvent me){
  }
  
  public void mouseReleased(MouseEvent me){
  }

  public void play(){

  }

  public void pause(){

  }

  public void stop(){
    sec0 = sec1 = min0 = min1 = hour0 = hour1 = 0;
    time = 0;
    repaint();
  }

  public void setTime(long time){
    this.time = time;
    sec0 = (byte)(time%10);
    sec1 = (byte)((time/10)%6);
    min0 = (byte)((time/60)%10);
    min1 = (byte)((time/600)%6);
    hour0 = (byte)((time/3600)%10);
    hour1 = (byte)((time/36000)%10);
    repaint();
  }


/*
  private void drawDigits(){
    Graphics g = this.getGraphics();
    boolean[] d;
    int old;
    g.setColor(this.back);
    old = (hour1 == 0) ? 5 : hour1 - 1;
    d = ssd.toClear(old, hour1);
    draw(d, 5, g);
    old = (hour0 == 0) ? 9 : hour0 - 1;
    d = ssd.toClear(old, hour0);
    draw(d, 13, g);
    old = (min1 == 0) ? 5 : min1 - 1;
    d = ssd.toClear(old, min1);
    draw(d, 24, g);
    old = (min0 == 0) ? 9 : min0 - 1;
    d = ssd.toClear(old, min0);
    draw(d, 32, g);
    old = (sec1 == 0) ? 5 : sec1 - 1;
    d = ssd.toClear(old, sec1);
    draw(d, 43, g);
    old = (sec0 == 0) ? 9 : sec0 -1;
    d = ssd.toClear(old, sec0);
    draw(d, 51, g);
    old = (tenth == 0) ? 9 : tenth - 1;
    d = ssd.toClear(old, tenth);
    draw(d, 62, g);

    g.setColor(this.dig);

    old = (hour1 == 0) ? 5 : hour1 - 1;
    d = ssd.toPaint(old, hour1);
    draw(d, 5, g);
    old = (hour0 == 0) ? 9 : hour0 - 1;
    d = ssd.toPaint(old, hour0);
    draw(d, 13, g);
    old = (min1 == 0) ? 5 : min1 - 1;
    d = ssd.toPaint(old, min1);
    draw(d, 24, g);
    old = (min0 == 0) ? 9 : min0 - 1;
    d = ssd.toPaint(old, min0);
    draw(d, 32, g);
    old = (sec1 == 0) ? 5 : sec1 - 1;
    d = ssd.toPaint(old, sec1);
    draw(d, 43, g);
    old = (sec0 == 0) ? 9 : sec0 -1;
    d = ssd.toPaint(old, sec0);
    draw(d, 51, g);
    old = (tenth == 0) ? 9 : tenth - 1;
    d = ssd.toPaint(old, tenth);
    draw(d, 62, g);
    g.dispose();
  }*/

  public void paintComponent(Graphics g){
    super.paintComponent(g);
    g.setColor(back);
    g.fillRect(4, 4, 65, 65);
    g.setColor(dig);
    if(!this.mode){
      g.drawLine(21, 9, 21, 10);
      g.drawLine(21, 13, 21, 14);
      g.drawLine(40, 9, 40, 10);
      g.drawLine(40, 13, 40, 14);

      draw(ssd.getDigit(this.hour1), 5, g);
      draw(ssd.getDigit(this.hour0), 13, g);
      draw(ssd.getDigit(this.min1), 24, g);
      draw(ssd.getDigit(this.min0), 32, g);
      draw(ssd.getDigit(this.sec1), 43, g);
      draw(ssd.getDigit(this.sec0), 51, g);
    }
    else{
      draw(ssd.getDigit((byte)((this.time/1000000)%10)), 4, g);
      draw(ssd.getDigit((byte)((this.time/100000)%10)), 12, g);
      draw(ssd.getDigit((byte)((this.time/10000)%10)), 20, g);
      draw(ssd.getDigit((byte)((this.time/1000)%10)), 28, g);
      draw(ssd.getDigit((byte)((this.time/100)%10)), 36, g);
      draw(ssd.getDigit((byte)((this.time/10)%10)), 44, g);
      draw(ssd.getDigit((byte)(this.time%10)), 52, g);
    }
  }

  private void draw(boolean[] toDraw, int x, Graphics g){
    if(toDraw[0])
      g.drawLine(x + 1, 5, x + 4, 5);
    if(toDraw[1])
      g.drawLine(x + 1, 11, x + 4, 11);
    if(toDraw[2])
      g.drawLine(x + 1, 17, x + 4, 17);
    if(toDraw[3])
      g.drawLine(x, 6, x, 10);
    if(toDraw[4])
      g.drawLine(x + 5, 6, x + 5, 10);
    if(toDraw[5])
      g.drawLine(x, 12, x, 16);
    if(toDraw[6])
      g.drawLine(x + 5, 12, x + 5, 16);
  }

  class SevenSegmentDisplay{
    /**
             0
           ****
          *    *
         3*    *4
          *    *
          *    *
          *  1 *
           ****
          *    *
         5*    *6
          *    *
          *    *
          *  2 *
           ****

    */

    protected final boolean[] getDigit(byte digit){
      boolean[] segments = new boolean[7];
      switch(digit){
        case 0:
          segments[0] = segments[2] = segments[3] = segments[4] = segments[5] = segments[6] = true;
          segments[1] = false;
          break;
        case 1:
          segments[4] = segments[6] = true;
          segments[0] = segments[1] = segments[2] = segments[3] = segments[5] = false;
          break;
        case 2:
          segments[0] = segments[4] = segments[1] = segments[5] = segments[2] = true;
          segments[3] = segments[6] = false;
          break;
        case 3:
          segments[0] = segments[1] = segments[2] = segments[4] = segments[6] = true;
          segments[3] = segments[5] = false;
          break;
        case 4:
          segments[1] = segments[3] = segments[4] = segments[6] = true;
          segments[0] = segments[2] = segments[5] = false;
          break;
        case 5:
          segments[0] = segments[1] = segments[2] = segments[3] = segments[6] = true;
          segments[4] = segments[5] = false;
          break;
        case 6:
          segments[0] = segments[1] = segments[2] = segments[3] = segments[5] = segments[6] = true;
          segments[4] = false;
          break;
        case 7:
          segments[0] = segments[4] = segments[6] = true;
          segments[1] = segments[2] = segments[3] = segments[5] = false;
          break;
        case 8:
          for(int i = 0; i < segments.length; ++i)
            segments[i] = true;
          break;
        case 9:
          segments[0] = segments[1] = segments[2] = segments[3] = segments[4] = segments[6] = true;
          segments[5] = false;
          break;
      }
      return segments;
    }

    protected final boolean[] toPaint(int from, int to){
      boolean[] result = new boolean[7];
      boolean[] oldD = this.getDigit((byte)from);
      boolean[] newD = this.getDigit((byte)to);
      for(int i = 0; i < result.length; ++i){
        if(oldD[i] == false && newD[i] == true)
          result[i] = true;
        else
          result[i] = false;
      }
      return result;
    }

    protected final boolean[] toClear(int from, int to){
      boolean[] result = new boolean[7];
      boolean[] oldD = this.getDigit((byte)from);
      boolean[] newD = this.getDigit((byte)to);
      for(int i = 0; i < result.length; ++i){
        if(oldD[i] == true && newD[i] == false)
          result[i] = true;
        else
          result[i] = false;
      }
      return result;
    }
  } /* END class */

  public static void main(String[] argv){
    Clock c = new Clock(new Color(153, 153, 204), Color.BLACK);
  }

}
