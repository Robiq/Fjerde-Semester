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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.table.*;

public class RouterEditPaneOSPF extends EditPane{
  private Router route;
  private JTable table;
  private static String[] columns = null;
  private String[][] data = new String[0][0];
  private JTextField type = new JTextField(6);
  private JTextField from = new JTextField(6);
  private JTextField dest = new JTextField(6);
  private JTextField m_delay = new JTextField(6);
  private JTextField m_throughput = new JTextField(6);
  private JTextField m_reliability = new JTextField(6);
  private JTextField m_cost = new JTextField(6);
  private JTextField gate = new JTextField(6);
  private JTextField age = new JTextField(6);
  private JTextField loll = new JTextField(6);
  private JTextField addr = new JTextField(7);
  private JTextField start = new JTextField(7);
  private JTextField areas = new JTextField(7);

  public RouterEditPaneOSPF(Router route){
    super();
    this.route = route;
    this.title = "Router: "+this.route.getAddress();
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.init();
  }
  
  public void init(){
	  
	  boolean usingMultipleMetrics = (route.getOspfMode() & Consts.OSPFMULTIPLEMETRICS) != 0;
	  int numberOfColumns = LinkStateTable.COLS;
	  int currentColumn = 0;
	  	  
	  if(usingMultipleMetrics)
		  numberOfColumns += Consts.OSPFNUMBEROFMETRICS - 1;

	  columns = new String[numberOfColumns];
	  
	  columns[currentColumn++] = "Record-Type";
	  
	  columns[currentColumn++] = "Source";
	  columns[currentColumn++] = "Destination";
	  columns[currentColumn++] = "Connection";
	  
	  if(usingMultipleMetrics) {
		  columns[currentColumn++] = "Delay";
		  columns[currentColumn++] = "Throughput";
		  columns[currentColumn++] = "Reliability";
	  }
	  
	  columns[currentColumn++] = "Cost";
	  columns[currentColumn++] = "Age";
	  columns[currentColumn++] = "Lollipop-No";
	  
    this.table = new JTable(new Model());
    
    int width = 500;

    if(usingMultipleMetrics) //resize accordingly
    	width += 200;
    
    table.setPreferredScrollableViewportSize(new Dimension(width, 140));
    	
    JScrollPane scrollPane = new JScrollPane(table);
    this.setValues();
    JButton addentry = new JButton("add/edit entry");
    JButton delete = new JButton("delete");
    JButton clear = new JButton("clear");
    this.table.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent me){
    	int offset = 0;
        int i;
        if((i = RouterEditPaneOSPF.this.table.getSelectedRow()) != -1){
        	
          type.setText(RouterEditPaneOSPF.this.table.getValueAt(i, LinkStateTable.TYPE).toString());	
          from.setText(RouterEditPaneOSPF.this.table.getValueAt(i, LinkStateTable.FROM).toString());
          dest.setText(RouterEditPaneOSPF.this.table.getValueAt(i, LinkStateTable.DEST).toString());
          gate.setText(RouterEditPaneOSPF.this.table.getValueAt(i, LinkStateTable.GATE).toString());
          m_delay.setText(RouterEditPaneOSPF.this.table.getValueAt(i, LinkStateTable.COST).toString());
          // if using MM-Mode, display all metrics
          if((route.getOspfMode() & Consts.OSPFMULTIPLEMETRICS) != 0) {
        	  m_throughput.setText(RouterEditPaneOSPF.this.table.getValueAt(i, LinkStateTable.COST + Package.M_THROUGHPUT).toString());
              m_reliability.setText(RouterEditPaneOSPF.this.table.getValueAt(i, LinkStateTable.COST + Package.M_RELIABILITY).toString());
              m_delay.setText(RouterEditPaneOSPF.this.table.getValueAt(i, LinkStateTable.COST + Package.M_COST).toString());
              offset = Consts.OSPFNUMBEROFMETRICS - 1;
          }
          
          age.setText(RouterEditPaneOSPF.this.table.getValueAt(i, LinkStateTable.TIME + offset).toString());
          loll.setText(RouterEditPaneOSPF.this.table.getValueAt(i, LinkStateTable.NUMB + offset).toString());
        }
      }
    });
    addentry.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        if(RouterEditPaneOSPF.this.validateInput()){
          //System.out.println("VALID");
          RouterEditPaneOSPF.this.add();
        }
      }
    });
    delete.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
          RouterEditPaneOSPF.this.remove();
      }
    });
    clear.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
          RouterEditPaneOSPF.this.clear();
      }
    });
    JPanel pane = new JPanel();
    pane.add(new JLabel("address ", JLabel.RIGHT));
    pane.add(addr);
    //pane.setPreferredSize(new Dimension(400, 25));
    //pane.setMaximumSize(new Dimension(400, 25));
    //pane.setMinimumSize(new Dimension(400,25));
    this.add(pane);
    if((route.getOspfMode() & Consts.OSPFMULTIPLEAREAS) != 0) {
    	pane = new JPanel();
        pane.add(new JLabel("areas, separated by commas", JLabel.RIGHT));
        pane.add(areas);
        this.add(pane);
        
        String tmp = new String();
        Iterator i = route.getAreas().iterator();
        while(i.hasNext()) {
        	tmp += i.next() + ",";
        }
        tmp = tmp.substring(0,tmp.length() - 1);

        areas.setText(tmp);
    }
    pane = new JPanel();
    pane.add(new JLabel("start ", JLabel.RIGHT));
    pane.add(start);
    this.add(pane);
    
    this.add(scrollPane);
    pane = new JPanel();
    
    pane.add(type);
    pane.add(from);
    pane.add(dest);
    pane.add(gate);
    pane.add(m_delay);
    // if using MM-Mode, add three more textfields
    if(usingMultipleMetrics) {
    	pane.add(m_throughput);
    	pane.add(m_reliability);
    	pane.add(m_cost);
    }
    pane.add(age);
    pane.add(loll);
    //pane.setPreferredSize(new Dimension(400, 25));
    //pane.setMaximumSize(new Dimension(400, 25));
    //pane.setMinimumSize(new Dimension(400,25));
    this.add(pane);
    pane = new JPanel();
    pane.add(addentry);
    pane.add(delete);
    pane.add(clear);
    //pane.setPreferredSize(new Dimension(400, 25));
    //pane.setMaximumSize(new Dimension(400, 25));
    //pane.setMinimumSize(new Dimension(400,25));
    this.add(pane);
    addr.setText(""+this.route.getAddress());
    start.setText(""+this.route.getRoutingStart());
  }

  public boolean save(){
    if(!this.validateAddressAndRoutingStart())
      return false;
    this.route.setAddress(new Integer(addr.getText()).intValue());
    this.route.setRoutingStart(new Integer(start.getText()).intValue());
    if((route.getOspfMode() & Consts.OSPFMULTIPLEAREAS) != 0 ) {
    	String tmp[] = this.areas.getText().split(",");
    	TreeSet set = new TreeSet();
    	for(int i = 0; i < tmp.length; i++) {
    		set.add((new Integer(tmp[i].trim())).intValue());
    	}
    	route.setAreas(set);
    }
    return true;
  }

  public void remove(){
    int i;
    if(this.route.rtable != null && (i = this.table.getSelectedRow()) != -1){
    	int[][][] data = null;
    	if(this.route.rtable instanceof LinkStateTable)
    		data = ((LinkStateTable)(this.route.rtable)).remove(i);
    	else {
    		System.out.println("Warning, rtable is not LinkStateTable");
    		System.exit(1);
    	}
    		
      data[0][LinkStateTable.TIME][0] = Consts.MAXAGE;
      this.route.rtable.add(data[0]); // start flooding
      this.table.clearSelection();
    }
    this.setValues();
  }

  public void clear(){
    if(this.route.rtable == null)
      return;
    this.route.rtable.clear();
    this.setValues();
  }

  private boolean validateAddressAndRoutingStart(){
    try{
      int addr = new Integer(this.addr.getText()).intValue();
      if((this.route.scr.getAddressExists(addr) && (this.route.getAddress() != addr)) || addr < 1)
            return false;
      if((new Integer(start.getText()).intValue()) < 0)
    		  return false;
      if((route.getOspfMode() & Consts.OSPFMULTIPLEAREAS) != 0 ) {
      	String tmp[] = this.areas.getText().split(",");
      	for(int i = 0; i < tmp.length; i++) {
      		new Integer(tmp[i].trim()).intValue();
      	}
      }
    } catch(NumberFormatException nfe){
      return false;
    }
    return true;
  }

  public void add(){
    int i;
    if((i = this.table.getSelectedRow()) != -1){
    	if(this.route.rtable instanceof LinkStateTable) {
    		((LinkStateTable)(this.route.rtable)).remove(i);
    		this.table.clearSelection();
    	}
    }
    int[][] val = new int[LinkStateTable.COLS][Consts.OSPFNUMBEROFMETRICS];
    val[LinkStateTable.TYPE][0] = new Integer(type.getText()).intValue();
    val[LinkStateTable.FROM][0] = new Integer(from.getText()).intValue();
    val[LinkStateTable.DEST][0] = new Integer(dest.getText()).intValue();
    val[LinkStateTable.COST][0] = new Integer(m_delay.getText()).intValue();
    // if using MM-mode, add new entered metrics
    if((route.getOspfMode() & Consts.OSPFMULTIPLEMETRICS) != 0) {
    	val[LinkStateTable.COST][Package.M_THROUGHPUT] = Consts.INVERSEMETRICMAX -(new Integer(m_throughput.getText()).intValue());
        val[LinkStateTable.COST][Package.M_RELIABILITY] = Consts.INVERSEMETRICMAX -(new Integer(m_reliability.getText()).intValue());
        val[LinkStateTable.COST][Package.M_COST] = (new Integer(m_cost.getText()).intValue());
    } else { // if not, just add zeros as metrics
    	val[LinkStateTable.COST][Package.M_THROUGHPUT] = 0;
    	val[LinkStateTable.COST][Package.M_RELIABILITY] = 0;
    	val[LinkStateTable.COST][Package.M_COST] = 0;
    }
    val[LinkStateTable.GATE][0] = new Integer(gate.getText()).intValue();
    val[LinkStateTable.TIME][0] = new Integer(age.getText()).intValue();
    val[LinkStateTable.NUMB][0] = new Integer(loll.getText()).intValue();
    
    if(this.route.rtable != null){
      this.route.rtable.add(val);
      // Flood new Information
      LinkStateTable lst = (LinkStateTable)this.route.rtable;
      OSPFFloodingPackage flood = new OSPFFloodingPackage(this.route.getAddress(), 0, -1, lst.getDescription(false), lst.getLolly());
      lst.sendNeighbors(flood);
    }
    this.type.setText("");
    this.from.setText("");
    this.dest.setText("");
    this.m_delay.setText("");
    this.m_throughput.setText("");
    this.m_reliability.setText("");
    this.m_cost.setText("");
    this.gate.setText("");
    this.age.setText("");
    this.loll.setText("");
    this.setValues();
  }

  private boolean validateInput(){
    if(age.getText().equals(""))
      age.setText("0");
    if(loll.getText().equals(""))
      loll.setText(""+(1-Consts.OSPF_INF));
    if(dest.getText().equals("") || m_delay.getText().equals("") || m_throughput.getText().equals("") || m_reliability.getText().equals("") || m_cost.getText().equals("") ||gate.getText().equals("") || from.getText().equals(""))
      return false;
    try{
      new Integer(type.getText());
      new Integer(from.getText());
      new Integer(dest.getText());
      new Integer(m_delay.getText());
      new Integer(m_throughput.getText());
      new Integer(m_reliability.getText());
      new Integer(m_cost.getText());
      new Integer(gate.getText());
      new Integer(age.getText());
      new Integer(loll.getText());
    } catch(NumberFormatException nfe){
      return false;
    }
    return true;
  }

  private void setValues(){
    if(this.route.rtable != null)
      data = this.route.rtable.getTable();
    else{
      this.data = new String[0][0];
    }
//    System.out.println("Data: \n\n " + data + "\n\n");
//    System.out.println(data.length);
//    System.out.println(data[0].length);
    
    this.table.revalidate();
    this.table.repaint();
  }

class Model extends AbstractTableModel{
	
    public Model(){
    }

    public int getColumnCount(){
      return columns.length;
    }

    public int getRowCount() {
      return data.length;
    }

    public String getColumnName(int col) {
      return columns[col];
    }

    public Object getValueAt(int row, int col) {
      return data[row][col];
    }

    public Class getColumnClass(int c) {
      return getValueAt(0, c).getClass();
    }
  }

}
