package irvtool;

import java.awt.*;

public class Package implements java.io.Serializable{
	
  public static final int T_IP = 0;
  public static final int T_RIP = 1;
  public static final int T_OSPF_FLOODING = 2;
  public static final int T_OSPF_HELLO = 4;
  public static final int T_OSPF_EXCHANGE = 8;
  public static final int M_UNUSED = 0; // Constants for use with Multiple Metrics
  public static final int M_DELAY = 0;
  public static final int M_THROUGHPUT = 1;
  public static final int M_RELIABILITY = 2;
  public static final int M_COST = 3;
  public static final int A_UNUSED = 0;
  private int source;
  private int destination;
  private int type;
  private String text;
  private int ttl = 15;
  private int metric; // containing information about which metric should be used for transport
  private int area; // information about this packet's area

  public Package(int from, int to, int type, int metric, String text){
    this.source = from;
    this.destination = to;
    this.type = type;
    this.metric = metric;
    this.text = text;
  }

  public Package(int from, int to, int type, String text){
    this(from, to, type, M_UNUSED, text);
  }

  public void setTTL(int t){
    this.ttl = t;
  }

  public int getTTL(){
    return this.ttl;
  }

  public void decTTL(){
    this.ttl--;
  }

  public void decTTL(int dec){
    this.ttl -= dec;
  }

  public void setDestination(int to){
    this.destination = to;
  }

  public int getMetric() {
  	return metric;
  }

  public void setMetric(int metric) {
  	this.metric = metric;
  }
  
  public int getArea() {
	  return area;
  }

  public int from(){ return this.source;}
  public int to(){return this.destination;}
  public int type(){return this.type;}
  public String message(){return this.text;}
  public String toString(){return this.message();}
  public Image getImage(){
    return null;
  }

  public Object clone(){
    return new Package(this.source, this.destination, this.type, this.metric, this.text);
  }

}
