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

public class LinkStateTable implements RoutingTable{
  public static final int COLS = 7;
  public static final int TYPE = 0;
  public static final int FROM = 1;
  public static final int DEST = 2;
  public static final int GATE = 3;
  public static final int COST = 4;
  public static final int TIME = 5;
  public static final int NUMB = 6;
  
  public static final int TYPE_LINK = 0; // Single-Link-Records
  public static final int TYPE_SUMMARY = 1; // Summary-Records
  
  public static final int MULTI = 1;

  private int[][][] table = null;
  protected int addr;
  private int status;
  private Router route = null;
  private int size;
  private int elements;
  private int lollipop = 1-Consts.OSPF_INF;
  private ArrayList neighbors = new ArrayList();
  private Hashtable wForAck = new Hashtable();
  private Hashtable multi = new Hashtable();
  transient private SPFAlgorithm spfalg = new SPFAlgorithm(this, 0);
  private boolean recalc = true;

  public LinkStateTable(Router route, int flags){
    this.route = route;
    this.addr = route.getAddress();
    this.status = flags;
    this.size = 4;
    this.elements = 0;
    this.table = new int[this.size][COLS][Consts.OSPFNUMBEROFMETRICS];
    this.status = flags;
  }
  
  private boolean getAllNeighborsFound(){
    int num = 0;
    ArrayList cons = this.route.getConnections();
    for(int i = 0; i < cons.size(); ++i){
      if(((Connection)cons.get(i)).online())
        num++;
    }
    return (this.neighbors.size() >= num);
  }
  
  // returns the cost for a specific connection and metric
  public int getCost(int from, int to, int metric){
    for(int i = 0; i < this.elements; ++i){
      if(this.table[i][FROM][0] == from && this.table[i][DEST][0] == to)
        return this.table[i][COST][metric];
    }
    return -1;
  }

  public int getLolly(){
    return this.lollipop;
  }

  public ArrayList getNodesExcept(int node){
    ArrayList result = new ArrayList();
    for(int i = 0; i < this.elements; ++i){
      if(table[i][FROM][0] != node && !result.contains(new Integer(table[i][FROM][0]))){
        result.add(new Integer(table[i][FROM][0]));
      }
      else if(table[i][DEST][0] != node && !result.contains(new Integer(table[i][DEST][0]))){
        result.add(new Integer(table[i][DEST][0]));
      }
    }
    return result;
  }

  // returns all paths from a node with the specific metric
  public ArrayList getPaths(int from, int metric){
	  
    ArrayList result = new ArrayList();
    if(this.table == null)
      return result;
    for(int i = 0; i < this.elements; ++i){
      if(table[i][FROM][0] == from) {
        result.add(new Path(from, table[i][DEST][0], table[i][COST][metric]));
      }
    }
    return result;
  }

  private void removeAll(int from){
    int i;
    while((i = indexOf(from)) != -1){
      this.remove(i);
    }
  }

  // returns all paths from the end of an existing path with the specific metric
  public ArrayList getNextPaths(Path p, int metric){
    ArrayList result = new ArrayList();
    for(int i = 0; i < this.elements; ++i){
      if(table[i][FROM][0] == p.last() /*&& this.indexOf(table[i][FROM], table[i][DEST]) != -1*/){ /*!!!! funktioniert ohne && nicht (warum?)  !!!!!!!*/
        Path newP = (Path)p.clone();
        newP.addNode(table[i][DEST][0], table[i][COST][metric]);
        //if(!result.contains(newP)){
        result.add(newP);
        //}
      }
    }
    return result;
  }

  public int next(){
    if(this.lollipop < Consts.OSPF_INF-2)
      this.lollipop += 1;
    else if(this.lollipop == Consts.OSPF_INF-2)
      this.lollipop = 0;
    return this.lollipop;
  }

  public int next(int last){
    if(last < Consts.OSPF_INF-2)
      return last++;
    else
      return 0;
  }

  public static int compareTo(int mylol, int yourlol){
    int halfcircle = (Consts.OSPF_INF-1)/2;
    if(mylol < 0 || yourlol < 0){
      if(mylol > yourlol)
        return 1;
      else if(mylol < yourlol)
        return -1;
      else
        return 0;
    }
    else{
      if(mylol == 0 && yourlol == 0)
        return 0;
      else if(mylol < yourlol){
        if(yourlol-mylol < halfcircle)
          return -1;
        else
          return 1;
      }
      else{
        if(mylol-yourlol < halfcircle)
          return 1;
        else
          return -1;
      }
    }
  }

