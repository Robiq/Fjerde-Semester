package irvtool;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.net.*;

public class Host implements Computer{
	
  //private Queue inQueue = new Queue();
  private Point pos;
  private boolean marked = true;
  protected transient Screen scr;
  private Connection con = null;
  private int dest = 0;
  private int addr;
  private int destArea;
  private String message = "hallo";
  private static Image img = new ImageIcon(Computer.class.getResource("icons/host.png")).getImage();
  private long start = 0;
  private int cycle = 10;
  private int metric = 0; // used when in Multiple-Metrics-Mode

  public Host(Point pos, Screen scr){
    this.pos = pos;
    this.scr = scr;
    this.addr = Address.next();
  }

  public void setStart(long when){
    this.start = when;
  }

  public void setScreen(Screen s){
    this.scr = s;
  }

  //public Queue getInQueue(){ return this.inQueue;}

  public void setAddress(int addr){ this.addr = addr;}

  public int getAddress(){ return this.addr;}

  public long getStart(){ return this.start;}

  public void setMessage(String msg){ this.message = msg;}

  public String getMessage(){
    return this.message;
  }

  // sets OSPF-Metric
  public void setOSPFMetric(int m){ this.metric = m;}

  //gets OSPF-Metric
  public int getOSPFMetric(){ return this.metric;}

  public void setCycle(int c){ this.cycle = c;}

  public int getCycle(){ return this.cycle;}
  
  public String toString(){
	  String ospfMM = "";
	  String ospfMA = "";
	  if ((this.scr.extendedOSPFDisplay & Consts.OSPFMULTIPLEMETRICS) != 0) {
		  ospfMM = "\n\tospf_metric:\t";
			
			switch (this.metric) {
				case Package.M_THROUGHPUT:
					ospfMM += "throughput";
					break;
				case Package.M_RELIABILITY:
					ospfMM += "reliability";
					break;
				case Package.M_COST:
					ospfMM += "cost";
					break;
				default:
					ospfMM += "delay";
			}
		} else {
			ospfMM = "";
		}
	  
	  if ((this.scr.extendedOSPFDisplay & Consts.OSPFMULTIPLEAREAS) != 0) {
		  ospfMA = "\n\tdestination area:\t" + destArea;
	  }
	  
    return "Host:\n\taddress:\t"+this.addr+
      "\n\tdestination:\t"+this.dest+
      ospfMA +
      "\n\tmessage:\t"+this.message+
      "\n\tstart:\t"+this.start+
      "\n\tcycle:\t"+this.cycle+
      "\n\tconnection:\t"+((this.con == null)?(0):(this.con.getAddress()))+
      ospfMM;
  }

  public ArrayList getConnections(){
    ArrayList result = new ArrayList(1);
    result.add(con);
    return result;
  }

  public int getCon(){
    if(con != null)
      return this.con.getAddress();
    else
      return -1;
  }

  public int getDiam(){
    return DIAM;
  }

  public void setDestination(int p){
    this.dest = p;
  }

  public int getDestination(){
    return this.dest;
  }

  public int getDestArea() {
  	return destArea;
  }

  public void setDestArea(int destArea) {
  	this.destArea = destArea;
  }

  public void send(){
    if(this.con != null && this.con.online() && dest != 0){
      this.con.getQueue().add(makePackage(), this);
      //System.out.println("["+this.addr+"] Package sent to "+this.dest);
    }
  }

  public void send(int to, String msg){
    if(this.con != null && this.con.online()){
      this.con.getQueue().add(new IPPackage(this.addr, to, msg), this);
    }
  }

  public void send(int to, String msg, int ttl){
    if(this.con != null && this.con.online()){
      IPPackage p = new IPPackage(this.addr, to, Package.M_UNUSED, msg);
      p.setTTL(ttl);
      this.con.getQueue().add(p, this);
    }
  }

