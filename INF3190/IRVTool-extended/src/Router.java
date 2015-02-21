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
import javax.swing.*;
import java.util.*;

public class Router implements Computer{
	
  private Queue inQueue = new Queue();
  private Point pos;
  private boolean marked = true;
  protected transient Screen scr;
  private ArrayList connections;
  private TreeSet areas;
  private static Image img = new ImageIcon(Router.class.getResource("icons/router.png")).getImage();
  private int addr;
  private long start = 0;
  private int cycle = 30;
  private int ospfMode = 0; // Routermode: Multiple Metrics, Multiple Areas, etc...
  protected RoutingTable rtable = null;

  public Router(Point pos, Screen scr){
    this.pos = pos;
    this.scr = scr;
    this.connections = new ArrayList();
    this.addr = Address.next();
    areas = new TreeSet();
    areas.add(0);
  }

  public void setRoutingTable(RoutingTable rtable){
    this.rtable = rtable;
  }

  public void setRoutingStart(long start){
    this.start = start;
  }

  public void setScreen(Screen s){
    this.scr = s;
  }

  public long getStart(){
    return 0;
  }

  public long getRoutingStart(){
    return this.start;
  }

  public Queue getInQueue(){ return this.inQueue;}

  public String toString(){
    String result = "Router:\n\taddress:\t"+this.addr;
    if((ospfMode&Consts.OSPFMULTIPLEAREAS) != 0) {
    	result += "\n\tareas:\t";
    	Iterator i = areas.iterator();
    	while(i.hasNext()) {
    		result += i.next().toString() + " ";
    	}
    }
    
    
    result += "\n\tstart:\t"+this.start+"\n\tcycle:\t"+this.cycle+"\n\tconnections:";
    for(int i = 0; i < connections.size(); ++i){
      if(connections.get(i) != null)
        result += "\n\t\t"+((Connection)connections.get(i)).getAddress();
    }
    return result;
  }

  public synchronized void broadcast(){
	  //System.out.println("broadcasting...");
	  
    Object[] cons = this.connections.toArray();     // get all connections of this router
    //System.out.println("Size of Connections: " + cons.length);
    Arrays.sort(cons);                              // sort the connections in increasing order of their addresses
    // if router uses RIP -------------------------------
    if(this.rtable instanceof DistanceVectorTable){
      for(int i = 0; i < cons.length; ++i){         // check all connections
        Connection c = (Connection)cons[i];
        //System.out.println("Connection " + i + " status: online: " + c.online() + ", address: " + c.getAddress() + ", cost: " + c.getCost());
        // if connections is down -> set cost to INF
        if(!c.online())
          ((DistanceVectorTable)this.rtable).setINF(c.getAddress());
        // else if connection is up and the node on the other side is a host, add that host to the routing table
        else if(c.online() && (this.scr.getPaintable(c.opposite(this)) instanceof Host))
          this.rtable.add(c.opposite(this), 0, c.getAddress());
      }
      // check all connections
      for(int i = 0; i < cons.length; ++i){
        Connection c = (Connection)cons[i];
        // if the connection is online send an update to the opposite node
        if(c.online()){
          c.getQueue().add(rtable.getPackageTo(c.getAddress()), this);
        }
      }
    }
    // END RIP
    // if router uses OSPF -------------------------------
    else if(this.rtable instanceof LinkStateTable || ((ospfMode & Consts.OSPFMULTIPLEMETRICS) != 0)){
    	//System.out.println("broadcasting OSPF");
      // check all connections
      for(int i = 0; i < cons.length; ++i){
        Connection c = (Connection)cons[i];
        
        // if connection is down -> do nothing
        if(!c.online()){
          continue;
        }
        // otherwise
        else{
          // send Hello to reachable Routers
          if(this.scr.getPaintable(c.opposite(this)) instanceof Router){
        	  ((LinkStateTable)this.rtable).send(new OSPFHelloPackage(this.addr, c.opposite(this)));
          }
          // add rechable Hosts to the routing table
          else
        	  ((LinkStateTable)(this.rtable)).addHost(c.opposite(this), this.addr, c.getMetrics(), c.getAddress());
        }
      }
    }
    // END OSPF
  }

public void send(int to, String msg){
	System.out.println("public void send(int to, String msg)");
    this.inQueue.add(new IPPackage(this.addr, to, msg), this);
  }

  public void send(int to, String msg, int ttl){
	System.out.println("public void send(int to, String msg, int ttl)");
    IPPackage p = new IPPackage(this.addr, to, msg);
    p.setTTL(ttl);
    this.inQueue.add(p, this);
  }