  // always called before simulating routing Information
  public void check(long time){
    Neighbor n = null;
    for(int i = 0; i < neighbors.size(); ++i){ // outtime neighbors
      n = (Neighbor)neighbors.get(i);
      if(time-n.getTime() > Consts.HELLO_INTERVAL+1){ // remove Neighbor that didn't answer
        this.removeNeighbor(n.getNode());
      }
    }
    this.next(); // lollypop
    for(int i = 0; i < elements; ++i){
      table[i][TIME][0]++; // Aging of entries
      if(table[i][TIME][0] >= Consts.MAXAGE){
        int[][][] entry = remove(i);
        if(entry != null){
          OSPFFloodingPackage p = new OSPFFloodingPackage(this.addr, 0, -1, entry, this.getLolly());
          if(this.addr == entry[0][FROM][0] && containsNeighbor(entry[0][DEST][0])){
            this.removeNeighbor(entry[0][DEST][0]);
          }
          this.sendNeighbors(p); // Flood removal to all Neighbors
          i = Math.max(0, i-1);
        }
      }
    }    
    
    ArrayList cons = this.route.getConnections();
    for(int i = 0; i < cons.size(); ++i){
      Connection c = (Connection)cons.get(i);
      if(!c.online()) // remove unreachable Neighbors
        this.removeNeighbor(c.opposite(this.route));
    }
    if(this.route.getRoutingStart() != -1 && time >= this.route.getRoutingStart() && ((time-this.route.getRoutingStart())%Consts.HELLO_INTERVAL) == 0){
      //System.out.println("broadcast");
      this.route.broadcast();
    }
  }
  
  public int getGateTo(int to) {
	  return getGateTo(to,Package.M_UNUSED, false);
  }
  
  public int getGateTo(int to, boolean destinationIsArea) {
	  return getGateTo(to,Package.M_UNUSED, destinationIsArea);
  }

  // return gate to a certain destination with the specific metric
  public int getGateTo(int to, int metric, boolean destinationIsArea){
	  
	// if destination is an area, there is no need for the SPF-algorithm
	if(destinationIsArea) {
		for(int i = 0; i < table.length; i++) {
			if((table[i][TYPE][0] == TYPE_SUMMARY) && (table[i][DEST][0] == to)) {
				return table[i][GATE][0];
			}
		}
		return -1; // no summary link for the specified destination area could be found
	}
	  
	  
	  
    //if(recalc){
	  // create a new SPFAlgorithm with the given metric
      this.spfalg = new SPFAlgorithm(this, metric);
      if((this.status&MULTI) != 0)
        this.spfalg.calc(SPFAlgorithm.MULTI);
      else
        this.spfalg.calc(SPFAlgorithm.SINGLE);
      recalc = false;
    //}
    if((this.status&MULTI) != 0){
    	// compute all paths to a destination
      ArrayList paths = this.spfalg.getShortestPaths(to);
      if(paths.size() < 1)
        return -1;
      
      //paths = this.spfalg.getMostPaths(paths);
      //System.out.println("list\n"+paths+"\n-----------\n");
      Integer dest = new Integer(to);
      if(this.multi.get(dest) == null){
        this.multi.put(dest, new Integer(0));
      }
      int next = Math.min(((Integer)this.multi.get(dest)).intValue(), paths.size()-1);
      Path p = (Path)paths.get(next);
      this.multi.put(dest, new Integer((next+1)%paths.size()));
      //System.out.println(p);
      if(p != null){
        int node = p.nextHop();
        //System.out.println("node = "+node);
        ArrayList cons = this.route.getConnections();
        Connection c = null;
        for(int i = 0; i < cons.size(); ++i){
          c = (Connection)cons.get(i);
          if(c.opposite(this.route) == node){
            //System.out.println("FOUND");
            //System.out.println(c.toString());
            return c.getAddress();
          }
        }
      }
      return -1;
    }
    else{
      Path p = this.spfalg.getShortestPath(to);
      //System.out.println(this.addr+"PATH = "+p);
      if(p != null){
        int node = p.nextHop();
        ArrayList cons = this.route.getConnections();
        Connection c = null;
        for(int i = 0; i < cons.size(); ++i){
          c = (Connection)cons.get(i);
          if(c.opposite(this.route) == node){
            return c.getAddress();
          }
        }
      }
      return -1;
    }
  }