  public void receive(Package p){
    //p.decTTL();
	
	
    if(p.message().equals("PING"))
      this.send(p.from(), "PONG");
    else if(p.message().equals("PONG"))
      this.scr.parent.term.write(this.addr+": one package received as answer to PING");
    else if(p.getTTL() == 0 && p.message().equals("TRACE"))
      this.send(p.from(), "TRACEBACK");
    else if(p.message().equals("TRACEBACK")){
      if(this.scr.parent.term.a == p.from())
        this.scr.parent.term.write(""+p.from());
      else
        this.scr.parent.term.write("  "+p.from());
    }
    else if(p.message().equals("RIP-Package"))
        ;
    else if(p.message().equals("OSPF_HELLO-Package"))
        ;
    else if(p.message().equals("OSPF_FLOODING-Package"))
        ;
    else if(p.message().equals("OSPF_EXCHANGE-Package"))
        ;
    else
      this.scr.parent.term.write(this.addr+": received message \""+p.message()+"\" from "+p.from());
  }

  private Package makePackage(){
    return new IPPackage(this.addr, this.dest, this.metric, this.message);
  }

  public boolean connect(Connection c){
    if(this.con != null)
      return false;
    this.con = c;
    return true;
  }

  public void disconnect(Connection c){
    if(this.con == c)
      this.con = null;
  }

  public Point getPosition(){
    return pos;
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
      g.fillOval(pos.x - DIAM/2 - 3, pos.y - DIAM/2 - 3, DIAM + 6, DIAM + 6);
    } else{
      g.drawImage(this.img, pos.x - DIAM/2, pos.y - DIAM/2, this.scr);
      this.drawAddress(g, Color.GRAY);
    }
    /*g.setColor(c2);
    g.drawOval(pos.x - DIAM/2, pos.y - DIAM/2, DIAM, DIAM);
    g.drawLine(pos.x - DIAM/4, pos.y, pos.x + DIAM/4, pos.y);
    g.drawLine(pos.x - DIAM/4, pos.y - DIAM/4, pos.x - DIAM/4, pos.y + DIAM/4);
    g.drawLine(pos.x + DIAM/4, pos.y - DIAM/4, pos.x + DIAM/4, pos.y + DIAM/4);*/
  }

  private void drawAddress(Graphics g, Color c){
	String tmp = "";
    g.setColor(c);
    g.setFont(new Font("SansSerif", Font.PLAIN, 20));
    
    if((this.scr.extendedOSPFDisplay&Consts.OSPFMULTIPLEAREAS) != 0) {
    	if(this.con != null) {
	    	Router r = (Router)(this.con.oppositeComputer(this));
	    	tmp += r.getAreas().first() + ".";
    	}
    }
    
    tmp += this.addr;
    
    if((this.scr.extendedOSPFDisplay & Consts.OSPFMULTIPLEMETRICS) != 0) { // if using (and displaying) multiple Metrics
    	// display Metric (D for delay, T for throughput, etc...)
    	switch(metric) {
    	case Package.M_THROUGHPUT: tmp += "T";
    			break;
    	case Package.M_RELIABILITY: tmp += "R";
				break;
    	case Package.M_COST: tmp += "C";
				break;
		default: tmp += "D";
    	}
    	g.drawString(tmp, pos.x-12, pos.y-DIAM/2-2);
    }
    else
    	g.drawString(tmp, pos.x-10, pos.y-DIAM/2-2);	
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

  public void delete(){
    this.mark(false);
    this.scr.remove(this);
    if(this.con != null)
      this.con.delete();
    this.con = null;
    this.shape(scr.getGraphics(), true);
  }

  public void setPosition(Point p){
    this.pos = p;
  }

  public boolean contains(Point p){
    if(p.x > (pos.x - DIAM/2) && p.x < (pos.x + DIAM/2) && p.y > (pos.y - DIAM/2) && p.y < (pos.y + DIAM/2))
      return true;
    else return false;
  }

}
