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
import javax.swing.table.*;

public class RouterEditPaneRIP extends EditPane{
  private Router route;
  private JTable table;
  private static final String[] columns = {"Destination", "Connection", "Cost", "Update-Timer", "GC-Timer"};
  private String[][] data = new String[0][0];
  private JTextField dest = new JTextField(7);
  private JTextField cost = new JTextField(7);
  private JTextField gate = new JTextField(7);
  private JTextField time = new JTextField(7);
  private JTextField gc = new JTextField(7);
  private JTextField addr = new JTextField(7);
  private JTextField start = new JTextField(7);
  private JTextField cycle = new JTextField(7);

  public RouterEditPaneRIP(Router route){
    super();
    this.route = route;
    this.title = "Router: "+this.route.getAddress();
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.init();
  }

  public void init(){
    this.table = new JTable(new Model());
    table.setPreferredScrollableViewportSize(new Dimension(400, 70));
    JScrollPane scrollPane = new JScrollPane(table);
    this.setValues();
    JButton addentry = new JButton("add/edit entry");
    JButton delete = new JButton("delete");
    JButton clear = new JButton("clear");
    this.table.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent me){
        int i;
        if((i = RouterEditPaneRIP.this.table.getSelectedRow()) != -1){
          dest.setText(RouterEditPaneRIP.this.table.getValueAt(i, 0).toString());
          gate.setText(RouterEditPaneRIP.this.table.getValueAt(i, 1).toString());
          cost.setText(RouterEditPaneRIP.this.table.getValueAt(i, 2).toString());
          time.setText(RouterEditPaneRIP.this.table.getValueAt(i, 3).toString());
          gc.setText(RouterEditPaneRIP.this.table.getValueAt(i, DistanceVectorTable.GC).toString());
        }
      }
    });
    addentry.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        if(RouterEditPaneRIP.this.validateInput()){
          //System.out.println("VALID");
          RouterEditPaneRIP.this.add();
        }
      }
    });
    delete.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
          RouterEditPaneRIP.this.remove();
      }
    });
    clear.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
          RouterEditPaneRIP.this.clear();
      }
    });

    JPanel pane = new JPanel();
    pane.add(new JLabel("address ", JLabel.RIGHT));
    pane.add(addr);
    //pane.setPreferredSize(new Dimension(400, 25));
    //pane.setMaximumSize(new Dimension(400, 25));
    //pane.setMinimumSize(new Dimension(400,25));
    this.add(pane);
    pane = new JPanel();
    pane.add(new JLabel("start ", JLabel.RIGHT));
    pane.add(start);
    this.add(pane);
    pane = new JPanel();
    pane.add(new JLabel("cycle ", JLabel.RIGHT));
    pane.add(cycle);
    this.add(pane);

    this.add(scrollPane);
    pane = new JPanel();
    pane.add(dest);
    pane.add(gate);
    pane.add(cost);
    pane.add(time);
    pane.add(gc);
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
    cycle.setText(""+this.route.getRoutingCycle());
  }

  public boolean save(){
    if(!this.validateAddressAndRoutingStart())
      return false;
    this.route.setAddress(new Integer(addr.getText()).intValue());
    this.route.setRoutingStart(new Integer(start.getText()).intValue());
    this.route.setCycle(new Integer(cycle.getText()).intValue());
    return true;
  }

  public void remove(){
    int i;
    if(this.route.rtable != null && (i = this.table.getSelectedRow()) != -1){
    	if(this.route.rtable instanceof DistanceVectorTable) {
    		((LinkStateTable)(this.route.rtable)).remove(i);
    		this.table.clearSelection();
    	}
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
      if(((new Integer(start.getText()).intValue()) < 0) ||
      ((new Integer(cycle.getText()).intValue()) < 0))
    	  return false;
    } catch(NumberFormatException nfe){
      return false;
    }
    return true;
  }

  public void add(){
    int i;
    if((i = this.table.getSelectedRow()) != -1){
    	if(this.route.rtable instanceof DistanceVectorTable) {
    		((LinkStateTable)(this.route.rtable)).remove(i);
    	    this.table.clearSelection();
    	}
      //this.setValues();
      //System.out.println("REMOVED");
    }
    int[] val = new int[DistanceVectorTable.COLS];
    val[DistanceVectorTable.DEST] = new Integer(dest.getText()).intValue();
    val[DistanceVectorTable.COST] = new Integer(cost.getText()).intValue();
    val[DistanceVectorTable.GATE] = new Integer(gate.getText()).intValue();
    val[DistanceVectorTable.TIME] = new Integer(time.getText()).intValue();
    val[DistanceVectorTable.GC] = new Integer(gc.getText()).intValue();
    if(this.route.rtable != null){
      this.route.rtable.add(val);
      //System.out.println("ADDED");
    }
    this.dest.setText("");
    this.cost.setText("");
    this.gate.setText("");
    this.time.setText("");
    this.gc.setText("");
    this.setValues();
  }

  private boolean validateInput(){
    if(time.getText().equals(""))
      time.setText("180");
    if(gc.getText().equals(""))
      gc.setText("-1");
    if(dest.getText().equals("") || cost.getText().equals("") || gate.getText().equals(""))
      return false;
    try{
      new Integer(dest.getText());
      new Integer(cost.getText());
      new Integer(gate.getText());
      new Integer(time.getText());
      new Integer(gc.getText());
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
