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

public class DistanceVectorTable implements RoutingTable{
	
  public static final int COLS = 5;
  public static final int DEST = 0;
  public static final int COST = 1;
  public static final int GATE = 2;
  public static final int TIME = 3; /* index of timestamp column */
  public static final int GC = 4;
  public static final int LOCAL = 0;
  public static final int SH = 1; /* split horizon */
  public static final int SH_PR = 2; /* split horizon with poisoned reverse */
  public static final int TRIGG = 4; /* triggered updates */

  private int[][] table;
  private int size;
  private int elements;
  private int status;
  private int addr;
  private Router router;

  public DistanceVectorTable(Router router, int flag){
    this(router, 4, flag);
  }

  public DistanceVectorTable(Router router, int size, int flag){
    this.table = new int[size][COLS];
    this.size = size;
    this.router = router;
    this.table[0][DEST] = router.getAddress();
    this.table[0][COST] = 0;
    this.table[0][GATE] = LOCAL;
    this.table[0][TIME] = 180;
    this.table[0][GC] = -1;
    this.elements = 1;
    this.status = flag;
    this.addr = router.getAddress();
  }

  public void setStatus(int flag){
    this.status = flag;
  }

  public void setAddress(int a){
    int i = indexOf(this.addr);
    if(i != -1){
      this.addr = a;
      this.table[i][DEST] = a;
    }
  }

  public void init(){
    for(int i = 0; i < router.getConnections().size(); ++i){
      Connection c = (Connection)router.getConnections().get(i);
      if(c.online())
        add(c.opposite(this.router), 0, c.getAddress());
    }
  }

  public String[][] getTable(){
    String[][] result = new String[this.elements][COLS];
    for(int i = 0; i < this.elements; ++i){
      result[i][0] = new Integer(this.table[i][DEST]).toString();
      result[i][2] = new Integer(this.table[i][COST]).toString();
      result[i][1] = new Integer(this.table[i][GATE]).toString();
      result[i][3] = new Integer(this.table[i][TIME]).toString();
      result[i][4] = new Integer(this.table[i][GC]).toString();
    }
    return result;
  }

  public void update(int[][] dv, int from){
    boolean modified = false;
    for(int i = 0; i < dv.length; ++i){
      //System.out.println("["+this.addr+"] COST: "+dv[i][COST]+" DEST: "+dv[i][DEST]+" GATE: "+from);
      if(this.add(dv[i][DEST], dv[i][COST], from)){
        modified = true;
      }
    }
    /* triggered update */
    if(modified && ((this.status&TRIGG) != 0)){
      //System.out.println("Triggered Update");
      this.router.broadcast();
    }
    //System.out.println(this);
  }

  private String addr2dottedNotation(int addr){
    return getOctet(addr, 3).append('.').append(getOctet(addr, 2)).append('.').append(getOctet(addr, 1)).append('.').append(getOctet(addr, 0)).toString();
  }

  private StringBuffer getOctet(int addr, int num){
    StringBuffer tmp = new StringBuffer().append((addr>>(num*8))&255);
    while(tmp.length() != 3)
      tmp.insert(0, '0');
    return tmp;
  }

  private String cost(int cost){
    if(cost == Consts.INF)
      return " INF";
    StringBuffer tmp = new StringBuffer().append(cost);
    while(tmp.length() != 4)
      tmp.insert(0, ' ');
    return tmp.toString();
  }

  private String gate(int gate){
    if(gate == LOCAL)
      return "  LOCAL";
    StringBuffer tmp = new StringBuffer().append(gate);
    while(tmp.length() != 7)
      tmp.insert(0, ' ');
    return tmp.toString();
  }

  public String toString(){
    String result = new String("Router "+this.addr+": routing table\n");
    result += "destination\tgateway\tcost\ttime\tgc\n";
    for(int i = 0; i < this.elements; ++i){
      result += this.table[i][DEST]+"\t";
      result += this.table[i][GATE]+"\t";
      result += this.table[i][COST]+"\t";
      result += this.table[i][TIME]+"\t";
      result += this.table[i][GC]+"\n";
    }
    return result;
  }

  public int indexOf(int dest){
    for(int i = 0; i < this.elements; ++i){
      if(this.table[i][DEST] == dest)
        return i;
    }
    return -1;
  }

  public boolean contains(int dest){
    if(this.indexOf(dest) != -1)
      return true;
    return false;
  }

  public void clear(){
    this.table = new int[4][COLS];
    this.elements = 0;
    this.size = 4;
    this.router.getInQueue().clear();
  }

  public boolean add(int dest, int cost, int gate){
    int index;
    boolean modified = false;
    int c = ((Connection)this.router.scr.getPaintable(gate)).getCost();
    int d = Math.min((cost+c), Consts.INF);
    /* there was already an entry for this destination */
    if((index = this.indexOf(dest)) != -1){
      /* if the new distance is smaller or the update came from the gateway that was already used */
      if(gate == this.table[index][GATE] || d < this.table[index][COST]){
        if(this.table[index][COST] != d)
          modified = true;
        if(this.table[index][COST] == Consts.INF)
          this.table[index][TIME] = 0; /* don't change GC */
        else {
          this.table[index][GC] = (d == Consts.INF) ? Consts.GC_TIMER : -1;
          this.table[index][TIME] = Consts.TIMER;
        }
        this.table[index][COST] = d;
        this.table[index][GATE] = gate;
        /* table has changed */
        return modified;
      }
     /* the destination is new */
    } else{
      this.resize();
      /* add new entry */
      this.table[elements][DEST] = dest;
      this.table[elements][COST] = d;
      this.table[elements][GATE] = gate;
      this.table[elements][TIME] = Consts.TIMER;
      this.table[elements][GC] = (d == Consts.INF) ? Consts.GC_TIMER : -1;
      elements++;
      /* table has changed */
      return true;
    } /* END-else */
    /* no changes in table */
    return false;
  }