  public void send(){
	
    Object[] o;
    Package p;
    // for all packets in the queue
        
    while((o = this.inQueue.remove()) != null){
      p = (Package)o[0];
      
      Connection c;
      // get address of connection to destination of the packet
      int gate = 0;
      
      boolean destinationIsArea = false;
      
      if(this.rtable instanceof LinkStateTable) {
	      if((ospfMode & Consts.OSPFMULTIPLEMETRICS) != 0) {
	    	  destinationIsArea = !this.areas.contains(p.getArea());
	    	  // get the gate for a specific destination with the packet's metric
	    	  // (no need to check (rtable instanceof LinkStateTable), since Multiple Metrics can
	    	  // only be used with Link State Routing
	    		  gate = ((LinkStateTable)(rtable)).getGateTo(p.to(),p.getMetric(), destinationIsArea);
	      } else { // if not using Multiple Metrics, simply get the needed gate
	    	  gate = ((LinkStateTable)(rtable)).getGateTo(p.to(),destinationIsArea);
	      }
      } else {
    	  gate = ((DistanceVectorTable)rtable).getGateTo(p.to());
      }

      // for all connections
      for(int i = 0; i < connections.size(); ++i){
        c = (Connection)connections.get(i);
        // packet is a RIP packet
        if(p instanceof RIPPackage){
          // if node on the other side of this connection is source update routing table
          if(c.opposite(this) == p.from()){
            rtable.update(((RIPPackage)p).getDistanceVector(), c.getAddress());
            continue;
          }
          // otherwise do nothing
          continue;
        }
        // packet is a OSPF flooding packet and addressed to this router
        else if(p instanceof OSPFFloodingPackage && p.to() == this.addr){ // receive flooding package
          // update link state database
          if(c.opposite(this) == p.from()){
        	((LinkStateTable)rtable).update((OSPFFloodingPackage)p);
            continue;
          }
          continue;
        }
        else if(p instanceof OSPFHelloPackage && p.to() == this.addr){ // receive hello package
          if(c.opposite(this) == p.from()){
        	  // Update rtable with the appropriate hellopackage, gate-address and gate-metrics
        	((LinkStateTable)rtable).update((OSPFHelloPackage)p, c.getAddress(), c.getMetrics());
            continue;
          }
          continue;
        }
        else if(p instanceof OSPFExchangePackage && p.to() == this.addr){ // receive exchange package
          if(c.opposite(this) == p.from()){
        	((LinkStateTable)rtable).update((OSPFExchangePackage)p);
            continue;
          }
          continue;
        }
        // if their is no way to the destination or the appropriate connection is down
        else if(gate == -1 || (c.getAddress() == gate && !c.online())){
          // if packet is traceroute packet, print message
          if(p.message().equals("TRACE") || p.message().equals("TRACEBACK"))
            this.scr.parent.term.write("no route to node");
          //System.out.println("NO ROUTE");
          // else do nothing
          break;
        }
        // there is a way to the destination and the appropriate connection is up
        else if(c.getAddress() == gate && c.online()){
          // add the packet to the queue of the connection
        	//System.out.println("Sending packet: " + p.message());
          c.getQueue().add(p, this);
        }
        // the destination of the packet is this router
        else if(p.to() == this.addr){
        	
          // receive packet
          this.receive(p);
          break;
        }
        else continue;
      }
    }
  }

  public int getCycle(){
    return 1;
  }

  public int getRoutingCycle(){
    return this.cycle;
  }

  public void setCycle(int c){
    this.cycle = c;
  }

  public void setStatus(int flag){
    if(this.rtable != null)
      this.rtable.setStatus(flag);
  }

  public void setAddress(int addr){
    this.addr = addr;
    if(this.rtable != null)
      this.rtable.setAddress(addr);
    	if(this.rtable != null) 
    		this.rtable.setAddress(addr);
    }

  public int getAddress(){
    return this.addr;
  }

  public int getDiam(){
    return DIAM;
  }

  public ArrayList getConnections(){
    return this.connections;
  }

  public boolean connect(Connection c){
    if(this.connections.contains(c))
      return false;
    this.connections.add(c);
    return true;
  }

  public void disconnect(Connection c){
    int i;
    if(this.connections != null && this.connections.contains(c)){
      i = this.connections.indexOf(c);
      this.connections.remove(i);
    }
  }

  public Point getPosition(){
    return pos;
  }

  public void receive(Package p){
    // don't receive null packets; should never happen
    if(p == null) {
    	System.err.println("null-packet received!");
    	System.exit(1);
    }
	// packet is designated to this router
    if(p.to() == this.addr){
      // it is a PING packet
      if(p.message().equals("PING"))
        // return PONG packet
        this.send(p.from(), "PONG");
      // it is a PONG packet
      else if(p.message().equals("PONG"))
        // print message to terminal
        this.scr.parent.term.write(this.addr+": one package received as answer to PING");
      // answer to traceroute
      else if(p.message().equals("TRACEBACK")){
        // if address is this address
        if(this.scr.parent.term.a == p.from())
          this.scr.parent.term.write(""+p.from());
        else
          this.scr.parent.term.write("  "+p.from());
      }
      // answer to traceroute
      else if(p.message().equals("TRACE")){
        this.send(p.from(), "TRACEBACK");
      }
      // RIP packets are handled earlier
      else if(p.message().equals("RIP-Package"))
        ;//System.out.println("RIP");
      else if(p.message().equals("OSPF_HELLO-Package"))
        this.inQueue.add(p, this);
      else if(p.message().equals("OSPF_EXCHANGE-Package"))
        this.inQueue.add(p, this);
      else if(p.message().equals("OSPF_FLOODING-Package"))
        this.inQueue.add(p, this);
      // if packet is not a packet of a routing protocol print message
      else
        this.scr.parent.term.write(this.addr+": received message \""+p.message()+"\" from "+p.from());
    }
    // always answer to traceroute; otherwise terminal could block
    else if(p.getTTL() <= 0 && p.message().equals("TRACE")){
        this.send(p.from(), "TRACEBACK");
    }
    // only relay packets, that which TTL has not expired
    if(p.getTTL() > 0 && p.to() != this.addr){
      //System.out.println("MESSAGE = "+p.message());
      this.inQueue.add(p, this);
    }
  }