  public void removeNeighbor(int node){
    //System.out.println("REMOVE NEIGHBOR");
    Neighbor n = null;
    for(int i = 0; i < this.neighbors.size(); ++i){
      n = (Neighbor)this.neighbors.get(i);
      if(n.getNode() == node){
        //System.out.println("REMOVING-----------");
        this.neighbors.remove(i);
        int index = indexOf(this.addr, n.getNode());
        //System.out.println("index = "+index);
        int[][][] entry = null;
        int[][][] help = null;
        if(index != -1)
          help = this.remove(index); // remove link from this to n
        index = indexOf(n.getNode(), this.addr);
        if(index != -1){
           if(help != null){
             entry = new int[2][COLS][Consts.OSPFNUMBEROFMETRICS];
             int[][][] help2 = this.remove(index);
             for(int k = 0; k < COLS; ++k){
               entry[0][k] = help[0][k];
               entry[1][k] = help2[0][k];
             }
           }
           else
            entry = this.remove(index);
        }
        else
          entry = help;
        if(entry != null){
          OSPFFloodingPackage flood = new OSPFFloodingPackage(this.addr, 0, -1, entry, this.getLolly());
          this.sendNeighbors(flood);
        }
        return;
      }
    }
  }

  // return RoutingPackage for destination to
  public Package getPackageTo(int to){
    return null;
  }

  // when saving or starting new Simulator
  public void init(){
    spfalg = new SPFAlgorithm(this, 0);
    recalc = true;
  }

  // update rtable with inforamtion from a package
  public void update(int[][] dv, int from){
  }

  public void update(OSPFFloodingPackage p){
    int[][][] des = p.getDescription();
    //boolean[] flood = new boolean[des.length];
    int len = 0;
    for(int i = 0; i < des.length; ++i){
      if(des[i][FROM][0] == this.addr || des[i][DEST][0] == this.addr){
        //OSPFFloodingPackage flood = new OSPFFloodingPackage(this.addr, 0, this.getDescription(), this.getLolly());
        //this.sendNeighbors(flood);
        continue; // ignore local updates
      }      
      if(add(des[i], p.from())){
        len++;
      }
    }
    //int[][] data = new int[len][COLS];
    int[][][] data;
    if(this.route.isAreaBorderRouter())
    	data = this.getDescription(true);
    else
    	data = this.getDescription(false);
    int j = 0;
    for(int i = 0; i < data.length; ++i){
      //while(!flood[j])
      //  j++;
      //data[i][FROM] = des[j][FROM];
      //data[i][DEST] = des[j][DEST];
      //data[i][GATE] = des[j][GATE];
      //data[i][COST] = des[j][COST];
      //data[i][NUMB] = des[j][NUMB];
      data[i][TIME][0]++; // = ++(des[j][TIME]);
    }
    
    if(len > 0){
      OSPFFloodingPackage pack = new OSPFFloodingPackage(this.addr, 0, -1, data, this.getLolly());
      this.sendNeighbors(pack, p.from());
    }
  }

  private long time(){
    return this.route.scr.parent.controller.sim.timeline.time();
  }

  public boolean containsNeighbor(int node){
    Neighbor n = null;
    for(int i = 0; i < this.neighbors.size(); ++i){
      n = (Neighbor)neighbors.get(i);
      if(n.getNode() == node)
        return true;
    }
    return false;
  }

  public Neighbor getNeighbor(int node){
    Neighbor result = null;
    for(int i = 0; i < this.neighbors.size(); ++i){
      result = (Neighbor)neighbors.get(i);
      if(result.getNode() == node)
        return result;
    }
    return null;
  }