  private void resize(){
    //if(size == 0)
    //  size = 1;
    /* we need more memory for the table */
    if(this.size == this.elements){
      size *= 2; /* double size */
      int[][] tmp = new int[size][COLS];
      /* copy current table */
      for(int i = 0; i < this.table.length; ++i){
        for(int j = 0; j < COLS; ++j){
          tmp[i][j] = this.table[i][j];
        }
      }
      this.table = tmp;
    }/* END-if */
  }

  public void add(int[] values){
    if(indexOf(values[DEST]) != -1){
      //System.out.println("RETURN");
      return;
     }
    this.resize();
    this.table[elements][DEST] = values[DEST];
    this.table[elements][COST] = Math.min(values[COST], Consts.INF);
    this.table[elements][GATE] = values[GATE];
    this.table[elements][TIME] = values[TIME];
    this.table[elements][GC] = values[GC];
    elements++;
    //System.out.println("ENTRY-ADDED");
  }

  public void setINF(int gate){
    for(int i = 0; i < this.elements; ++i){
      if(this.table[i][GATE] == gate){
        this.table[i][COST] = Consts.INF;
        this.table[i][TIME] = 0;
        this.table[i][GC] = Consts.GC_TIMER;
      }
    }
  }

  public void check(long time){
    for(int i = 0; i < this.elements; ++i){
      if(this.table[i][COST] == Consts.INF)
        this.table[i][TIME] = 0;
      if(this.table[i][GC] == -1 && this.table[i][TIME] == 0){
        this.table[i][GC] = Consts.GC_TIMER;
        this.table[i][COST] = Consts.INF;
      }
      if(this.table[i][GC] == 0){
        remove(i);
        i = Math.max(0, i-1);
      }
    }
    for(int i = 0; i < this.elements; ++i){
      if(this.table[i][GATE] != LOCAL && this.table[i][GC] == -1)
        this.table[i][TIME]--;
      else if(this.table[i][GATE] != LOCAL)
        this.table[i][GC]--;
    }
    if(this.router.getRoutingStart() != -1 && time >= this.router.getRoutingStart() && ((time-this.router.getRoutingStart())%this.router.getRoutingCycle()) == 0)
      this.router.broadcast();
    else if((this.status&TRIGG) != 0){
      ArrayList cons = this.router.getConnections();
      for(int i = 0; i < cons.size(); ++i){
        Connection c = (Connection)cons.get(i);
        ArrayList constr = c.getConstraints();
        for(int j = 0; j < constr.size(); ++j){
          Point point = (Point)constr.get(j);
          if(time == point.x || time == point.y) // remove unreachable Neighbors
            this.router.broadcast();
        }
      }
    }
  }

  public int[][] remove(int index){
    if(index < 0 || index >= elements)
      return null;
    for(int i = index; i < elements-1; ++i){
      this.table[i][DEST] = this.table[i+1][DEST];
      this.table[i][COST] = this.table[i+1][COST];
      this.table[i][GATE] = this.table[i+1][GATE];
      this.table[i][TIME] = this.table[i+1][TIME];
      this.table[i][GC] = this.table[i+1][GC];
    }
    this.elements--;
    return null;
  }

  public int getCostTo(int dest){
    int i;
    if((i = this.indexOf(dest)) != -1){
      return this.table[i][COST];
    }
    return -1;
  }

  public int getGateTo(int dest){
    int i;
    if((i = this.indexOf(dest)) != -1 && this.table[i][COST] != Consts.INF){
      return this.table[i][GATE];
    }
    return -1;
  }

  /**
  * @param togate The gateway wich we send this distance vector.
  * @return A distance vector (containing destinations and their costs) is returned
  */
  public int[][] getDistanceVector(int togate){
    int[][] result = new int[this.elements][2];
    int len = 0;
    for(int i = 0; i < this.elements; ++i){
      /* the neighbour from whom we got this route */
      if(this.table[i][GATE] == togate){
        /* split horizon: don't send to neighbours from whom we got an update */
        if((this.status&SH) != 0){
          //System.out.println("SPLIT HORIZON");
          continue;
        /* split horizon with poisoned reverse: send cost of INF to neighbours from whom we got an update */
        } else if((this.status&SH_PR) != 0){
          //System.out.println("SPLIT HORIZON WITH POISONED REVERSE");
          /* set costs to INF */
          result[len][COST] = Consts.INF;
        } else
          result[len][COST] = this.table[i][COST];
      } // togate != GATE
      else{
        result[len][COST] = this.table[i][COST];
      }
      result[len][DEST] = this.table[i][DEST];
      len++;
    }
    if(len != this.elements){
      int[][] tmp = new int[len][2];
      for(int i = 0; i < len; ++i){
        tmp[i][DEST] = result[i][DEST];
        tmp[i][COST] = result[i][COST];
      }
      result = tmp;
    }
    return result;
  }

  public Package getPackageTo(int togate){
    int[][] dv = this.getDistanceVector(togate);
    RIPPackage test = new RIPPackage(this.router.getAddress(), togate, dv);
    System.out.println(test.message());
    if(dv.length == 0)
      return null;
    return test;
  }

public void add(int[][] values) { // Should never be called!
	return;
	
}
  
  
}
