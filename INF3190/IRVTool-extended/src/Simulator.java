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

public class Simulator implements Runnable{
  protected Timeline timeline = new Timeline();
  protected Clock clock;

  protected IRVController controller; /* never serialize the controller */
  protected ArrayList hosts = new ArrayList();
  protected ArrayList routers = new ArrayList();
  protected ArrayList connections = new ArrayList();
  protected ArrayList nodes = new ArrayList();
  private Thread th = null;

  public Simulator(IRVController controller, Clock clock){
    this.clock = clock;
    this.clock.setTime(this.timeline.time());
    this.controller = controller;
    ArrayList help = controller.screen.getObjects();
    for(int i = 0; i < help.size(); ++i) {
      Paintable tmp = (Paintable)help.get(i);
      if(tmp instanceof Host){
        hosts.add(tmp);
        nodes.add(tmp);
      }
      else if(tmp instanceof Router) {
        Router r = (Router)tmp;
        int mode = 0;
        // if using MM-Mode, set all Routers to MM-Mode
        if(controller.frame.usingMultipleMetrics())
        	mode |= Consts.OSPFMULTIPLEMETRICS;
        if(controller.frame.usingMultipleAreas())
        	mode |= Consts.OSPFMULTIPLEAREAS;
        
        r.setOspfMode(mode);
        
        if (r.rtable == null
				|| (controller.frame.ospf() && !(r.rtable instanceof LinkStateTable))
				|| (controller.frame.rip() && !(r.rtable instanceof DistanceVectorTable)))
        		r.setRoutingTable(controller.getRoutingTable(r));
		r.setStatus(this.controller.getStatus());
		if(controller.frame.usingMultipleAreas()) 
			((LinkStateTable)(r.rtable)).updateOwnAreas();
		routers.add(tmp);
		nodes.add(tmp);   
      }
      else
          connections.add(tmp);
    }
  }
  

  public boolean isHost(int addr){
    for(int i = 0; i < this.hosts.size(); ++i)
      if(((Host)this.hosts.get(i)).getAddress() == addr) return true;
    return false;
  }

  public boolean isRouter(int addr){
    for(int i = 0; i < this.routers.size(); ++i)
      if(((Router)this.routers.get(i)).getAddress() == addr) return true;
    return false;
  }

  public boolean isConnection(int addr){
    for(int i = 0; i < this.connections.size(); ++i)
      if(((Connection)this.connections.get(i)).getAddress() == addr) return true;
    return false;
  }

  public Computer getNode(int addr){
    for(int i = 0; i < this.nodes.size(); ++i)
      if(((Computer)this.nodes.get(i)).getAddress() == addr) return (Computer)this.nodes.get(i);
    return null;
  }

  public Connection getConnection(int addr){
    for(int i = 0; i < this.connections.size(); ++i)
      if(((Connection)this.connections.get(i)).getAddress() == addr) return (Connection)this.connections.get(i);
    return null;
  }

  public boolean isNode(int addr){
    for(int i = 0; i < this.nodes.size(); ++i)
      if(((Computer)this.nodes.get(i)).getAddress() == addr) return true;
    return false;
  }

  public void setClock(Clock c){
    this.clock = c;
  }

  public void setController(IRVController irvc){
    this.controller = irvc;
  }

  public void play(){
    if(this.th == null){
      this.th = new Thread(this);
      this.th.start();
    }
  }

  public void stop(){
    if(this.th != null){
      try{
        this.th.join();
      } catch(InterruptedException ie){
        this.controller.frame.term.write(ie.toString());
        System.exit(1);
      }
    }
    this.th = null;
  }

  public void clear(){
    for(int i = 0; i < routers.size(); ++i){
      ((Router)routers.get(i)).setRoutingTable(controller.getRoutingTable((Router)routers.get(i))); // delete routing tables after stop
      ((Router)routers.get(i)).getInQueue().clear();
    }
    for(int i = 0; i < connections.size(); ++i){
      ((Connection)connections.get(i)).setOnline();
      //((Connection)connections.get(i)).getQueue().clear();
    }
    this.controller.frame.screen.repaint();
  }

