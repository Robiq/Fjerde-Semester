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

public class SPFAlgorithm{
  protected ArrayList evaluated = null;
  //protected ArrayList remaining = null;
  protected SortedList ordered = null;
  protected ArrayList shortest = null;
  private LinkStateTable table = null;
  private int src;
  public static final int MULTI = 0;
  public static final int SINGLE = 1;
  private int metric;

  // SPFAlgorithm needs metric if using Multiple Metrics (if not, metric = 0)
  public SPFAlgorithm(LinkStateTable lst, int metric){
    this.table = lst;
    this.src = lst.addr;
    this.metric = metric;
    this.init();
  }

  public void init(){
    this.evaluated = new ArrayList();
    this.shortest = new ArrayList();
    this.ordered = new SortedList();
    //this.remaining = new ArrayList();
    this.evaluated.add(new Integer(this.src));
    //this.remaining = this.table.getNodesExcept(this.src);
    this.ordered.addAll(this.table.getPaths(this.src, metric));
  }

  public void calc(int flag){
    //init();
    switch(flag){
      case MULTI:
        this.calcMultiple();
        break;
      case SINGLE:
        this.calcSingle();
        break;
    }
  }

  private void calcMultiple(){
    Path sh = null;
    while(!ordered.isEmpty()){ // step 2
      // step 3
      sh = (Path)ordered.first();
      ordered.remove(sh);
      if(evaluated.contains(new Integer(sh.last()))){
        // step 4
        Path last = null;
        for(int i = 0; i < this.shortest.size(); ++i){
          last = (Path)this.shortest.get(i);
          if(last.last() == sh.last() && last.first() == sh.first() && last.getCost() == sh.getCost()){
            this.shortest.add(sh);
            //if(!last.equals(sh))
              ordered.addAll(this.table.getNextPaths(sh, metric));
            break;
          }
        }
      }
      else{
        // step 5
        this.shortest.add(sh);
        //remaining.remove(new Integer(sh.last()));
        evaluated.add(new Integer(sh.last()));
        ordered.addAll(this.table.getNextPaths(sh, metric));
      }
    }
    //this.completePaths();
  }
  
  private void completePaths(){
    Path[] paths = new Path[this.shortest.size()];
    for(int i = 0; i < this.shortest.size(); ++i)
      paths[i] = (Path)this.shortest.get(i);
    Arrays.sort(paths);
    ArrayList sh = null;
    Path p = null;
    for(int i = paths.length-1; i >= 0; i--){
      int lastButOne = paths[i].lastButOne();
      int last = paths[i].last();
      if((sh = getShortestPaths(lastButOne)).size() > 1){
        for(int j = 0; j < sh.size(); ++j){
          p = (Path)((Path)sh.get(j)).clone();
          p.addNode(last, this.table.getCost(lastButOne, last, metric));
          if(!this.shortest.contains(p)){
            this.shortest.add(p);
          }
        }
      }
    }
    if(paths.length != this.shortest.size())
      this.completePaths();
  }
  
  private void calcSingle(){
    Path sh = null;
    do{
      do{
        if(ordered.isEmpty() || ((Path)ordered.first()).getCost() == Consts.OSPF_INF){
          // mark all nodes left in remaining as unreachable
          return;
        }
        sh = (Path)ordered.first();
        ordered.remove(sh);
      } while(evaluated.contains(new Integer(sh.last())));
      this.shortest.add(sh);
      //remaining.remove(new Integer(sh.last()));
      evaluated.add(new Integer(sh.last()));
      ordered.addAll(this.table.getNextPaths(sh, metric));
    } while(true);
  }

  /*public Iterator iterator(){
    return this.ordered.iterator();
  }*/

  public void setSource(int s){
    this.src = s;
    this.init();
  }

  public ArrayList getShortestPaths(int dest){
    ArrayList result = new ArrayList();
    ArrayList already = new ArrayList();
    //System.out.println("THIS---------\n"+this.shortest+"\n----------");
    for(int i = 0; i < this.shortest.size(); ++i){
      Path sh = (Path)this.shortest.get(i);
      Integer next = new Integer(sh.nextHop());
      if(sh.last() == dest && !already.contains(next)){
        already.add(next);
        result.add(sh);
      }
    }
    //System.out.println("-------------\n"+result+"\n----------");
    already = null;
    /*while(result.size() > 1){
      Path test1 = (Path)result.get(result.size()-1);
      Path test2 = (Path)result.get(result.size()-2);
      if(test1.getCost() > test2.getCost())
        result.remove(result.size()-1);
      else
        break;
    }
    if(this.src == 7 && result.size() != 0)
      System.out.println(result+"\n");*/
    return result;
  }

  public Path getShortestPath(int dest){
    for(int i = 0; i < this.shortest.size(); ++i){
      Path sh = (Path)this.shortest.get(i);
      if(sh.last() == dest)
        return sh;
    }
    return null;
  }

  public int getNumberOfShortestPaths(int dest){
    return this.getShortestPaths(dest).size();
  }
  
/*  public static void main(String[] argv){
    Router router = new Router(null, null);
    LinkStateTable table = new LinkStateTable(router, 0);
    int[] a = new int[6];

    a[0] = 1;
    a[1] = 2;
    a[2] = 1;
    a[3] = 5;
    a[0] = 1;
    a[0] = 1;
    table.add(a);
    a[0] = 2;
    a[1] = 3;
    a[3] = 6;
    table.add(a);
    a[0] = 3;
    a[1] = 4;
    a[3] = 7;
    table.add(a);
    a[0] = 4;
    a[1] = 1;
    a[3] = 8;
    table.add(a);
    a[0] = 1;
    a[1] = 3;
    a[3] = 9;
    table.add(a);
    a[0] = 2;
    a[1] = 1;
    a[3] = 5;
    table.add(a);
    a[0] = 3;
    a[1] = 2;
    a[3] = 6;
    table.add(a);
    a[0] = 4;
    a[1] = 3;
    a[3] = 7;
    table.add(a);
    a[0] = 1;
    a[1] = 4;
    a[3] = 8;
    table.add(a);
    a[0] = 3;
    a[1] = 1;
    a[3] = 9;
    table.add(a);

    SPFAlgorithm alg = new SPFAlgorithm(table, 0);
    //System.out.println(alg.evaluated);
    //System.out.println("--");
    //System.out.println(alg.remaining);
    //System.out.println("--");
    //System.out.println(alg.ordered.toString());
    //System.out.println("--");
    alg.setSource(2);
    alg.calc(alg.MULTI);
    System.out.println("\n----------------"+alg.shortest+"\n----------------");
    //System.out.println(alg.getShortestPaths(2));
    //System.out.println(alg.getShortestPaths(3));
    //System.out.println(alg.getShortestPaths(4));
    //alg.setSource(2);
    //alg.calc(alg.MULTI);
    //System.out.println(alg.getShortestPaths(1));
    //System.out.println(alg.getShortestPaths(2));
    //System.out.println(alg.getShortestPaths(3));
    //System.out.println(alg.getShortestPaths(4));
    //alg.setSource(3);
    //alg.calc(alg.MULTI);
    //System.out.println(alg.getShortestPaths(1));
    //System.out.println(alg.getShortestPaths(2));
    //System.out.println(alg.getShortestPaths(3));
    //System.out.println(alg.getShortestPaths(4));
    //alg.setSource(4);
    //alg.calc(alg.MULTI);
    //System.out.println(alg.getShortestPaths(1));
    //System.out.println(alg.getShortestPaths(2));
    //System.out.println(alg.getShortestPaths(3));
    //System.out.println(alg.getShortestPaths(4));
  } */
}
