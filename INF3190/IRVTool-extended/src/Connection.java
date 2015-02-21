package irvtool;

import java.awt.*;
import java.util.*;
import irvtool.Package;

public class Connection implements Paintable, Comparable{
	
  private Point start;
  private Point end;
  private int addr;
  private boolean marked = true;
  protected transient Screen scr;
  private Computer first;
  private Computer second;
  private Point spaint;
  private Point epaint;
  private Package pack = null;
  private Queue queue = new Queue();
  private Computer from = null;
  private boolean online = true;
  private ArrayList constraints = new ArrayList();
  private Color color = Color.GRAY;
  //private int cost = 1;
  private int cost[] = {1, Consts.INVERSEMETRICMAX-1, Consts.INVERSEMETRICMAX-1, 1}; 
  // added for OSPF Multiple Metrics feature: delay, throughput, reliability and cost
  // for Consts.INVERSEMETRICMAX, see Consts.java

  public Connection(Computer first, Computer second, Screen scr){
    this.addr = Address.next();
    this.start = first.getPosition();
    this.end = second.getPosition();
    this.first = first;
    this.second = second;
    this.scr = scr;
    double r = (double)first.getDiam()/2.0;
    int xdir = (int)(this.xdir()*r);
    int ydir = (int)(this.ydir()*r);
    this.spaint = new Point(start.x + xdir, start.y + ydir);
    this.epaint = new Point(end.x - xdir, end.y - ydir);
  }

  public String toString(){
    String result = "Connection:\n\taddress:\t"+this.addr;
    if((((Router)(this.scr.parent.controller.sim.routers.get(0))).getOspfMode() & Consts.OSPFMULTIPLEMETRICS) != 0) {
    	result += "\n\tmetrics:\tdelay: " + cost[Package.M_DELAY] 
    	          + " throughput: " + cost[Package.M_THROUGHPUT] 
    	          + " reliability: " + cost[Package.M_RELIABILITY] 
    	          + " cost: " + cost[Package.M_COST];
    } else
    	result += "\n\tcost:\t"+this.cost[Package.M_UNUSED];
    return result;
  }
  
  //returns normal cost value when not using Multiple Metrics
  public int getCost() {
	  return cost[Package.M_UNUSED]; 
  }
  
  //sets normal cost value when not using Multiple Metrics
  public void setCost(int new_cost) {
	  cost[Package.M_UNUSED] = new_cost; 
  }

  //returns an int array containing all metrics
  public int[] getMetrics() {
	  return cost;
  }
  
  //sets all metrics
  public void setMetrics(int[] metrics) {
	  cost = metrics;
  }

  public int compareTo(Object o){
    int other = ((Connection)o).addr;
    if(other == this.addr)
      return 0;
    else if(other > this.addr)
      return -1;
    else
      return 1;
  }

  public Queue getQueue(){
    return this.queue;
  }

  public ArrayList getConstraints(){
    return this.constraints;
  }

  public boolean online(){ return this.online;}

  public void setOnline(){
    this.online = true;
  }

  public void calc(long time){
    Point t;
    for(int i = 0; i < this.constraints.size(); ++i){
      t = (Point)this.constraints.get(i);
      if(time >= t.x && time < t.y){
        this.online = false;
        if (first instanceof Router) {
          Router route = (Router)first;
          if (route.rtable instanceof DistanceVectorTable)
            ((DistanceVectorTable)route.rtable).setINF(this.addr);
        }
        if (second instanceof Router) {
          Router route = (Router)second;
          if (route.rtable instanceof DistanceVectorTable)
            ((DistanceVectorTable)route.rtable).setINF(this.addr);
        }
        return;
      }
    }
    this.online = true;
  }

  public void add(int offline, int online){
    Point p = new Point(offline, online);
    if(!this.constraints.contains(p))
      this.constraints.add(p);
  }

  public void setScreen(Screen s){
    this.scr = s;
  }

  public void remove(int offline, int online){
    Point help = new Point(offline, online);
    int index;
    // delete all constraints that start at offline and end at online
    while((index = this.constraints.indexOf(help)) != -1){
      this.constraints.remove(help);
    }
  }

  public int opposite(Computer c){
    if(c == first)
      return second.getAddress();
    return first.getAddress();
  }
  
  // returns the opposite computer itself, rather than the computers address
  public Computer oppositeComputer(Computer c) {
	  if(c == first)
	      return second;
	    return first;  
  }

  public boolean equals(Object o){
    if(!(o instanceof Connection))
      return false;
    Connection c = (Connection) o;
    if((c.start.equals(this.start) && c.end.equals(this.end)) ||
    (c.start.equals(this.end) && c.end.equals(this.start)))
      return true;
    else
      return false;
  }

  public void setAddress(int addr){ this.addr = addr;}

  public int getAddress(){ return this.addr;}