  public void update(OSPFExchangePackage p){
    Integer i = new Integer(p.from());
    boolean waiting = (wForAck.get(i) != null) ? ((Boolean)wForAck.get(i)).booleanValue() : false;
    if(waiting && p.isMaster() && p.isInit()){
      if(p.from() < this.addr){ // solve collision
        return;
      }
      wForAck.put(i, Boolean.FALSE);
      sendAcknolegement(p.from());
    }
    else if(!waiting && p.isMaster() && p.isInit()){
      wForAck.put(i, Boolean.FALSE);
      sendAcknolegement(p.from());
    }
    else if(waiting && !p.isMaster() && p.isInit()){ // receive Acknolegement
      OSPFExchangePackage descr = new OSPFExchangePackage(this.addr, p.from(), -1 );
      descr.setDescription(this.getDescription(false));
      descr.setMaster(true);
      wForAck.put(i, Boolean.FALSE);
      send(descr);
    }
    else if(!waiting && !p.isMaster() && !p.isInit()){
      // receive from Slave
      this.setDescription(p.getDescription());
      if(getAllNeighborsFound()){
        // master has to flood
    	 
        OSPFFloodingPackage flood = new OSPFFloodingPackage(this.addr, 0, -1, this.getDescription(false), this.getLolly());
        this.sendNeighbors(flood, p.from());
      }
    }
    else if(!waiting && p.isMaster() && !p.isInit()){
      // receive from master
      OSPFExchangePackage descr = new OSPFExchangePackage(this.addr, p.from(), -1);
      this.setDescription(p.getDescription());
      descr.setDescription(this.getDescription(false));
      send(descr);
      if(getAllNeighborsFound()){
        // slave
    	OSPFFloodingPackage flood = new OSPFFloodingPackage(this.addr, 0, -1, this.getDescription(false), this.getLolly());
        this.sendNeighbors(flood, p.from());
      }
    }
  }

  public void setDescription(int[][][] d){
    for(int i = 0; i < d.length; ++i){
    	this.add(d[i], 0);  // second parameter zero, since add() doesn't use it
    }
  }

  public int[][][] getDescription(boolean onlySummaryRecords){
	  int count = elements;
	  int k = 0;
	  if(onlySummaryRecords) {
		  count = 0;
		  for(int i = 0; i < elements; ++i)
			  if(this.table[i][TYPE][0] == TYPE_SUMMARY) {
			  	count++;
			  	//System.out.println("Type is summary " + count);
			  }
	  }
    int[][][] tmp = new int[count][COLS][Consts.OSPFNUMBEROFMETRICS];
    if(onlySummaryRecords) {
    	for(int i = 0; i < elements; ++i)
        	if(this.table[i][TYPE][0] == TYPE_SUMMARY) {
        		for(int j = 0; j < COLS; ++j)
        		  tmp[k][j] = this.table[i][j];
        		k++;
        	}
    } else {
		for(int i = 0; i < elements; ++i)
				for(int j = 0; j < COLS; ++j)
				  tmp[i][j] = this.table[i][j];
}
    return tmp;
  }

  // after receiving a OSPFHelloPackage, update LinkStateTable
  public void update(OSPFHelloPackage p, int gate, int[] metrics){
    if(!this.containsNeighbor(p.from())){
      //System.out.println("Added");
      this.neighbors.add(new Neighbor(p.from(), time()));
      int[][] data = new int[COLS][Consts.OSPFNUMBEROFMETRICS];
      data[TYPE][0] = p.getArea();
      data[FROM][0] = p.from();
      data[DEST][0] = this.addr;
      data[GATE][0] = gate;
      data[COST] = metrics;
      data[TIME][0] = 0;
      data[NUMB][0] = this.getLolly();
      this.add(data, 0);
      // start exchange procedure
      this.sendMasterPackage(p.from());
    }
    else{
      this.getNeighbor(p.from()).setTime(time());
    }
  }

  private void sendMasterPackage(int to){
    OSPFExchangePackage p = new OSPFExchangePackage(this.addr, to, -1);
    p.setMaster(true);
    p.setInit(true);
    this.wForAck.put(new Integer(to), Boolean.TRUE);
    //System.out.println("MasterPackageSent");
    this.send(p);
  }

  // sending slave packet
  private void sendAcknolegement(int to){
    OSPFExchangePackage p = new OSPFExchangePackage(this.addr, to, -1);
    p.setInit(true);
    this.send(p);
  }

  protected void send(Package p){
    Connection c;
    ArrayList cs = this.route.getConnections();
    for(int i = 0; i < cs.size(); ++i){
      c = (Connection)cs.get(i);
      if(c.online() && this.route.scr.getPaintable(p.to()) instanceof Router && c.opposite(this.route) == p.to()){
        c.getQueue().add(p, this.route);
      }
    }
  }

