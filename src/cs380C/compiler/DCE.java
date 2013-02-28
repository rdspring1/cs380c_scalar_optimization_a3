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
		HashMap<Integer, Set<String>> analysis = new LA(cmdlist, graph).liveAnalysis();
		
		ListIterator<String> iter = cmdlist.listIterator();
		while(iter.hasNext())
		{
			String[] cmd = iter.next().split(":")[1].trim().split("\\s");
			int blocknum = getBlockNum(graph, numline);
			
			if(blocknum < 0 && checkRemove(cmd, analysis.get(blocknum)))
			{
				iter.remove();
				updateCmdlist(iter, numline);
				return true;
			}
			++numline;
		}	
		return false;
	}

	private void updateCmdlist(ListIterator<String> iter, int numline) {
		// All instructions and references are decremented by
		// the number of lines removed from the command list
		while(iter.hasNext())
		{
			iter.set(updateLine(iter.next()));
			++numline;
		}
	}

	private String updateLine(String line) {
		// TODO Update the line number and references in the line
		return null;
	}

	private boolean checkRemove(String[] cmd, Set<String> set) {
		// Determine if the command is live or dead
		for(int i = 1; i < cmd.length; ++i)
		{
			String arg = cmd[i].split("#")[0];
			if(cmd[i].contains("#") && !arg.contains("_base"))
			{
				if(!set.contains(arg))
					return true;
			}
		}
		return false;
	}

	private int getBlockNum(CFG graph, int numline) {
		// Return the block number for the current line
		int function = graph.getPrevFunction(numline);
		return graph.getCurrentBlock(function, numline);
	}
}