  public void paint(Graphics g){
    shape(g, this.color);
    if(isMarked())
      paintMark(g, this.markc);
  }

  protected void shape(Graphics g, Color c){
    this.start = this.first.getPosition();
    this.end = this.second.getPosition();
    double r = (double)first.getDiam()/2.0;
    int xdir = (int)(this.xdir()*r);
    int ydir = (int)(this.ydir()*r);
    this.spaint = new Point(start.x + xdir, start.y + ydir);
    this.epaint = new Point(end.x - xdir, end.y - ydir);
    g.setColor(c);
    if(online || c.equals(this.scr.getBackground())){
      g.drawLine(spaint.x, spaint.y, epaint.x, epaint.y);
    }
    else {
      /*int len = this.length();
      for(int i = 0; i < len-2-r; i += 10){
        g.drawLine((int)(spaint.x+i*xdir()), (int)(spaint.y+i*ydir()), (int)(spaint.x+(i+1)*xdir()), (int)(spaint.y+(i+1)*ydir()));
      }
      */
      double st_x = (double)spaint.x;
      double st_y = (double)spaint.y;
      double dx = this.epaint.x-this.spaint.x;
      double dy = this.epaint.y-this.spaint.y;
      double len = Math.sqrt(dx*dx+dy*dy);
      int num = (int)(len/4.0);
      double ed_x;
      double ed_y;
      int i = 0;
      while(i < num){
        ed_x = spaint.x+dx*(i+1)/num;
        ed_y = spaint.y+dy*(i+1)/num;
        g.drawLine((int)st_x, (int)st_y, (int)ed_x, (int)ed_y);
        i += 2;
        st_x = spaint.x+dx*i/num;
        st_y = spaint.y+dy*i/num;
      }
    }
    this.drawAddress(g, c);
  }

  private void drawAddress(Graphics g, Color c){
      g.setColor(c);
      g.setFont(new Font("SansSerif", Font.PLAIN, 20));
      int dx = end.x - start.x;
      int dy = end.y - start.y;
      Point mid = new Point();
      mid.x = start.x + dx/2;
      mid.y = start.y + dy/2;
      if((this.scr.extendedOSPFDisplay & Consts.OSPFMULTIPLEMETRICS) != 0) { // if using (and displaying) Multiple Metrics ...
    	  g.drawString(""+this.addr, mid.x-10, mid.y-25); // display address
    	  // display all metrics
    	  String tmp = this.cost[Package.M_DELAY] + "/" +
    	  	(Consts.INVERSEMETRICMAX - this.cost[Package.M_THROUGHPUT]) + "/" +
    	  	(Consts.INVERSEMETRICMAX - this.cost[Package.M_RELIABILITY]) + "/" +
    	  	this.cost[Package.M_COST];
    	  
    	  g.setFont(new Font("SansSerif", Font.PLAIN, 18));
    	  g.drawString(" ["+tmp+"]", mid.x-(tmp.length()*5), mid.y-2);
      } else { // if not using Multiple Metrics, just display address and cost
    	  if(this.scr.parent.ospf())
    		  g.drawString(this.addr+" ["+this.cost[Package.M_UNUSED]+"]", mid.x-10, mid.y-2);
    	  else
    		  g.drawString(this.addr+" ["+((this.cost[Package.M_UNUSED] > (Consts.INF - 1))? "INF" : ""+this.cost[Package.M_UNUSED])+"]", mid.x-10, mid.y-2);
      }
  }

  private double xdir(){
    double xdir = (double)(end.x - start.x);
    xdir /= (double)this.length();
    return xdir;
  }

  private double ydir(){
    double ydir = (double)(end.y - start.y);
    ydir /= (double)this.length();
    return ydir;
  }

  public boolean isMarked(){
    return this.marked;
  }

  public void mark(boolean m){
    this.marked = m;
    this.paintMark(scr.getGraphics(), (m ? this.markc : scr.getBackground()));
    this.scr.paintObjects(scr.getGraphics());
    scr.requestFocus();
  }

  private int length(){
    int dx = start.x - end.x;
    int dy = start.y - end.y;
    return (int)Math.sqrt((dx*dx) + (dy*dy));
  }

  private void paintMark(Graphics g, Color c){
    g.setColor(c);
    int dx = end.x - start.x;
    int dy = end.y - start.y;
    Point mid = new Point();
    mid.x = start.x + dx/2;
    mid.y = start.y + dy/2;
    g.fillRect(mid.x - 3, mid.y - 3, 6, 6);
  }

  public void setPosition(Point p){
    int dx = end.x - start.x;
    int dy = end.y - start.y;
    this.start = p;
    this.end = new Point(p.x + dx, p.y + dy);
  }

  private double getAngle(){
    return Math.acos((double)(Math.abs(end.x - start.x))/((double)this.length()));
  }