  protected void sendNeighbors(Package p){
    Neighbor n = null;
    Package newP = null;
    for(int i = 0; i < this.neighbors.size(); ++i){
      n = (Neighbor)this.neighbors.get(i);
      //if(p.getArea() == ((Router)this.route.scr.getPaintable(n.getNode())).getArea()) {
	      newP = (Package)p.clone();
	      newP.setDestination(n.getNode());
	      this.send(newP);
    //  } else
    //	  System.out.println("Not sending " + p.message() + " to " + n.getNode() + " because his area is " + ((Router)this.route.scr.getPaintable(n.getNode())).getArea() + " and packet's area is " + p.getArea());
    }
  }

  protected void sendNeighbors(Package p, int except){
    Neighbor n = null;
    Package newP = null;
    for(int i = 0; i < this.neighbors.size(); ++i){
      n = (Neighbor)this.neighbors.get(i);
//      if((n.getNode() != except) && (p.getArea() == ((Router)this.route.scr.getPaintable(n.getNode())).getArea())) {
        newP = (Package)p.clone();
        newP.setDestination(n.getNode());
        this.send(newP);
//      } else
//    	  if(n.getNode() != except)
//    		  System.out.println("Not sending " + p.message() + " to " + n.getNode() + " because his area is " + ((Router)this.route.scr.getPaintable(n.getNode())).getArea() + " and packet's area is " + p.getArea());
    }
  }

  private boolean contains(int[] entry){
    for(int i = 0; i < this.elements; ++i){
      if(table[i][FROM][0] == entry[FROM] && table[i][DEST][0] == entry[DEST] && table[i][GATE][0] == entry[GATE])
        return true;
    }
    return false;
  }

  // Terminal output
  public String toString(){
    String result = new String("Router "+this.addr+": routing table\n");
    result += "type\tsource\tdestination\tgateway\tcost 1 2 3 4 \t\tnumber\ttimer\n";
    for(int i = 0; i < this.elements; ++i){
      result += this.table[i][TYPE][0]+"\t";
      result += this.table[i][FROM][0]+"\t";
      result += this.table[i][DEST][0]+"\t";
      result += this.table[i][GATE][0]+"\t";
      result += this.table[i][COST][Package.M_DELAY]+" ";
      if((route.getOspfMode() & Consts.OSPFMULTIPLEMETRICS) != 0) {
    	  result += (Consts.INVERSEMETRICMAX - this.table[i][COST][Package.M_THROUGHPUT])+" ";
          result += (Consts.INVERSEMETRICMAX - this.table[i][COST][Package.M_RELIABILITY])+" ";
          result += this.table[i][COST][Package.M_COST]+"\t";
      }
      result += this.table[i][NUMB][0]+"\t";
      result += this.table[i][TIME][0]+"\n";
    }
    return result;
  }

  // true if table updated, false else
  public boolean add(int dest, int cost, int gate){
    System.err.println("DO NOT USE add(int dest, int cost, int gate)");
    System.exit(1);
    return true;
  }
  
  
  public void addHost(int from, int to, int[] metrics, int gate){
    if(!this.containsNeighbor(from)){
      //System.out.println("Added");
      this.neighbors.add(new Neighbor(from, time()));
      this.addFirst(from, to, metrics, 0, gate);
      if(getAllNeighborsFound()){
        OSPFFloodingPackage flood = new OSPFFloodingPackage(this.addr, 0, -1, this.getDescription(false), this.getLolly());
        this.sendNeighbors(flood);
      }
    }
    else{
      this.getNeighbor(from).setTime(time());
    }
  }

  // assembles database-records and adds them with add(int [][] values)
  public void addFirst(int from, int to, int[] metrics, int numb, int gate){
	int[][] data1 = new int[COLS][Consts.OSPFNUMBEROFMETRICS];
	data1[FROM][0] = from;
	data1[DEST][0] = to;
	data1[GATE][0] = gate;
	data1[COST] = metrics;
	data1[TIME][0] = numb;
	data1[NUMB][0] = this.getLolly();
    this.add(data1);
    
    //corresponding entry with source/destination switched
    int[][] data2 = new int[COLS][Consts.OSPFNUMBEROFMETRICS];
	data2[FROM][0] = to;
	data2[DEST][0] = from;
	data2[GATE][0] = gate;
	data2[COST] = metrics;
	data2[TIME][0] = numb;
	data2[NUMB][0] = this.getLolly();
    this.add(data2);
  }

