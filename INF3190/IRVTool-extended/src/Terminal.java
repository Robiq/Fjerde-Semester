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
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Terminal extends JPanel implements KeyListener, Runnable {
  private JTextField cmdline = new JTextField();
  private JTextArea  output = new JTextArea();
  private JScrollBar bar;
  private static String prompt = "[none]$ ";
  private int node = 0;
  private Simulator sim;
  private boolean tracing = false;
  private static int ttl;
  protected static int a;
  private String[] runString;
  private int nextLine;
  private TerminalNavigationFilter nav = new TerminalNavigationFilter(Terminal.prompt);
  private TerminalHistory hist = new TerminalHistory(Terminal.prompt);

  public Terminal(){
    super(false);
    JPanel dummy = new JPanel();
    this.setLayout(new BorderLayout());
    dummy.setLayout(new GridLayout(1,1));
    cmdline.setNavigationFilter(nav);
    cmdline.getCaret().setVisible(false);
    dummy.add(cmdline);
    this.add(dummy, BorderLayout.NORTH);
    JScrollPane sp = new JScrollPane(output);
    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    bar = sp.getVerticalScrollBar();
    
    this.add(sp, BorderLayout.CENTER);
    output.setBackground(Color.BLACK);
    cmdline.setBackground(Color.BLACK);
    output.setForeground(Color.WHITE);
    cmdline.setForeground(Color.WHITE);
    output.setLineWrap(true);
    output.setWrapStyleWord(true);
    output.getCaret().setVisible(false);
    cmdline.setCaretColor(Color.WHITE);
    cmdline.getCaret().setBlinkRate(500);
    output.setEditable(false);
    cmdline.addKeyListener(this);
    cmdline.setText(this.prompt);
    cmdline.getCaret().setDot(this.prompt.length()+1);
    this.setPreferredSize(new Dimension(this.getPreferredSize().width, this.getPreferredSize().height*5));
  }
  
 

  public void setSimulator(Simulator sim){
    this.sim = sim;
  }

  public void requestFocus(){
    this.cmdline.requestFocus();
    this.cmdline.getCaret().setVisible(true);
  }

  public void write(String s){
    if(s.startsWith("  "))
      this.trace(0);
    else{
      this.cmdline.setEditable(true);
      tracing = false;
      this.cmdline.requestFocus();
    }    
    output(s+"\n");
  }

  public void keyTyped(KeyEvent ke){
    if(ke.getKeyChar() == '\n'){
      String cmd = this.cmdline.getText();
      this.cmdline.setText(((this.node == 0) ? (this.prompt = "[none]$ ") : (this.prompt = "["+this.node+"]$ ")));
      this.nav.setPrompt(this.prompt);
      this.hist.setPrompt(this.prompt);
      if(!cmd.equals(this.prompt)){
        //System.out.println(cmd);
        this.hist.push(cmd);
      }
      if(!cmd.equals(""))
        this.compute(cmd);
    }
    else if(ke.getKeyChar() == '\b'){
      if(cmdline.getCaret().getDot() < this.prompt.length()+1)
        this.cmdline.setText(this.prompt+" ");
    }
  }
  
  public void keyPressed(KeyEvent ke){
    if(ke.getKeyCode() == KeyEvent.VK_UP){
      this.cmdline.setText(this.hist.moveBackward());
    }
    else if(ke.getKeyCode() == KeyEvent.VK_DOWN){
      this.cmdline.setText(this.hist.moveForward());
    }
  }

  public void enable(){
    this.cmdline.setEditable(true);
    output("unlocked ...\n");
    this.cmdline.requestFocus();
  }

  public void disable(){
    this.cmdline.setEditable(false);
    this.cmdline.getCaret().setVisible(false);
    output("locked ...\n");
  }

  public boolean isEnabled(){
    return this.cmdline.isEnabled();
  }

  private void compute(String cmd){
    StringTokenizer tok = new StringTokenizer(cmd, " \t", false);

    if(tok.nextToken().equals(""))
      return;
    String[] argv = new String[tok.countTokens()];
    if(tok.countTokens() == 0)
      return;
    for(int i = 0; i < argv.length; ++i)
      argv[i] = tok.nextToken();
    if(argv[0].equals("clear"))
      this.output.setText("");
    else if(argv[0].equals("login"))
      this.login(argv);
    else if(argv[0].equals("logout"))
      this.logout();
    else if(argv[0].equals("ls"))
      this.ls(argv);
    else if(argv[0].equals("ping"))
      this.ping(argv);
    else if(argv[0].equals("route"))
      this.route(argv);
    else if(argv[0].equals("send"))
      this.send(argv);
    else if(argv[0].equals("traceroute"))
      this.traceroute(argv);
    else if(argv[0].equals("exit"))
      this.sim.controller.frame.quit();
    else if(argv[0].equals("broadcast"))
      this.broadcast(argv);
    else if(argv[0].equals("set"))
      this.set(argv);
    else if(argv[0].equals("help"))
      this.help();
    else if(argv[0].equals("run")){
      this.runString = argv;
      new Thread(this).start();
    }
    else if(argv[0].equals("echo")){
      for(int i = 1; i < argv.length; ++i)
        output(argv[i]+" ");
      output("\n");
    }
    else if(argv[0].equals("sleep"))
      this.sleep(argv);
    else if(argv[0].equals(""))
      ;
    else
      output(argv[0]+": no such command\n");

    
  }

  private synchronized void sleep(String[] argv){
    if(argv.length == 1 || argv.length > 2){
      output("usage: sleep <seconds>\n");
      return;
    }
    int sec;
    if((sec = isNumber(argv[1])) == -1){
      output(argv[1]+": is not a number ...\n");
      return;
    }
    //this.sim.timeline.setWaiter(this, sec);

    long time = this.sim.timeline.time();
    while(this.sim.timeline.time() < time+sec){
      try{
        Thread.sleep(5);
      } catch(InterruptedException ie){
        output(ie.toString()+'\n');
      }
    }
  }
  
  public void output(String s) {
	  this.output.append(s);
	  
//	  bar.setAutoscrolls(true);
//	  bar.setValue(bar.getMaximum());
	  output.setCaretPosition(output.getDocument().getLength());
  }

  private String replace(String str, String delim, int num){
    int i = str.indexOf(delim);
    if(i == -1)
      return str;
    String result = str.substring(0, i);
    result += (num+str.substring(i+delim.length(), str.length()));
    return result;
  }

  public void run(){
    String[] argv = this.runString;
    if(argv.length == 1 || argv.length > 2){
      output("usage: run <file>\n");
      return;
    }
    try{
      BufferedReader script = new BufferedReader(new FileReader(argv[1]));
      String line = null;
      ArrayList buff = new ArrayList();
      while((line = script.readLine()) != null){
        if(line.startsWith("#"))
          continue;
        buff.add(line);
      }
      script.close();
      this.parse(buff);
      output("end of script\n");
    } catch(IOException ioe){
      output(argv[1]+": error reading file ...\n");
    }
  }

  private synchronized void parse(ArrayList script){
    for(int i = 0; i < script.size(); i = checkRepeat(script, i)){
      ;
    }
  }

  private synchronized int checkRepeat(ArrayList script, int ln){
    if(ln >= script.size())
      return ln+1;
    String line = (String)script.get(ln);
    if(line.indexOf("repeat") != -1){
      StringTokenizer tok = new StringTokenizer(line, " \t");
      tok.nextToken(); // kick "repeat"
      int times = new Integer(tok.nextToken()).intValue();
      //System.out.println("TIMES "+times);
      int endline = 0;
      for(int i = ln+1; i < script.size(); ++i){
        if(((String)script.get(i)).indexOf(";;") != -1){
          endline = i;
          break;
        }
      }

      for(int i = 0; i < times; ++i){
        for(int j = ln+1; j < endline; j = this.checkFor(script, j)){
          ;
        }
      }
      return endline+1;
    }
    else
      return checkFor(script, ln);
  }

  private synchronized int checkFor(ArrayList script, int ln){
    String line = (String)script.get(ln);
    //if(line.indexOf(";") != -1 )
    //  return ln+1;
    if(line.indexOf("for") != -1){
      StringTokenizer tok = new StringTokenizer(line, " \t");
      String[] tmp = new String[tok.countTokens()];
      for(int i = 0; i < tmp.length; ++i){
        tmp[i] = tok.nextToken();
      } // FOR
      int endline = ln+1;

      for(int i = ln+1; i < script.size(); ++i){
        if(((String)script.get(i)).indexOf(";") != -1){
          endline = i;
          break;
        }
      } // FOR
      if(tmp[1].equals("all")){
        if(tmp[2].equals("nodes")){
          for(int i = 0; i < this.sim.nodes.size(); ++i){
            Computer comp = (Computer)this.sim.nodes.get(i);
            String[] cmd = {"login", ""+comp.getAddress()};
            this.login(cmd);
            for(int j = ln+1; j < endline; ++j){
              String t = (String)script.get(j);
              //System.out.println("SOWAS "+t);
              t = this.prompt+t;
              t = this.replace(t, "this", comp.getAddress());
              //System.out.println(t);
              this.compute(t);
            } // FOR
            this.logout();
          } // FOR
          return endline+1;
        } // NODES
        else if(tmp[2].equals("hosts")){
          for(int i = 0; i < this.sim.hosts.size(); ++i){
            Host host = (Host)this.sim.hosts.get(i);
            String[] cmd = {"login", ""+host.getAddress()};
            this.login(cmd);
            for(int j = ln+1; j < endline; ++j){
              String t = (String)script.get(j);
              t = this.prompt+t;
              t = this.replace(t, "this", host.getAddress());
              //System.out.println(t);
              this.compute(t);
            } // FOR
            this.logout();
          } // FOR
          return endline+1;
        } // HOSTS
        else if(tmp[2].equals("routers")){
          for(int i = 0; i < this.sim.routers.size(); ++i){
            Router r = (Router)this.sim.routers.get(i);
            String[] cmd = {"login", ""+r.getAddress()};
            this.login(cmd);
            for(int j = ln+1; j < endline; ++j){
              String t = (String)script.get(j);
              t = this.prompt+t;
              t = this.replace(t, "this", r.getAddress());
              //System.out.println(t);
              this.compute(t);
            } // FOR
            this.logout();
          } // FOR
          return endline+1;
        } // ROUTERS
        else if(tmp[2].equals("connections")){
          for(int i = 0; i < this.sim.connections.size(); ++i){
            Connection con = (Connection)this.sim.connections.get(i);
            //String[] cmd = {"login", ""+con.getAddress()};
            //this.login(cmd);
            for(int j = ln+1; j < endline; ++j){
              String t = (String)script.get(j);
              t = this.prompt+t;
              t = this.replace(t, "this", con.getAddress());
              //System.out.println(t);
              this.compute(t);
            } // FOR
            //this.logout();
          } // fOR
          return endline+1;
        } // CONNECTIONS
        else{
          output("error in script ...\n");
          return script.size();
        }
      } // ALL
      else if(tmp[1].equals("nodes")){
        for(int i = 2; i < tmp.length; ++i){
          int num;
          if((num = isNumber(tmp[i])) == -1){
            output(tmp[i]+": not a number\n");
            continue;
          } // IF
          if(!this.sim.isNode(num)){
            output(num+": no such node\n");
            continue;
          } // IF
          Computer comp = this.sim.getNode(num);
          String[] cmd = {"login", ""+comp.getAddress()};
          this.login(cmd);
          for(int j = ln+1; j < endline; ++j){
            String t = (String)script.get(j);
            t = this.prompt+t;
            t = this.replace(t, "this", comp.getAddress());
            //System.out.println(t);
            this.compute(t);
          } // FOR
          this.logout();
        } // FOR
        return endline+1;
      } // NODES
      else if(tmp[1].equals("hosts")){
        for(int i = 2; i < tmp.length; ++i){
          int num;
          if((num = isNumber(tmp[i])) == -1){
            output(tmp[i]+": not a number\n");
            continue;
          } // IF
          if(!this.sim.isHost(num)){
            output(num+": no such host\n");
            continue;
          } // IF
          Host host = (Host)this.sim.getNode(num);
          String[] cmd = {"login", ""+host.getAddress()};
          this.login(cmd);
          for(int j = ln+1; j < endline; ++j){
            String t = (String)script.get(j);
            t = this.prompt+t;
            t = this.replace(t, "this", host.getAddress());
            //System.out.println(t);
            this.compute(t);
          } // FOR
          this.logout();
        } // FOR
        return endline+1;
      } // HOSTS
      else if(tmp[1].equals("routers")){
        for(int i = 2; i < tmp.length; ++i){
          int num;
          if((num = isNumber(tmp[i])) == -1){
            output(tmp[i]+": not a number\n");
            continue;
          } // iF
          if(!this.sim.isRouter(num)){
            output(num+": no such router\n");
            continue;
          } // IF
          Router r = (Router)this.sim.getNode(num);
          String[] cmd = {"login", ""+r.getAddress()};
          this.login(cmd);
          for(int j = ln+1; j < endline; ++j){
            String t = (String)script.get(j);
            t = this.prompt+t;
            t = this.replace(t, "this", r.getAddress());
            //System.out.println(t);
            this.compute(t);
          } // FOR
          this.logout();
        } // FOR
        return endline+1;
      } // ROUTERS
      else if(tmp[1].equals("connections")){
        for(int i = 2; i < tmp.length; ++i){
          int num;
          if((num = isNumber(tmp[i])) == -1){
            output(tmp[i]+": not a number\n");
            continue;
          } // IF
          if(!this.sim.isConnection(num)){
            output(num+": no such connection\n");
            continue;
          } // IF
          Connection con = this.sim.getConnection(num);
          //String[] cmd = {"login", ""+con.getAddress()};
          //this.login(cmd);
          for(int j = ln+1; j < endline; ++j){
            String t = (String)script.get(j);
            t = this.prompt+t;
            t = this.replace(t, "this", con.getAddress());
            //System.out.println(t);
            this.compute(t);
          } // FOR
          //this.logout();
        } // FOR
        return endline+1;
      } // CONNECTIONS
      else{
        output("error in script ...\n");
        return script.size();
      }
    } // FOR
    else{
      this.compute(this.prompt+line);
      return ln+1;
    }
  }

  protected synchronized void setNextLine(int line){
    this.nextLine = line;
  }

  protected synchronized int getNextLine(){
    return this.nextLine;
  }

  /*private synchronized void doLine(ArrayList script, int ln){
    if(ln >= script.size())
      return;
    String line = (String)script.get(ln);
    if(line.startsWith("repeat")){
      StringTokenizer tok = new StringTokenizer(line, " \t");
      tok.nextToken();
      int times = new Integer(tok.nextToken()).intValue();
      int endline = 0;
      for(int i = ln+1; i < script.size(); ++i){
        if(((String)script.get(i)).indexOf(";;") != -1){
          endline = i;
          break;
        }
      }

      for(int i = 0; i < times; ++i){
        for(int j = ln+1; j < endline; ++j){
          this.doLine(script, j);
        }
      }
      this.doLine(script, endline+1);
      return;
    }
    else if(line.startsWith("for")){
      StringTokenizer tok = new StringTokenizer(line, " \t");
      String[] tmp = new String[tok.countTokens()];
      for(int i = 0; i < tmp.length; ++i)
        tmp[i] = tok.nextToken();
      }
      int endline;

      for(int i = ln+1; i < script.size(); ++i){
        if(((String)script.get(i)).indexOf(";;") != -1){
          endline = i;
          break;
        }
      }

      if(tmp[1].equals("all")){

        if(tmp[2].equals("nodes")){
          for(int i = 0; i < this.sim.nodes.size(); ++i){
            Computer comp = (Computer)this.sim.nodes.get(i);
            String[] cmd = {"login", ""+comp.getAddress()};
            this.login(cmd);
            for(int j = ln+1; j < endline; ++j){
              String t = (String)script.get(j);
              t = this.prompt+t;
              t = this.replace(t, "this", comp.getAddress());
              System.out.println(t);
              this.compute(t);
            }
            this.logout();
          }
        }
        else if(tmp[2].equals("hosts")){
          for(int i = 0; i < this.sim.hosts.size(); ++i){
            Host host = (Host)this.sim.hosts.get(i);
            String[] cmd = {"login", ""+host.getAddress()};
            this.login(cmd);
            for(int j = 0; j < lines.size(); ++j){
              String t = (String)lines.get(j);
              t = this.prompt+t;
              t = this.replace(t, "this", host.getAddress());
              System.out.println(t);
              this.compute(t);
            }
            this.logout();
          }
        }
        else if(tmp[2].equals("routers")){
          for(int i = 0; i < this.sim.routers.size(); ++i){
            Router r = (Router)this.sim.routers.get(i);
            String[] cmd = {"login", ""+r.getAddress()};
            this.login(cmd);
            for(int j = 0; j < lines.size(); ++j){
              String t = (String)lines.get(j);
              t = this.prompt+t;
              t = this.replace(t, "this", r.getAddress());
              System.out.println(t);
              this.compute(t);
            }
            this.logout();
          }
        }
        else if(tmp[2].equals("connections")){
          for(int i = 0; i < this.sim.connections.size(); ++i){
            Connection con = (Connection)this.sim.connections.get(i);
            //String[] cmd = {"login", ""+con.getAddress()};
            //this.login(cmd);
            for(int j = 0; j < lines.size(); ++j){
              String t = (String)lines.get(j);
              t = this.prompt+t;
              t = this.replace(t, "this", con.getAddress());
              System.out.println(t);
              this.compute(t);
            }
            //this.logout();
          }
        }
        else{
          output("error in script ...\n");
          script.close();
          return;
        }
      }
      else if(tmp[1].equals("nodes")){
        for(int i = 2; i < tmp.length; ++i){
          int num;
          if((num = isNumber(tmp[i])) == -1){
            output(tmp[i]+": not a number\n");
            continue;
          }
          if(!this.sim.isNode(num)){
            output(num+": no such node\n");
            continue;
          }
          Computer comp = this.sim.getNode(num);
          String[] cmd = {"login", ""+comp.getAddress()};
          this.login(cmd);
          for(int j = 0; j < lines.size(); ++j){
            String t = (String)lines.get(j);
            t = this.prompt+t;
            t = this.replace(t, "this", comp.getAddress());
            System.out.println(t);
            this.compute(t);
          }
          this.logout();
        }
      }
      else if(tmp[1].equals("hosts")){
        for(int i = 2; i < tmp.length; ++i){
          int num;
          if((num = isNumber(tmp[i])) == -1){
            output(tmp[i]+": not a number\n");
            continue;
          }
          if(!this.sim.isHost(num)){
            output(num+": no such host\n");
            continue;
          }
          Host host = (Host)this.sim.getNode(num);
          String[] cmd = {"login", ""+host.getAddress()};
          this.login(cmd);
          for(int j = 0; j < lines.size(); ++j){
            String t = (String)lines.get(j);
            t = this.prompt+t;
            t = this.replace(t, "this", host.getAddress());
            System.out.println(t);
            this.compute(t);
          }
          this.logout();
        }
      }
      else if(tmp[1].equals("routers")){
        for(int i = 2; i < tmp.length; ++i){
          int num;
          if((num = isNumber(tmp[i])) == -1){
            output(tmp[i]+": not a number\n");
            continue;
          }
          if(!this.sim.isRouter(num)){
            output(num+": no such router\n");
            continue;
          }
          Router r = (Router)this.sim.getNode(num);
          String[] cmd = {"login", ""+r.getAddress()};
          this.login(cmd);
          for(int j = 0; j < lines.size(); ++j){
            String t = (String)lines.get(j);
            t = this.prompt+t;
            t = this.replace(t, "this", r.getAddress());
            System.out.println(t);
            this.compute(t);
          }
          this.logout();
        }
      }
      else if(tmp[1].equals("connections")){
        for(int i = 2; i < tmp.length; ++i){
          int num;
          if((num = isNumber(tmp[i])) == -1){
            output(tmp[i]+": not a number\n");
            continue;
          }
          if(!this.sim.isConnection(num)){
            output(num+": no such connection\n");
            continue;
          }
          Connection con = this.sim.getConnection(num);
          //String[] cmd = {"login", ""+con.getAddress()};
          //this.login(cmd);
          for(int j = 0; j < lines.size(); ++j){
            String t = (String)lines.get(j);
            t = this.prompt+t;
            t = this.replace(t, "this", con.getAddress());
            System.out.println(t);
            this.compute(t);
          }
          //this.logout();
        }
      }
      else{
        output("error in script ...\n");
        script.close();
        return;
      }
    }
    else
      this.compute(this.prompt+line);
  }*/

  private synchronized void help(){
    output(
      "broadcast\t\t\t(when logged in at a router)\n"+
      "clear\t\t\t(clears the output field)\n"+
      "exit\t\t\t(closes the application)\n"+
      "login <addr>\n"+
      "logout\n"+
      "ls <addr>\t\t\t(lists properties of <addr>)\n"+
      "ping <addr>\n"+
      "route <addr>\t\t\t(shows the routing table of <addr>)\n"+
      "send <addr> <msg>\t\t(sends the text <msg> to <addr>)\n"+
      "set [<connection>] <prop> [<value>] [<value>]\t(sets the property <prop> to the value <val>)\n"+
      "traceroute <addr>\n\n"
     );
  }

  private static int isNumber(String str){
    int result = -1;
    try{
      result = new Integer(str).intValue();
    } catch(NumberFormatException nfe){
      return -1;
    }
    return result;
  }

  private synchronized void login(String[] argv){
    int addr;
    if((addr = isNumber(argv[1])) != -1){
      if(this.sim.isNode(addr)){
        this.node = addr;
        this.cmdline.setText(((this.node == 0) ? (this.prompt = "[none]$ ") : (this.prompt = "["+this.node+"]$ ")));
        this.nav.setPrompt(this.prompt);
        this.hist.setPrompt(this.prompt);
      }
      else
        output(addr+": no such node ...\n");
    }
  }

  private synchronized void set(String[] argv){
    int num = -1;
    if(argv.length == 0){
      output("usage: set <property> <value>\n");
      return;
    }
    if((num = isNumber(argv[1])) != -1){
      if(this.sim.isConnection(num)){
        int num2 = -1;
        int num3 = -1;
        if(argv.length == 4 && argv[2].equals("address") && (num2 = isNumber(argv[3])) != -1){
          if(this.sim.controller.screen.getAddressExists(num2) && this.sim.getConnection(num).getAddress() != num2)
            output(num2+": address already exists ...\n");
          else{
            this.sim.getConnection(num).setAddress(num2);
          }
        }
        else if(argv.length == 4 && argv[2].equals("cost") && (num2 = isNumber(argv[3])) != -1){
        	if((((Router)(this.sim.routers.get(0))).getOspfMode()&Consts.OSPFMULTIPLEMETRICS) == 0)
        		this.sim.getConnection(num).setCost(num2);
        	else
        		output("unable to set cost: Multiple Metrics used");
        }
        else if(argv.length == (4 + Consts.OSPFNUMBEROFMETRICS - 1) && argv[2].equals("metrics")) {
        	if((((Router)(this.sim.routers.get(0))).getOspfMode()&Consts.OSPFMULTIPLEMETRICS) != 0) {
	        	int[] metrics = new int[Consts.OSPFNUMBEROFMETRICS];
	        	boolean metricsCorrect = true;
	        	
	        	metrics[0] = isNumber(argv[3]);
	        	metrics[1] = Consts.INVERSEMETRICMAX - isNumber(argv[4]);
	        	metrics[2] = Consts.INVERSEMETRICMAX - isNumber(argv[5]);
	        	metrics[3] = isNumber(argv[6]);
	        	
	        	for(int i = 0; i < Consts.OSPFNUMBEROFMETRICS; i++) 
	        		if(metrics[i] < 0) {
	        			output("unable to set metrics: negative or not a number\n");
	        			metricsCorrect = false;
	        			break;
	        		}
	        	if(metricsCorrect)
	        		this.sim.getConnection(num).setMetrics(metrics);
        	} else
        		output("unable to set metrics: Multiple Metrics not used\n");
        }
        else if(argv.length == 5 && argv[2].equals("down") && (num2 = isNumber(argv[3])) != -1 && (num3 = isNumber(argv[4])) != -1){
          this.sim.getConnection(num).add(num2, num3);
        }
        else if(argv.length == 3 && argv[2].equals("clear")){
          this.sim.getConnection(num).getConstraints().clear();
        }
        else if(argv.length == 5 && argv[2].equals("remove") && (num2 = isNumber(argv[3])) != -1 && (num3 = isNumber(argv[4])) != -1){
          this.sim.getConnection(num).remove(num2, num3);
        }
        else
          output("usage: set <connection> <property> <value>\n");
      }
      else
        output(num+": no such connection ...\n");
      return;
    }
    if(this.node != 0){
      if(this.sim.isRouter(this.node)){
	        if((argv.length == 3) && argv[1].equals("start") && (num = isNumber(argv[2])) != -1){
	          ((Router)this.sim.getNode(this.node)).setRoutingStart(num);
	        }
	        else if((argv.length == 3) && argv[1].equals("address") && (num = isNumber(argv[2])) != -1){
	          if(this.sim.controller.screen.getAddressExists(num) && this.sim.getNode(this.node).getAddress() != num)
	            output(num+": address already exists ...\n");
	          else{
	            ((Router)this.sim.getNode(this.node)).setAddress(num);
	            String[] tmp = {"login", ""+num};
	            this.login(tmp);
	          }

        } else if((argv.length > 2) && argv[1].equals("areas")) {
        	if((((Router)(this.sim.routers.get(0))).getOspfMode()&Consts.OSPFMULTIPLEAREAS) != 0) {
        		TreeSet set = new TreeSet();
        		try {
        		for(int i = 2; i < argv.length; i++)
        			set.add((new Integer(argv[i].trim())).intValue());
        		((Router)this.sim.getNode(this.node)).setAreas(set);
        		} catch (NumberFormatException e) {
        			output("usage: set areas <0-9> \t (separated by whitespaces\n");
        		}
        		
        	} else
        		output("unable to set areas: Multiple Areas not used\n");
        }
        else
          output("usage: set <property> <value>\n");
      }
      else if(this.sim.isHost(this.node)){
        if(argv[1].equals("address") && (num = isNumber(argv[2])) != -1){
          if(this.sim.controller.screen.getAddressExists(num) && this.sim.getNode(this.node).getAddress() != num)
            output(num+": address already exists ...\n");
          else{
            ((Host)this.sim.getNode(this.node)).setAddress(num);
            String[] tmp = {"login", ""+num};
            this.login(tmp);
          }
        }
        else if(argv[1].equals("destination") && (num = isNumber(argv[2])) != -1)
          ((Host)this.sim.getNode(this.node)).setDestination(num);
        else if(argv[1].equals("message"))
          ((Host)this.sim.getNode(this.node)).setMessage(argv[2]);
        else if(argv[1].equals("start") && (num = isNumber(argv[2])) != -1)
          ((Host)this.sim.getNode(this.node)).setStart(num);
        else if(argv[1].equals("cycle") && (num = isNumber(argv[2])) != -1)
          ((Host)this.sim.getNode(this.node)).setCycle(num);
        else if(argv[1].equals("metric"))
        	if((((Router)(this.sim.routers.get(0))).getOspfMode()&Consts.OSPFMULTIPLEMETRICS) != 0) {
	        	int metric = -1;
	        	if(argv[2].equals("delay"))
	        		metric = Package.M_DELAY;
	        	else if(argv[2].equals("throughput"))
	        		metric = Package.M_THROUGHPUT;
	        	else if(argv[2].equals("reliability"))
	        		metric = Package.M_RELIABILITY;
	        	else if(argv[2].equals("cost"))
	        		metric = Package.M_COST;
	        	else
	        		output("usage: set metric <delay|throughput|reliability|cost\n");
	        	if(metric != -1) {
	        		((Host)this.sim.getNode(this.node)).setOSPFMetric(metric);
	        		this.sim.controller.screen.repaint();
	        	}
        	} else
        		output("unable to set metric: Multiple Metrics not used");
        else
          output("usage: set <property> <value>\n");
      }
      else
        output(this.node+": no such node ...\n");
    }
    else if(this.node == 0)
      output("ERROR: you have to be logged in\n");
    else
      output("usage: set <property> <value>\n");
  }

  private synchronized void logout(){
    this.node = 0;
    this.cmdline.setText((this.prompt = "[none]$ "));
    this.nav.setPrompt(this.prompt);
    this.hist.setPrompt(this.prompt);
  }

  private synchronized void ls(String[] argv){
    int addr;
    if(argv.length == 2 && (addr = isNumber(argv[1])) != -1){
      if(this.sim.isNode(addr)){
        output(this.sim.getNode(addr).toString()+"\n");
        //this.cmdline.getCaret().setDot(15);
      }
      else if(this.sim.isConnection(addr)){
        output(this.sim.getConnection(addr).toString()+"\n");
      }
      else
        output(addr+": no such node or connection ...\n");
    }
    else
      output("usage: ls <address>\n");
  }

  private synchronized void ping(String[] argv){
    int addr;
    if(this.node != 0 && argv.length == 2 && (addr = isNumber(argv[1])) != -1){
      if(this.sim.isNode(addr)){
        this.sim.getNode(this.node).send(addr, "PING");
      }
      else
        output(addr+": no such node ...\n");
    }
    else if(this.node == 0)
      output("ERROR: you have to be logged in\n");
    else
      output("usage: ping <address>\n");
  }

  private synchronized void broadcast(String[] argv){
    if(this.node != 0 && argv.length == 1){
      if(this.sim.isRouter(this.node)){
        ((Router)this.sim.getNode(this.node)).broadcast();
      }
      else
        output(this.node+": is no router ...\n");
    }
    else if(this.node == 0)
      output("ERROR: you have to be logged in\n");
    else
      output("usage: broadcast\n");
  }

  private synchronized void route(String[] argv){
    int addr;
    if(argv.length == 2 && (addr = isNumber(argv[1])) != -1){
      if(this.sim.isRouter(addr)){
        output(((Router)this.sim.getNode(addr)).rtable.toString());
      }
      else
        output(addr+": no such router ...\n");
    }
    else
      output("usage: route <address>\n");
  }

  private synchronized void send(String[] argv){
    int addr;
    if(this.node != 0 && argv.length > 1 && (addr = isNumber(argv[1])) != -1){
      if(this.sim.isNode(addr)){
        StringBuffer msg = new StringBuffer();
        if(argv.length > 2){
          for(int i = 2; i < argv.length; ++i){
            msg.append(argv[i]);
            if(i != argv.length-1)
              msg.append(' ');
          }
        }
        this.sim.getNode(this.node).send(addr, msg.toString());
      }
      else
        output(addr+": no such node ...\n");
    }
    else if(this.node == 0)
      output("ERROR: you have to be logged in\n");
    else
      output("usage: send <address> [<text>]\n");
  }

  private synchronized void traceroute(String[] argv){
    int addr;
    if(this.node != 0 && argv.length == 2 && (addr = isNumber(argv[1])) != -1){
      if(this.sim.isNode(addr)){
        output("traceroute\n");
        this.trace(addr);
      }
      else
        output(addr+": no such node ...\n");
    }
    else if(this.node == 0)
      output("ERROR: you have to be logged in\n");
    else
      output("usage: traceroute <address>\n");
  }

  private synchronized void trace(int addr){
    if(tracing == false){
      this.cmdline.setEditable(false);
      tracing =true;
      ttl = 0;
      a = addr;
    }
    if(ttl == 16){
      this.write("no route to node ...");
    }
    else this.sim.getNode(this.node).send(a, "TRACE", ++ttl);
  }

  public void keyReleased(KeyEvent ke){
  }

}

