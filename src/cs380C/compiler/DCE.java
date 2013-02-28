package cs380C.compiler;

import java.util.*;

// Dead Code Elimination
public class DCE implements Optimization {

	private LinkedList<String> cmdlist;
	
	private static DCE instance;
	private DCE() { }
	
	public static DCE instance() 
	{
		if(instance == null)
			instance = new DCE();
		return instance;
	}
	
	@Override
	public LinkedList<String> performOptimization(LinkedList<String> input) {
		this.cmdlist = input;
		while(update());
		return cmdlist;
	}
	private boolean update() {
		int numline = 1;
		CFG graph = new CFG(cmdlist);
		graph.updateCFG();
		HashMap<Integer, Set<String>> analysis = new LA(cmdlist, graph).liveAnalysis();
		
		ListIterator<String> iter = cmdlist.listIterator();
		while(iter.hasNext())
		{
			String[] cmd = iter.next().split(":")[1].trim().split("\\s");
			int blocknum = getBlockNum(graph, numline);
			
			if(blocknum > 0 && checkRemove(cmd, analysis.get(blocknum)))
			{
				iter.remove();
				updateCmdlist(numline);
				return true;
			}
			++numline;
		}	
		return false;
	}

	private void updateCmdlist(int linechange) {
		// All instructions and references are decremented by
		// the number of lines removed from the command list
		ListIterator<String> iter = cmdlist.listIterator();
		int numline = 1;
		while(iter.hasNext())
		{
			iter.set(updateLine(iter.next(), numline, linechange));
			++numline;
		}
	}

	private String updateLine(String line, int numline, int linechange) {
		// Update the line number and references in the line
		int linenum  = Integer.valueOf(line.split(":")[0].trim().split("\\s")[1]);
		
		if(numline >= linechange)
			--linenum;
		
		String[] cmd = line.split(":")[1].trim().split("\\s");
		for(int i = 1; i < cmd.length; ++i)
		{
			if(cmd[i].contains("(") && cmd[i].contains(")") && numline >= linechange)
			{
				int reference = Integer.valueOf(cmd[i].substring(1, cmd[i].length() - 1));
				--reference;
				cmd[i] = "(" + reference + ")"; 
			}
			else if(cmd[i].contains("[") && cmd[i].contains("]"))
			{
				int reference = Integer.valueOf(cmd[i].substring(1, cmd[i].length() - 1));
				
				if(reference > linechange)
				{
					--reference;
					cmd[i] = "[" + reference + "]";
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("    instr " + linenum + ": " + cmd[0]);
		for(int i = 1; i < cmd.length; ++i)
		{
			sb.append(" " + cmd[i]);
		}
		return sb.toString();
	}

	private boolean checkRemove(String[] cmd, Set<String> set) {
		// Determine if the command is live or dead
		if(LA.acceptCmd(cmd[0], LA.DEFCMD))
		{		
			if(cmd[2].contains("#") && !set.isEmpty())
				return !set.contains(cmd[2].split("#")[0]);
		}
		return false;
	}

	private int getBlockNum(CFG graph, int numline) {
		// Return the block number for the current line
		int function = graph.getCurrentFunction(numline);
		
		if(function != -1)
			return graph.getCurrentBlock(function, numline);
		else
			return -1;
	}
}
