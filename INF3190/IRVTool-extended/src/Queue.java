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

public class Queue implements java.io.Serializable{
  private int size = 0;    // number of Elements in Queue
  private El first = null; // first Element in Queue
  private El last = null;  // last  Element in Queue; after this a new one is added

  private class El implements java.io.Serializable{
    Package value; // Package in Queue
    El next;       // next Element in Queue (Element after this)
    Computer node;

    protected El(Package value, Computer node){
      this(value, node, null);
    }

    protected El(Package value, Computer node, El next){
      this.value = value;
      this.next = next;
      this.node = node;
    }
  }

  public synchronized void add(Package p, Computer from){
    switch(size){
      case 0:  first = last = new El(p, from); break;
      case 1:  first.next = last = new El(p, from); break;
      default: last = last.next = new El(p, from); break;
    }
    ++size;
  }

  public Object[] remove(){
    Object[] result = new Object[2];
    switch(size){
      case 0:  return null;
      case 1:  result[0] = first.value; result[1]= first.node; first = last = null; break;
      default: result[0] = first.value; result[1] = first.node; first = first.next; break;
    }
    --size;
    return result;
  }

  public int size(){return this.size;}

  public void attach(Queue q){
    switch(size){
      case 0:  first = q.first; last = q.last; break;
      default: last.next = q.first; last = q.last; break;
    }
    size += q.size;
    q.clear();
  }

  public void clear(){
    first = last = null;
    size = 0;
  }

  public String toString(){
    StringBuffer result = new StringBuffer();
    El tmp = first;
    if(tmp == null) return null;
    do{
      result.append(tmp.value.toString());
      result.append(' ');
      result.append(tmp.node.toString());
      result.append('\n');
      result.append('-');
      result.append('\n');
      tmp = tmp.next;
    } while(tmp != null);
    return result.toString();
  }

  public boolean isEmpty(){
    return (this.size == 0);
  }

  public static void main(String[] args){

  }
}