  public void delete(){
    Graphics g = this.scr.getGraphics();
    this.mark(false);
    this.scr.remove(this);
    this.shape(g, scr.getBackground());
    this.scr.paintObjects(g);
    this.first.disconnect(this);
    this.second.disconnect(this);
  }

  /**
  p   ... Point to check
  end ... Point where the Connection ends
  dx  ... difference between end.x and p.x
  dy  ... difference between end.y and p.y
  abs ... length of vector(end - p)
  a ... angle between abs and x-axis

    Point p             cos(a) = dx/abs --> dx = cos(a)*abs
                    sin(a) = dy/abs --> dy = sin(a)*abs
    |\
    |  \
    |    \  abs
    |      \
  dy  |        \
    |          \
    |          a \
    |______________\ Point end

        dx
  */
  public boolean contains(Point point){
    Point p = (Point)point.clone();
    Point left = (start.x < end.x) ? start : end;
    Point right = (left.equals(start)) ? end : start;
    Point higher = (start.y < end.y) ? start : end;
    Point lower = (higher.equals(start)) ? end : start;

    double dx = (double)(right.x - left.x);
    double dy = (double)(lower.y - higher.y);

    if(dx < dy){
      double x = p.y - left.y;
      if(left.equals(lower))
        p.x = (int)((double)p.x + (dx*x)/dy);
      else
        p.x = (int)((double)p.x - (dx*x)/dy);
      if(p.y >= (higher.y + 10) && p.y <= (lower.y - 10) && p.x > (left.x - 3) && p.x < (left.x + 3))
        return true;
      else return false;
    }
    else{
      double x = p.x - left.x;
      if(left.equals(lower))
        p.y = (int)((double)p.y + (dy*x)/dx);
      else
        p.y = (int)((double)p.y - (dy*x)/dx);
      if(p.x >= (left.x + 10) && p.x <= (right.x - 10) && p.y > (left.y - 3) && p.y < (left.y + 3))
            return true;
      else return false;
    }
  }

  public boolean makeStep(int step){
    if(step == 0){
      if(this.queue.isEmpty()){
        return true;
      }
      else{
        Object[] o = this.queue.remove();
        this.pack = (Package)o[0];
        this.from = (Computer)o[1];
      }
    }
    
    Computer to;
    // paint
    double r = (double)first.getDiam()/2.0;
    int xdir = (int)(this.xdir()*(r + 7.0));
    int ydir = (int)(this.ydir()*(r + 7.0));
    Point sour;
    Point dest;
    if(from.getAddress() == first.getAddress()){
      sour = new Point(start.x + xdir, start.y + ydir);
      dest = new Point(end.x - xdir, end.y - ydir);
      to = second;
    }
    else {
      dest = new Point(start.x + xdir, start.y + ydir);
      sour = new Point(end.x - xdir, end.y - ydir);
      to = first;
    }
    int dx = dest.x - sour.x;
    int dy = dest.y - sour.y;
    Point curr = (Point)sour.clone();
    Point old = new Point();
    Point last = new Point();

    curr.x = sour.x + (int)((double)Consts.STEPS*(double)dx/(double)Consts.STEPS);
    curr.y = sour.y + (int)((double)Consts.STEPS*(double)dy/(double)Consts.STEPS);


    Graphics g = this.scr.getGraphics();

    g.setColor(this.scr.getBackground());
    g.fillOval(last.x - 7, last.y - 7, 14, 14);

    old.x = sour.x + (int)((double)((step-1)%Consts.STEPS)*(double)dx/(double)Consts.STEPS);
    old.y = sour.y + (int)((double)((step-1)%Consts.STEPS)*(double)dy/(double)Consts.STEPS);
    g.fillOval(old.x - 7, old.y - 7, 14, 14);

    curr.x = sour.x + (int)((double)step*(double)dx/(double)Consts.STEPS);
    curr.y = sour.y + (int)((double)step*(double)dy/(double)Consts.STEPS);
    if(this.pack != null)
      g.drawImage(this.pack.getImage(), curr.x - 6, curr.y - 6, this.scr);
    this.paint(g);

    // end paint
    if(step == Consts.STEPS){ // last step
      //g.setColor(this.scr.getBackground());
      //g.fillOval(curr.x - 6, curr.y - 6, 12, 12);
      if(this.pack != null){
        this.pack.decTTL(); // always decrement by 1
        to.receive(this.pack);
      }
    }
    if(step == Consts.STEPS && this.queue.isEmpty())
      return true;
    return false;
  }

  public void transmit(){
    Object o[];
    while((o = this.queue.remove()) != null){
      Package p = (Package)o[0];
      from = (Computer)o[1];
      p.decTTL();
      //System.out.println(p.message());
      if(second.getAddress() == from.getAddress())
        first.receive(p);
      else
        second.receive(p);
    }
  }
  
  
}
