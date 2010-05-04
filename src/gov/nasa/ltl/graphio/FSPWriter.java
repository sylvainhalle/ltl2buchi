/**
 * 
 */
package gov.nasa.ltl.graphio;

import java.io.PrintStream;

import gov.nasa.ltl.graph.Guard;
import gov.nasa.ltl.graph.Edge;
import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.Literal;
import gov.nasa.ltl.graph.Node;
import gov.nasa.ltl.trans.State;
import gov.nasa.ltl.trans.Transition;

/**
 * @author estar
 *
 */
class FSPWriter<PropT> extends Writer<PropT> {
  private PrintStream out;
  
  FSPWriter(PrintStream s) {
    out = s;
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.graph.Graph)
   */
  @Override
  public void write (Graph<PropT> g) {
    boolean first = true;
    if (g.getInit () == null) {
      out.print ("Empty");
      return;
    }
    out.print ("RES = S" + g.getInit ().getId ());
    for (Node<PropT> n: g.getNodes ()) {
      out.println (',');
      write (n);
    }
    out.println ('.');
    if (g.getIntAttribute ("nsets") != 0) { // gba?
      for (int i = 0; i < g.getIntAttribute ("nsets"); i++) {
        out.print ("AS" + i + " = { ");
        for (Node<PropT> n: g.getNodes ()) {
          if (!n.getBooleanAttribute ("acc" + i))
            continue;
          if (!first)
            out.print (", ");
          first = false;
          out.print ("S" + n.getId ());
        }
        out.println (" }");
      }
    } else { // ba?
      out.print ("AS = { ");
      for (Node<PropT> n: g.getNodes ()) {
        if (!n.getBooleanAttribute ("accepting"))
          continue;
        if (!first)
          out.print (", ");
        first = false;
        out.print ("S" + n.getId ());
      }
      out.println (" }");
    }
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.graph.Node)
   */
  @Override
  public void write (Node<PropT> n) {
    boolean first = true;
    out.print ("S" + n.getId () + "=(");
    for (Edge<PropT> e: n.getOutgoingEdges ()) {
      if (!first)
        out.print (" |");
      first = false;
      write (e);
    }
    out.print (')');
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.graph.Edge)
   */
  @Override
  public void write (Edge<PropT> e) {
    int nsets = e.getSource ().getGraph ().getIntAttribute ("nsets");
    boolean first = true;
    write (e.getGuard ());
    if (nsets != 0) {
      for (int i = 0; i < nsets; i++) {
        if (e.getBooleanAttribute ("acc" + i)) {
          if (first)
            out.print ('{');
          else
            out.print (',');
          first = false;
          out.print (i);
        }
      }
      if (!first)
        out.print ('}');
    } else
      if (e.getBooleanAttribute ("accepting"))
        out.print ('@');
    out.print ("-> S" + e.getNext ().getId ());
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.trans.State<PropT>[])
   */
  @Override
  public void write (State<PropT>[] states) {
    if (states == null || states.length == 0)
      out.print ("\n\nRES = STOP.\n");
    out.print ("\n\nRES = S0");
    /* TODO: Is it all right to use states.length instead of
     * Pool.assign here?
     */
//    for (int i = 0; i < Pool.assign(); i++) {
    for (int i = 0; i < states.length; i++) {
      if (i != states[i].get_representativeId ()) // not representative
        continue;
      out.print (',');
      write (states[i]);
    }
    out.print (".\n");
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.trans.State)
   */
  @Override
  public void write (State<PropT> s) {
    boolean first = true;
    out.print ('S');
    out.print (s.get_representativeId ());
    out.print ('=');
    for (Transition<PropT> t: s.getTransitions ()) {
      if (first)
        out.print ('(');
      else
        out.print ('|');
      first = false;
      write (t);
    }
    if (!first) // loop ran at least once, so we’ve written '('
      out.print (')');
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.trans.Transition)
   */
  @Override
  public void write (Transition<PropT> t) {
    write (t.getGuard ());
    out.print ('{');
    if (gov.nasa.ltl.trans.Node.accepting_conds == 0) {
      if (t.isSafeAccepting ())
        out.print ('0');
    } else
      for (int i = 0; i < gov.nasa.ltl.trans.Node.accepting_conds; i++)
        if (t.isAccepting (i))
          out.print (i);
    out.print ("} -> S" + t.goesTo () + " ");
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.trans.Guard)
   */
  @Override
  public void write (Guard<PropT> g) {
    boolean first = true;
    if (g.isEmpty ())
      write (new Literal<PropT> (null, false, true));
    for (Literal<PropT> l: g) {
      if (!first)
        out.print ("&");
      first = false;
      write (l);
    }
  }

  @Override
  public void write (Literal<PropT> l) {
    if (l.isTrue ())
      out.print ("TRUE");
    else {
      if(l.isNegated ())
        out.print ('!');
      out.print (l.getAtom ());
    }
  }
}
