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

public class SortedList{
  private El first;
  private El last;
  private int size;

  public SortedList(){
    init();
  }

  private void init(){
    first = last = null;
    size = 0;
  }

  public int size(){
    return this.size;
  }

  public void addAll(Collection c){
    for(Iterator it = c.iterator(); it.hasNext(); ){
      this.add(it.next());
    }
  }

  public void add(Object o){
    if(size == 0){
      first = last = new El(o);
    }
    else if(size == 1){
      if(first.compareTo(o) <= 0){
        first.next = new El(o);
        last = first.next;
      }
      else{
        first = new El(o, first);
        last = first.next;
        last.next = null;
      }
    }
    else{
      El iterator = first;
      El inserter = null;
      while(iterator != null && iterator.compareTo(o) <= 0){
        inserter = iterator;
        iterator = iterator.next;
      }
      if(inserter == null){ // before first element
        first = new El(o, first);
      }
      else if(inserter == last){ // after last element
        last.next = new El(o);
        last = last.next;
      }
      else{ // elsewhere
        inserter.next = new El(o, inserter.next);
      }
    }
    size++;
  }

  public boolean remove(Object o){
    if(size == 0)
      return false;
    El iterator = first;
    El remover = null;
    while(iterator != null && !iterator.value.equals(o)){
      remover = iterator;
      iterator = iterator.next;
    }
    if(iterator == null){
      //System.out.println(last);
      //System.out.println("remove nothing");
      return false;
    }
    if(size == 1){
      if(first.value.equals(o)){
        first = last = null;
        size = 0;
        return true;
      }
      return false;
    }
    else if(size == 2){
      if(remover == null){ // remove first
        first = first.next;
      }
      else{ // remove last
        first.next = null;
        last = first;
      }
      size--;
      return true;
    }
    else{
      //System.out.println("remover "+remover.value.toString());
      if(remover == null){ // remove first
        first = first.next;
      }
      else if(iterator == last){ // remove last
        remover.next = null;
        last = remover;
      }
      else{
        remover.next = remover.next.next;
      }
      size--;
      return true;
    }
  }
  
  public boolean contains(Object o) {
	  if(isEmpty() || (o == null))
		  return false;
	  El e = first;
	  while(e != null) {
		  if(e.compareTo(new El(o)) == 0)
			  return true;
		  e = e.next;
	  }
	  return false;
  }
  
  public String toString(){
    String result = new String();
    for(Iterator it = this.iterator(); it.hasNext();){
      result += it.next().toString()+"\n";
    }
    return result;
  }

  public Object first(){
    return this.first.value;
  }

  public boolean isEmpty(){
    return (first == null && last == null && size == 0);
  }

  private class El implements Comparable{
    public El next;
    public Comparable value;

    public El(Object o){
      this.value = (Comparable)o;
    }

    public El(Object o, El next){
      this.value = (Comparable)o;
      this.next = next;
    }

    public int compareTo(Object o){
      if(o instanceof El)
        return this.value.compareTo(((El)o).value);
      else
        return this.value.compareTo(o);
    }
  }

  public Iterator iterator(){
    return new SortedListIterator();
  }

  private class SortedListIterator implements Iterator{
    private El it = SortedList.this.first;
    public boolean hasNext(){
      return (it != null);
    }

    public Object next(){
      Object result = this.it.value;
      this.it = this.it.next;
      return result;
    }

    public void remove(){}
  }

  public static void main(String[] argv){
    SortedList list = new SortedList();
    System.out.println("list\n"+list.toString());
    list.add(new Path(1, 2, 1));
    System.out.println("list\n"+list.toString());
    list.add(new Path(2, 1, 2));
    System.out.println("list\n"+list.toString());
    list.add(new Path(1, 3, 3));
    System.out.println("list\n"+list.toString());
    list.add(new Path(3, 1, 1));
    System.out.println("list\n"+list.toString()+"\n\n");
    list.remove(new Path(1, 2, 1));
    System.out.println("list\n"+list.toString());
    list.remove(new Path(2, 1, 2));
    System.out.println("list\n"+list.toString());
    list.remove(new Path(1, 3, 3));
    System.out.println("list\n"+list.toString());
    list.remove(new Path(3, 1, 1));
    System.out.println("list\n"+list.toString());
  }
}