  private boolean noTraffic(){
    for(int i = 0; i < this.connections.size(); ++i){
      if(!((Connection)this.connections.get(i)).getQueue().isEmpty())
        return false;
    }
    return true;
  }

  public void run(){
	//System.out.println("running...");
    while(this.controller.play){
    	//System.out.println("Size of Connections: " + connections.size() + "\nSize of Routers: " + routers.size() + "Using Metric: " + controller.frame.multipleMetrics());
      for(int i = 0; i < connections.size(); ++i){
        Connection con = (Connection)connections.get(i);
        con.calc(timeline.time());
      }
      //System.out.println("ROUTING INFORMATION");
      for(int i = 0; i < routers.size(); ++i){
        Router r = (Router)routers.get(i);
        // Check timers and triggered Updates
        //System.out.println("Simulator: Starting Check");
        r.rtable.check(timeline.time());
      }
      //System.out.println("ROUTING INFORMATION");
      //System.out.println("NODES");
      for(int i = 0; i < nodes.size(); ++i){
        Computer c = (Computer)nodes.get(i);
        if((c.getStart() != -1) && (c.getCycle() != 0) && (c.getStart() <= timeline.time()) && (((timeline.time()-c.getStart())%c.getCycle()) == 0))
          c.send();
      }
      //System.out.println("NODES");
      //System.out.println("SEND");
      int ready = 0;
      boolean r[] = new boolean[connections.size()];
      for(int i = 0; i < r.length; ++i)
        r[i] = false;
      boolean no = noTraffic();
      while(ready != connections.size()){
        for(int i = 0; i <= Consts.STEPS; ++i){
          for(int j = 0; j < connections.size(); ++j){
        	  
            if(r[j]){ // ready
              //System.out.println("continue");
              continue;
            }
            if(((Connection)connections.get(j)).makeStep(i)){
              if(r[j] == false){
                r[j] = true;
                ready++;
              }
            }
          }
          this.controller.screen.paintObjects(this.controller.screen.getGraphics());
          if(i == Consts.STEPS)
            this.controller.screen.repaint();
          try{
            th.sleep(Consts.delay());
          } catch(InterruptedException ie){
            this.controller.frame.term.write(ie.toString());
            System.exit(1);
          }
        }
      }
      //System.out.println("SEND");
      if(no){
        try{
          th.sleep(10);
        } catch(InterruptedException ie){
          this.controller.frame.term.write(ie.toString());
          //System.err.println(ie);
          System.exit(1);
          }
      }
      timeline.next();
      clock.setTime(timeline.time());
    }
  }

  public void calc(int steps){
    for(int j = 0; j < steps; ++j){
      for(int i = 0; i < connections.size(); ++i){
        Connection con = (Connection)connections.get(i);
        con.calc(timeline.time());
      }
      //System.out.println("ROUTING INFORMATION");
      for(int i = 0; i < routers.size(); ++i){
        Router r = (Router)routers.get(i);
        r.rtable.check(timeline.time());
      }
      //System.out.println("ROUTING INFORMATION");
      //System.out.println("NODES");
      for(int i = 0; i < nodes.size(); ++i){
        Computer c = (Computer)nodes.get(i);
        if((c.getCycle() != 0) && (c.getStart() <= timeline.time()) && (((timeline.time()-c.getStart())%c.getCycle()) == 0))
          c.send();
      }
      //System.out.println("NODES");
      //System.out.println("SEND");

      for(int i = 0; i < connections.size(); ++i){
        ((Connection)connections.get(i)).transmit();
      }
      //System.out.println("SEND");
      timeline.next();
      clock.setTime(timeline.time());
    }
    this.controller.screen.repaint();
  }
}
