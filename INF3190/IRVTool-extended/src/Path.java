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

public class Path implements java.io.Serializable, Comparable{
  private int[] nodes;
  private int cost;

  public Path(int from, int to, int cost){
    this.nodes = new int[2];
    this.nodes[0] = from;
    this.nodes[1] = to;
    this.cost = cost;
  }

  public Path(){
  }

  public void addNode(int node, int c){
    int tmp[] = new int[this.nodes.length+1];
    int i;
    for(i = 0; i < this.nodes.length; ++i){
      tmp[i] = nodes[i];
    }
    tmp[i] = node;
    this.nodes = tmp;
    this.cost = this.cost + c;
  }

  public Object clone(){
    Path result = new Path();
    result.nodes = new int[this.nodes.length];
    for(int i = 0; i < this.nodes.length; ++i)
      result.nodes[i] = this.nodes[i];
    result.cost = this.cost;
    return result;
  }

  public int compareTo(Object o){
    if(!(o instanceof Path))
      return 0;
    Path other = (Path)o;
    if(this.cost < other.cost)
      return -1;
    else if(this.cost > other.cost)
      return 1;
    else
      return 0;
  }

  public boolean equals(Object o){
    if(!(o instanceof Path))
      return false;
    Path other = (Path)o;
    if(other.cost == this.cost && this.nodes.length == other.nodes.length){
      for(int i = 0; i < this.nodes.length; ++i){
        if(this.nodes[i] != other.nodes[i])
          return false;
      }
      return true;
    }
    return false;
  }

  public int getCost(){
    return this.cost;
  }

  public int last(){
    if(this.nodes == null)
      return 0;
    return this.nodes[this.nodes.length-1];
  }
  
  public int lastButOne(){
    if(this.nodes == null)
      return 0;
    if(this.nodes.length == 1)
      return this.nodes[0];
    return this.nodes[this.nodes.length-2];
  }

  public int first(){
    return this.nodes[0];
  }

  public int nextHop(){
    return this.nodes[1];
  }

  public String toString(){
    String result = new String();
    result += "[";
    for(int i = 0; i < nodes.length; ++i){
      if(i != nodes.length-1)
        result += nodes[i]+", ";
      else
        result += nodes[i]+"]";
    }
    result += " cost = "+this.cost;
    return result;
  }
  
  public Iterator iterator(){
    return new ReverseIterator();
  }
  
  private class ReverseIterator implements Iterator{
    private int index = Path.this.nodes.length-1;
    public boolean hasNext(){
      return (index != -1);
    }
    
    public Object next(){
      this.index--;
      return new Integer(this.index+1);
    }
    
    public void remove(){
    }
  }
  
 /* public static void main(String[] argv){
    System.out.println("Hallo");
    Path p = new Path(1, 2, 1);
    p.addNode(3, 1);
    p.addNode(4, 1);
    p.addNode(5, 1);
    
    for(Iterator it = p.iterator(); it.hasNext(); )
      System.out.println(((Integer)it.next()).intValue()+"");
  }*/
}