  // set cost of gate to INF
  public void setINF(int from, int to, int gate, int metric){
    for(int i = 0; i < this.elements; ++i){
      if(table[i][FROM][0] == from && table[i][DEST][0] == to && table[i][GATE][0] == gate){
        this.table[i][COST][metric] = Consts.OSPF_INF;
        this.table[i][TIME][0] = Consts.MAXAGE;
      }
    }
  }

  // for RouterEditPane
  public String[][] getTable(){
	  String[][] result;
	  int offset = 0;
	// if using Multiple Metrics, RouterEditPane has more columns (offset = number of additional metrics)
	if((route.getOspfMode() & Consts.OSPFMULTIPLEMETRICS) != 0) {
		result = new String[this.elements][COLS + Consts.OSPFNUMBEROFMETRICS - 1];
		offset = Consts.OSPFNUMBEROFMETRICS - 1;
	}
	else
		result = new String[this.elements][COLS];
	
    for(int i = 0; i < this.elements; ++i){
      result[i][TYPE] = new Integer(this.table[i][TYPE][0]).toString();
      
      if(this.table[i][FROM][0] == -1) {
    	  result[i][FROM] = "";
      } else {
    	  result[i][FROM] = new Integer(this.table[i][FROM][0]).toString();
      }
      
      result[i][DEST] = new Integer(this.table[i][DEST][0]).toString();
      result[i][GATE] = new Integer(this.table[i][GATE][0]).toString();
      result[i][COST] = new Integer(this.table[i][COST][0]).toString();
      
      // if using MultipleMetrics, add further metric info
      if((route.getOspfMode() & Consts.OSPFMULTIPLEMETRICS) != 0) {
    	  result[i][COST + Package.M_THROUGHPUT] = (new Integer(Consts.INVERSEMETRICMAX - this.table[i][COST][Package.M_THROUGHPUT]).toString());
          result[i][COST + Package.M_RELIABILITY] = (new Integer(Consts.INVERSEMETRICMAX - this.table[i][COST][Package.M_RELIABILITY]).toString());
          result[i][COST + Package.M_COST] = new Integer(this.table[i][COST][Package.M_COST]).toString();
      }
      
      result[i][TIME+offset] = new Integer(this.table[i][TIME][0]).toString();
      result[i][NUMB+offset] = new Integer(this.table[i][NUMB][0]).toString();
    }
    return result;
  }

  // RouterEditPane
  public int[][][] remove(int index) {
    if(index < 0 || index >= elements)
      return null;
    int[][][] result = new int[1][COLS][Consts.OSPFNUMBEROFMETRICS];
    for(int i = 0; i < COLS; ++i) // copy removed entry
      result[0][i] = table[index][i];
    result[0][TIME][0] = Consts.MAXAGE;
    // move all following entries
    for(int i = index; i < elements-1; ++i){
      this.table[i][TYPE] = this.table[i+1][TYPE];
      this.table[i][FROM] = this.table[i+1][FROM];
      this.table[i][DEST] = this.table[i+1][DEST];
      this.table[i][GATE] = this.table[i+1][GATE];
      this.table[i][COST] = this.table[i+1][COST];
      this.table[i][NUMB] = this.table[i+1][NUMB];
      this.table[i][TIME] = this.table[i+1][TIME];
    }
    this.elements--;
    recalc = true;
    return result;
  }

  // parameter "from" not used?
  public boolean add(int[][] values, int from){
    // exists
    int index;
    
    // ignore 0 entries
    if(values[FROM][0] == 0 && values[DEST][0] == 0)
    	return false;
    
    if((index = indexOf(values[FROM][0], values[DEST][0])) != -1){
      if(values[TIME][0] >= Consts.MAXAGE){ // always accept
        for(int i = 0; i < COLS; ++i)
          this.table[index][i] = values[i];
        recalc = true;
        return false; // flooded from check()... don't flood here!!!!!
      }
      else{
        long diff = Math.abs(values[TIME][0]-table[index][TIME][0]);
        if(diff <= Consts.MAXAGEDIFF){
          // keep local value
          return false;
        }
        else{
          if(compareTo(this.table[index][NUMB][0], values[NUMB][0]) <= 0){
            // keep local value
            return false;
          }
          else{
            for(int i = 0; i < COLS; ++i)
              this.table[index][i] = values[i];
            recalc = true;
            return true;
          }
        }
      }
    }
    else{ // new
      if(values[TIME][0] >= Consts.MAXAGE)
        return false;
      this.add(values);
      return true;
    }
  }
  
