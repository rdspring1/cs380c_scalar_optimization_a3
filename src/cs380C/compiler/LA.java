package cs380C.compiler;

import java.util.*;

public class LA {
	private static String[] ignore = { "enter", "ret", "write", "wrl", "store", "param", "call", "br", "blbs", "blbc", "br", "entrypc"};
	
	private LinkedList<String> cmdlist = new LinkedList<String>();
	private CFG cfg;
	
	private boolean[] def;
	private HashMap<Integer, Set<Integer>> use = new HashMap<Integer, Set<Integer>>();

	private LinkedHashMap<Integer, Set<Integer>> instructLiveSet = new LinkedHashMap<Integer, Set<Integer>>();
	private LinkedHashMap<Integer, Set<Integer>> in = new LinkedHashMap<Integer, Set<Integer>>();
	private LinkedHashMap<Integer, Set<Integer>> out = new LinkedHashMap<Integer, Set<Integer>>();
	
	public LA(LinkedList<String> input, CFG cfg)
	{
		this.cmdlist = input;
		this.cfg = cfg;
		def = new boolean[cmdlist.size()];
		generateDefUse();
		setupBlocks();
	}

	public HashMap<Integer, Set<Integer>> liveAnalysis()
	{		
		while(update());	
		return out;
	}
	
	private void setupBlocks() {
		Iterator<Integer> funcIter = cfg.getIterator();
		while(funcIter.hasNext())
		{
			Integer func = funcIter.next();
			SortedSet<Integer> nodes = cfg.getNodes(func);
			for(Integer block : nodes)
			{
				in.put(block, new TreeSet<Integer>());
				out.put(block, new TreeSet<Integer>());
			}
		}
	}

	private void generateDefUse() {
		int numline = 1;
		
		for(String line : cmdlist)
		{
			// Setup instructLiveSet
			instructLiveSet.put(numline, new TreeSet<Integer>());
			
			String[] cmd = line.split(":")[1].trim().split("\\s");
			
			if(checkCmd(cmd[0]))
				def[numline - 1] = true;
			else
				def[numline - 1] = false;
			
			for(int i = 1; i < cmd.length; ++i)
			{
				if(cmd[i].contains("(") && cmd[i].contains(")"))
				{
					Integer linenum = Integer.valueOf(numline);
					if(!use.containsKey(linenum))
						use.put(linenum, new TreeSet<Integer>());
					use.get(linenum).add(Integer.valueOf(cmd[i].substring(1, cmd[i].length() - 1)));
				}
			}
			++numline;
		}
	}

	private boolean checkCmd(String cmd) {
		for(String i : ignore)
		{
			if(cmd.equals(i))
				return false;
		}
		return true;
	}

	private boolean update() {
		boolean update = false;
		Iterator<Integer> funcIter = cfg.getIterator();
		
		while(funcIter.hasNext())
		{
			boolean result = updateFunc(funcIter.next());
			if(!update)
				update = result;
		}
		
		return update;
	}

	private boolean updateFunc(Integer function) {
		boolean update = false;
		Iterator<Integer> funcIter = cfg.getNodes(function).iterator();
		
		while(funcIter.hasNext())
		{
			boolean result = updateBlock(function, funcIter.next());
			if(!update)
				update = result;
		}
		
		return update;
	}

	private boolean updateBlock(Integer function, Integer block) {
		boolean update = false;
		
		// out[B] = U in[B-successors]
		SortedSet<Integer> successors = cfg.getEdges(block);
		Set<Integer> outblock = out.get(block);
		for(Integer i : successors)
		{
			outblock.addAll(in.get(i));
		}
		out.put(block, outblock);
		
		int startline = block;
		int currentline = cfg.getNextBlock(function, block) - 1;
		instructLiveSet.put(currentline, out.get(block));
		--currentline;
		
		for(; currentline >= startline; --currentline)
		{
			// out[currentline] = (out[currentline + 1] - def[currentline + 1]) U use[currentline + 1]
			Set<Integer> newSet = instructLiveSet.get(currentline + 1);
			
			if(def[currentline + 1])
				newSet.remove(currentline + 1);
			
			if(!use.get(currentline + 1).isEmpty())
				newSet.addAll(use.get(currentline + 1));
			
			instructLiveSet.put(currentline, newSet);
		}
		
		Set<Integer> newSet = instructLiveSet.get(currentline + 1);
		
		if(def[currentline + 1])
			newSet.remove(currentline + 1);
		
		if(!use.get(currentline + 1).isEmpty())
			newSet.addAll(use.get(currentline + 1));
		
		if(in.get(startline).equals(newSet))
			update = true;
		
		in.put(startline, newSet);
		
		return update;
	}
}
