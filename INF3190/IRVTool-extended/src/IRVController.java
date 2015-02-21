package irvtool;
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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class IRVController implements ActionListener, MouseMotionListener, MouseListener, KeyListener{
  protected Screen screen;
  protected IRVFrame frame;

  private static final int BUT1 = 64;  // left mouse button
  private static final int BUT2 = 128; // middle mouse button
  private static final int BUT3 = 256; // right mouse button
  private int currButt = BUT1;
  private boolean mark = false;
  private boolean remove = false;
  private boolean router = false;
  private boolean host = false;
  private boolean connection = false;
  private boolean edit = false;
  protected boolean play = false;
  protected boolean pause = false;
  protected boolean stop = true;
  private boolean changed = false;
  protected Clock clock;
  private Computer node;
  protected Simulator sim = null;

  public IRVController(Screen screen, IRVFrame frame){
    this.screen = screen;
    this.frame = frame;
  }

  public void setClock(Clock c){
    this.clock = c;
  }

  public void setChanged(boolean c){
    this.changed = c;
  }

  public boolean getChanged(){
    return this.changed;
  }

  public void setScreen(Screen screen){
    this.screen = screen;
  }

  public void setSimulator(Simulator sim){
    this.sim = sim;
  }

  public RoutingTable getRoutingTable(Router r){
    RoutingTable result = null;
    int flag = 0;
    if(this.frame.rip()){
      if(this.frame.splitHorizon() && !this.frame.splitHorizonWithPoisonedReverse()){
        //System.out.println("SH");
        flag |= DistanceVectorTable.SH;
      }
      else if(this.frame.splitHorizonWithPoisonedReverse()){
        //System.out.println("SH_PR");
        flag |= DistanceVectorTable.SH_PR;
      }
      if(this.frame.triggeredUpdates()){
        //System.out.println("TRIGG");
        flag |= DistanceVectorTable.TRIGG;
      }
      result = new DistanceVectorTable(r, flag);
    }
    else if(this.frame.ospf()){
      if(this.frame.multiplePaths())
        flag |= LinkStateTable.MULTI;
      result = new LinkStateTable(r, flag);
    }
    return result;
  }

  public int getStatus(){
    int flag = 0;
    if(this.frame.rip()){
      if(this.frame.splitHorizon() && !this.frame.splitHorizonWithPoisonedReverse()){
        //System.out.println("SH");
        flag |= DistanceVectorTable.SH;
      }
      else if(this.frame.splitHorizonWithPoisonedReverse()){
        //System.out.println("SH_PR");
        flag |= DistanceVectorTable.SH_PR;
      }
      if(this.frame.triggeredUpdates()){
        //System.out.println("TRIGG");
        flag |= DistanceVectorTable.TRIGG;
      }
    }
    else if(this.frame.ospf()){
      if(this.frame.multiplePaths())
        flag |= LinkStateTable.MULTI;
    }
    return flag;
  }

  public void mouseClicked(MouseEvent me){
    //System.out.println("Mouse clicked ...");
    //currButt = chooseButton(me.getModifiersEx()); Button got from mousePressed
    int x = me.getX();
    int y = me.getY();
    if(x < Computer.DIAM/2 + 5)
      x = Computer.DIAM/2 + 5;
    if(y < Computer.DIAM/2 + 5)
      y = Computer.DIAM/2 + 5;
    if(x > screen.getSize().width - Computer.DIAM/2 - 5)
      x = screen.getSize().width - Computer.DIAM/2 - 5;
    if(y > screen.getSize().height - Computer.DIAM/2 - 5)
      y = screen.getSize().height - Computer.DIAM/2 - 5;

    if(this.host && currButt == BUT1 && this.stop){
      Host h = new Host(new Point(x, y), screen);
      screen.insert(h); // insert a new host
      this.setChanged(true);
      //h.start();
    } else if(this.router && currButt == BUT1 && this.stop){
      Router rtr = new Router(new Point(x, y), screen);
      screen.insert(rtr); // insert a new router
      rtr.setRoutingTable(this.getRoutingTable(rtr)); // initialize RoutingTable
      this.setChanged(true);
    } else if(this.mark && currButt == BUT1 && this.stop){
      screen.unmark();
      screen.marked = screen.getObjectAt(me.getPoint());
      if(screen.marked != null)
        screen.marked.mark(true);
    } else if((currButt == BUT3 || (this.edit && currButt == BUT1)) && (this.stop || this.pause)){
      Paintable p = screen.getObjectAt(me.getPoint());
      if(p instanceof Host){
    	  if(this.frame.ospf())
    		  // if using Multiple Metrics, display correct Pane
    		  new IRVDialog(this.frame, new HostEditPaneOSPF((Host)p));
    	  else
    		  new IRVDialog(this.frame, new HostEditPane((Host)p));
        this.setChanged(true);
      } else if(p instanceof Router){
        Router r = (Router)p;
        if(r.rtable instanceof DistanceVectorTable)
          new IRVDialog(this.frame, new RouterEditPaneRIP((Router)p));
        else if(r.rtable instanceof LinkStateTable)
          new IRVDialog(this.frame, new RouterEditPaneOSPF((Router)p));
        this.setChanged(true);
      } else if(p instanceof Connection){
    	  if(this.frame.ospf())
    		  // if using Multiple Metrics, display correct Pane
    		  new IRVDialog(this.frame, new ConnectionEditPaneOSPF((Connection)p));
    	  else
    		  new IRVDialog(this.frame, new ConnectionEditPane((Connection)p));
        this.setChanged(true);
      }
    }
  }

  private int getButton(int value){
    switch(value){
      case InputEvent.BUTTON2_DOWN_MASK:
        return BUT2;
      case InputEvent.BUTTON3_DOWN_MASK:
        return BUT3;
      default:
        return BUT1;
    }
  }

  public void mouseEntered(MouseEvent me){}

  public void mouseExited(MouseEvent me){}

  public void mousePressed(MouseEvent me){
    //System.out.println("Mouse pressed ...");
    currButt = getButton(me.getModifiersEx()); // also for mouseClicked
    Point tmp = me.getPoint();

    if(!this.mark && this.stop)
      screen.unmark();

    if((this.connection || this.mark) && currButt == BUT1 && this.stop){
      if(!(screen.getObjectAt(tmp) instanceof Computer)){
        screen.start = null;
        return;
      }
      node = (Computer)screen.getObjectAt(tmp);
      screen.start = node.getPosition();
      screen.old = node.getPosition();
    }
    if(this.remove && currButt == BUT1 && this.stop){
      screen.unmark();
      screen.start = me.getPoint();
      screen.end = me.getPoint();
    }
  }

  public void mouseReleased(MouseEvent me){
    //System.out.println("Mouse released ...");
    if(this.connection && currButt == BUT1 && this.stop){
      screen.end = me.getPoint();
      if(screen.start != null && screen.getObjectAt(screen.end) instanceof Computer){
        Computer first = (Computer)screen.getObjectAt(screen.start);
        Computer second = (Computer)screen.getObjectAt(screen.end);
        if(!(first instanceof Host && second instanceof Host)){
          if((first instanceof Host && ((Host)first).getCon() == -1) || (second instanceof Host && ((Host)second).getCon() == -1) || (!(first instanceof Host) && !(second instanceof Host))){
            Connection con = new Connection(first, second, screen);
            if(first.connect(con) && second.connect(con)){
              screen.insert(con); // insert new connection
              this.setChanged(true);
            }
          }
        }
      }
      screen.paintLine(this.screen.getBackground());
      screen.start = null;
      screen.end = null;
      screen.old = null;
    }
    if(this.node != null && this.mark && currButt == BUT1 && this.stop){
      //System.out.println("after mark");
      this.node = null;
      screen.start = null;
      screen.end = null;
      screen.old = null;
    }
    if(screen.start != null && this.remove && currButt == BUT1 && this.stop){
      screen.paintRect(screen.getBackground());
      screen.end = me.getPoint();
      ArrayList toDelete = new ArrayList();
      for(int i = 0; i < screen.objects.size(); ++i){
        Object object = screen.objects.get(i);
        //System.out.println("" + i);
        if(object instanceof Computer){
          Computer com = (Computer)object;
          int x = (screen.start.x < screen.end.x) ? screen.start.x : screen.end.x;
          int y = (screen.start.y < screen.end.y) ? screen.start.y : screen.end.y;
          int height = Math.abs(screen.start.y - screen.end.y);
          int width = Math.abs(screen.start.x - screen.end.x);
          int xpos = com.getPosition().x;
          int ypos = com.getPosition().y;
          if((xpos > x) && (xpos < (x + width)) && (ypos > y) && (ypos < (y + height))){
            toDelete.add(com);
          }
        }
      }
      for(int i = 0; i < toDelete.size(); ++i)
        ((Computer)toDelete.get(i)).delete();
      toDelete = null;
      screen.start = null;
      screen.end = null;
      screen.old = null;
      this.setChanged(true);
    }
  }

  public void mouseDragged(MouseEvent me){
    //System.out.println("Mouse dragged ...");
    if(this.connection && currButt == BUT1 && this.stop){
      if(screen.start == null)
        return;
      screen.paintLine(screen.getBackground());
      screen.update(screen.getGraphics()); // paint Objects without painting Background
      screen.old = me.getPoint();
      screen.paintLine(Color.GRAY);
    }
    if(this.mark && currButt == BUT1 && this.stop){
      if(screen.start == null)
        return;
      //System.out.println("move");
      screen.unmark();
      screen.marked = node;
      node.mark(false);
      ArrayList cons = node.getConnections();
      for(int i = 0; i < cons.size(); ++i){
        Connection con = (Connection)cons.get(i);
        if(con != null){
          con.shape(screen.getGraphics(), screen.getBackground());
        }
      }
      node.shape(screen.getGraphics(), true);

      int x = me.getX();
      int y = me.getY();
      if(x < Computer.DIAM/2 + 5)
        x = Computer.DIAM/2 + 5;
      if(y < Computer.DIAM/2 + 5)
        y = Computer.DIAM/2 + 5;
      if(x > screen.getSize().width - Computer.DIAM/2 - 5)
        x = screen.getSize().width - Computer.DIAM/2 - 5;
      if(y > screen.getSize().height - Computer.DIAM/2 - 5)
        y = screen.getSize().height - Computer.DIAM/2 - 5;

      node.setPosition(new Point(x, y));
      for(int i = 0; i < cons.size(); ++i){
        Connection con = (Connection)cons.get(i);
        if(con != null){
          con.shape(screen.getGraphics(), Paintable.markc);
        }
      }
      node.shape(screen.getGraphics(), true);
      node.mark(true);
      this.setChanged(true);
    }
    if(this.remove && currButt == BUT1 && this.stop){
      if(screen.start == null)
        return;
      screen.unmark();
      screen.paintRect(screen.getBackground());
      screen.end = me.getPoint();
      screen.paintRect(Color.GRAY);

    }
  }

  public void mouseMoved(MouseEvent me){}

  public void keyPressed(KeyEvent ke){
    //System.out.println("Key pressed ...");
    int code = ke.getKeyCode();
    if(code == KeyEvent.VK_DELETE && screen.marked != null){
      screen.marked.delete();
      this.screen.marked = null;
      this.setChanged(true);
    }
  }

  public void keyReleased(KeyEvent ke){}

  public void keyTyped(KeyEvent ke){}

  public void actionPerformed(ActionEvent ae){
    //System.out.println("AP");
    String cmd = ae.getActionCommand();
    Cursor cu;
    if(cmd.equals("Mark")){
      //System.out.println("Mark");
      cu = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(this.getClass().getResource("icons/MarkerToolCursor.png")).getImage(), new Point(0,0), "myCursor");
      this.screen.setCursor(cu);
      this.mark = true;
      remove = router = host = connection = edit = false;
    }else if(cmd.equals("Remove")){
      //System.out.println("Remove");
      cu = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(this.getClass().getResource("icons/RemoveToolCursor.png")).getImage(), new Point(0,0), "myCursor");
      this.screen.setCursor(cu);
      this.remove = true;
      mark = router = host = connection = edit = false;
    }else if(cmd.equals("Host")){
      //System.out.println("Host");
      cu = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(this.getClass().getResource("icons/HostsToolCursor.png")).getImage(), new Point(0,0), "myCursor");
      this.screen.setCursor(cu);
      this.host = true;
      mark = remove = router = connection = edit = false;
    }else if(cmd.equals("Router")){
      //System.out.println("Router");
      cu = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(this.getClass().getResource("icons/RouterToolCursor.png")).getImage(), new Point(0,0), "myCursor");
      this.screen.setCursor(cu);
      this.router = true;
      mark = remove = host = connection = edit = false;
    }else if(cmd.equals("Connection")){
      //System.out.println("Connection");
      cu = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(this.getClass().getResource("icons/ConnectionToolCursor.png")).getImage(), new Point(0,0), "myCursor");
      this.screen.setCursor(cu);
      this.connection = true;
      mark = remove = router = host = edit = false;
    }else if(cmd.equals("Edit")){
      //System.out.println("Edit");
      cu = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(this.getClass().getResource("icons/EditToolCursor.png")).getImage(), new Point(0,0), "myCursor");
      this.screen.setCursor(cu);
      this.edit = true;
      mark = remove = router = host = connection = false;
    }else if(cmd.equals("New")){
      frame.newNetwork();
      this.setChanged(false);
    } else if(cmd.equals("Open")){
      frame.openNetwork();
    } else if(cmd.equals("Close")){
      frame.closeNetwork();
    } else if(cmd.equals("Save")){
      frame.saveNetwork();
    } else if(cmd.equals("Save As ...")){
      frame.saveNetworkAs();
    } else if(cmd.equals("Quit")){
      frame.quit();
    }else if(cmd.equals("Apply")){
      if(this.sim == null){
        Simulator simu = new Simulator(this, clock);
        this.setSimulator(simu);
        this.frame.term.setSimulator(simu);
        this.frame.term.enable();
        
      }
      int steps = (int)(new Long(this.frame.remoteMenu.spin.getValue().toString()).longValue()-this.sim.timeline.time());
      if(steps < 0){
        JOptionPane.showMessageDialog(this.frame, "Can't go to the Past!", "ERROR", JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getClass().getResource("icons/notPast01.png")));
        this.frame.remoteMenu.spin.setValue(new Long(this.sim.timeline.time()));
        //JButton butt = new JButton("Apply");
        //butt.addChangeListener(this);
        //this.frame.remoteMenu.remove(this.frame.remoteMenu.getComponent(2/*apply*/));
        //this.frame.remoteMenu.add(butt, 2);
        //this.frame.validate();
        //this.frame.repaint();
      }
      this.sim.calc(steps);
      this.sim.clock.setTime(this.sim.timeline.time());
      this.frame.remoteMenu.button2.doClick();
      return;
    } else if(cmd.equals("Play")){
      this.play = true;
      this.pause = false;
      this.stop = false;
      if(this.sim == null){
        Simulator simu = new Simulator(this, clock);
        this.setSimulator(simu);
        this.frame.term.setSimulator(simu);
        this.frame.term.enable();
      }
      this.frame.disableMenu();
      this.frame.term.enable();
      this.sim.play();

    } else if(cmd.equals("Pause")){
      this.play = false;
      this.pause = true;
      this.stop = false;
      if(this.sim != null){
        this.sim.stop();
        this.frame.term.enable();
      }

      this.frame.disableMenu();
      this.frame.remoteMenu.spin.setValue((sim == null) ? new Long(0) : new Long(this.sim.timeline.time()));

    } else if(cmd.equals("Stop")){
      this.play = false;
      this.pause = false;
      this.stop = true;
      if(this.sim != null){
        this.sim.stop();
        this.sim.clear();
        this.frame.term.disable();
      }
      this.clock.stop();
      this.sim = null;
      this.frame.enableMenu();
      this.frame.remoteMenu.spin.setValue(new Long(0));

    }
  }
}