  public void paint(Graphics g){
    //Graphics g = scr.getGraphics();
    Color old = g.getColor();
    this.shape(g, false);
    g.setColor(old);
  }

  public void shape(Graphics g, boolean delete){
    if(delete){
      this.drawAddress(g, this.scr.getBackground());
      g.setColor(this.scr.getBackground());
      g.fillOval(pos.x - DIAM/2 -3, pos.y - DIAM/2 - 3, DIAM + 6, DIAM + 6);
    } else{
      g.drawImage(this.img, pos.x - DIAM/2, pos.y - DIAM/2, this.scr);
      this.drawAddress(g, Color.GRAY);
    }
    /*
    g.setColor(c2);
    g.drawOval(pos.x - DIAM/2, pos.y - DIAM/2, DIAM, DIAM);
    g.drawLine(pos.x - DIAM/4, pos.y, pos.x + DIAM/4, pos.y);
    g.drawLine(pos.x - DIAM/4, pos.y - DIAM/4, pos.x + DIAM/4, pos.y - DIAM/4);
    g.drawLine(pos.x - DIAM/4, pos.y - DIAM/4, pos.x - DIAM/4, pos.y + DIAM/4);
    g.drawLine(pos.x + DIAM/4, pos.y - DIAM/4, pos.x + DIAM/4, pos.y);
    g.drawLine(pos.x + DIAM/8, pos.y, pos.x + DIAM/4, pos.y + DIAM/4);*/
  }

  private void drawAddress(Graphics g, Color c){
	String tmp ="";
	
	if((this.scr.extendedOSPFDisplay&Consts.OSPFMULTIPLEAREAS) != 0) {
		if(this.isAreaBorderRouter())
			tmp = "ABR ";
		else {
			Iterator i = areas.iterator();
			tmp = i.next()+".";
		}
	}
	tmp += this.addr;
			
    g.setColor(c);
    g.setFont(new Font("SansSerif", Font.PLAIN, 20));
    g.drawString(tmp, pos.x-10, pos.y-DIAM/2-2);
  }

  public void delete(){
    this.mark(false);
    this.scr.remove(this);
    Object[] cons = this.connections.toArray();
    for(int i = 0; i < cons.length; ++i)
      ((Connection)cons[i]).delete();
    this.connections.clear();
    this.connections = null;
    this.shape(scr.getGraphics(), true);
  }

  private void paintMark(Graphics g, Color c){
    g.setColor(c);
    g.fillRect(pos.x - DIAM/2, pos.y - DIAM/2, 6, 6);
    g.fillRect(pos.x - DIAM/2, pos.y + DIAM/2 - 6, 6, 6);
    g.fillRect(pos.x + DIAM/2 - 6, pos.y - DIAM/2, 6, 6);
    g.fillRect(pos.x + DIAM/2 - 6, pos.y + DIAM/2 -6, 6, 6);
    this.scr.paintObjects(scr.getGraphics());
  }

  public boolean isMarked(){
    return this.marked;
  }

  public void mark(boolean m){
      this.marked = m;
      this.paintMark(scr.getGraphics(), (m ? this.markc : scr.getBackground()));
      scr.requestFocus();
  }

  public void setPosition(Point p){
    this.pos = p;
  }

  public boolean contains(Point p){
      if(p.x > (pos.x - DIAM/2) && p.x < (pos.x + DIAM/2) && p.y > (pos.y - DIAM/2) && p.y < (pos.y + DIAM/2))
        return true;
      else return false;
  }

  // returns true if this router is in MM-Mode, false otherwise
  public int getOspfMode() {
	return ospfMode;
  }

  // set MM-Mode for this router
  public void setOspfMode(int mode) {
	this.ospfMode = mode;
  }
  
  public TreeSet getAreas() {
	  return areas;
  }
  
  public void setAreas(TreeSet newSet) {
	  areas = newSet;
	  ((LinkStateTable)rtable).updateOwnAreas();
  }
  
  public boolean isAreaBorderRouter() {
	  return areas.size() > 1;
  }

}