  public void add(int[] values) {
	  int[][] tmp = new int[values.length][Consts.OSPFNUMBEROFMETRICS];
	  for(int i = 0; i < values.length; i++) {
		  tmp[i][0] = values[i];
	  }
	  add(tmp);
  }

  public void add(int[][] values){
	  if(values[FROM][0] == 0 && values[DEST][0] == 0) {
		  System.out.println("values are zero");
		  return;
	  }
    if(indexOf(values[0][0], values[1][0]) != -1)
      return;
    this.resize();
    this.table[elements][TYPE][0] = values[TYPE][0];
    this.table[elements][FROM][0] = values[FROM][0];
    this.table[elements][DEST][0] = values[DEST][0];
    this.table[elements][COST] = values[COST]; // add all metric information
    this.table[elements][GATE][0] = values[GATE][0];
    this.table[elements][NUMB][0] = values[NUMB][0];
    this.table[elements][TIME][0] = ++(values[TIME][0]);
    elements++;
    if(table[elements-1][FROM][0] == this.addr && !this.containsNeighbor(table[elements-1][DEST][0])){
      if(this.route.scr.getPaintable(table[elements-1][DEST][0]) instanceof Router){ // send Hello to reachable Routers
        this.send(new OSPFHelloPackage(this.addr, table[elements-1][DEST][0]));
      }
      else // add rechable Hosts to Neighborlist
        this.addHost(table[elements-1][DEST][0], this.addr, table[elements-1][COST], table[elements-1][GATE][0]);
    }
    recalc = true;
  }


  public int indexOf(int from, int dest){
    for(int i = 0; i < elements; ++i){
      if(table[i][FROM][0] == from && table[i][DEST][0] == dest)
        return i;
    }
    return -1;
  }

  public int indexOf(int from){
    for(int i = 0; i < elements; ++i){
      if(table[i][FROM][0] == from)
        return i;
    }
    return -1;
  }

  private void resize(){
    //if(size == 0)
    //  size = 1;
    /* we need more memory for the table */
    if(this.size == this.elements){
      size *= 2; /* double size */
      int[][][] tmp = new int[size][COLS][Consts.OSPFNUMBEROFMETRICS];
      /* copy current table */
      for(int i = 0; i < this.table.length; ++i){
        for(int j = 0; j < COLS; ++j){
          tmp[i][j] = this.table[i][j]; // copy table entries to the new table
        }
      }
      this.table = tmp;
    }/* END-if */
  }

  // clears the table (new, empty table space for 4 entries)
  public void clear(){
    this.table = new int[4][COLS][Consts.OSPFNUMBEROFMETRICS];
    this.elements = 0;
    this.size = 4;
    this.route.getInQueue().clear();
    lollipop = 1-Consts.OSPF_INF;
    neighbors = new ArrayList();
    wForAck = new Hashtable();
    multi = new Hashtable();
    recalc = true;
  }

  public void setAddress(int addr){
    this.addr = addr;
  }

  public void setStatus(int flag){
    this.status = flag;
  }
  
  public void updateOwnAreas() {
	  TreeSet areas = this.route.getAreas();
	    Iterator it = areas.iterator();
	    //int[][] temp = new int[areas.size()][COLS];
	    for(int i = 0; i < areas.size(); i++) {
	    	this.resize();
	    	this.table[i][TYPE][0] = TYPE_SUMMARY;
	    	this.table[i][FROM][0] = -1;
	    	this.table[i][DEST][0] = ((Integer)(it.next())).intValue();
	    	this.table[i][GATE][0] = 0;
	    	this.table[i][COST][0] = 1234;
	    	this.table[i][TIME][0] = 0;
	    	this.table[i][NUMB][0] = this.getLolly();
	    	elements++;
	    }
  }
}
