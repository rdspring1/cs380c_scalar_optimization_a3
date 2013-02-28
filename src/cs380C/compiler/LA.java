package cs380C.compiler;

import java.util.*;

public class LA {
	private static List<String> DEFCMD = Arrays.asList("store", "move");
	
	private LinkedList<String> cmdlist = new LinkedList<String>();
	private CFG cfg;
	
	private HashMap<Integer, String> def = new HashMap<Integer, String>();
	private HashMap<Integer, Set<String>> use = new HashMap<Integer, Set<String>>();

	private LinkedHashMap<Integer, Set<String>> instructLiveSet = new LinkedHashMap<Integer, Set<String>>();
	private LinkedHashMap<Integer, Set<String>> in = new LinkedHashMap<Integer, Set<String>>();
	private LinkedHashMap<Integer, Set<String>> out = new LinkedHashMap<Integer, Set<String>>();
	
	public LA(LinkedList<String> input, CFG cfg)
	{
		this.cmdlist = input;
		this.cfg = cfg;
		generateDefUse();
		setupBlocks();
	}

	public HashMap<Integer, Set<String>> liveAnalysis()
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
				in.put(block, new TreeSet<String>());
				out.put(block, new TreeSet<String>());
			}
		}
	}

	private void generateDefUse() {
		int numline = 1;
		
		for(String line : cmdlist)
		{
			// Setup instructLiveSet
			instructLiveSet.put(numline, new TreeSet<String>());
			
			Integer linenum = Integer.valueOf(numline);
			String[] cmd = line.split(":")[1].trim().split("\\s");
			
			if(acceptCmd(cmd[0], DEFCMD))
			{
				if(cmd[1].contains("#"))
				{
					use.put(linenum, new TreeSet<String>());
					use.get(linenum).add(cmd[1].split("#")[0]);
				}
				
				if(cmd[2].contains("#"))
					def.put(linenum, cmd[2].split("#")[0]);
			}
			else
			{
				for(int i = 1; i < cmd.length; ++i)
				{
					String arg = cmd[i].split("#")[0];
					if(cmd[i].contains("#") && !arg.contains("_base"))
					{
						if(!use.containsKey(linenum))
							use.put(linenum, new TreeSet<String>());
						use.get(linenum).add(arg);
					}
				}
			}
			
			++numline;
		}
	}

	private boolean acceptCmd(String cmd, Iterable<String> iter) {
		for(String i : iter)
		{
			if(cmd.equals(i))
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private boolean ignoreCmd(String cmd, Iterable<String> iter) {
		for(String i : iter)
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
		
		// out[B] = Union of in[B->Successors]
		SortedSet<Integer> successors = cfg.getEdges(block);
		for(Integer i : successors)
			out.get(block).addAll(in.get(i));
		
		int startline = block;
		int endline = cfg.getNextBlock(function, block) - 1;
		
		if(endline < 0)
			endline = cfg.getNextFunction(function) - 1;
		
		instructLiveSet.put(endline, out.get(block));
		
		if(startline != endline)
		{
			for(int currentline = endline; currentline >= startline; --currentline)
			{
				// out[currentline - 1] = (out[currentline] - def[currentline]) U use[currentline]
				Set<String> newSet = new TreeSet<String>(instructLiveSet.get(currentline));
				
				if(def.containsKey(currentline))
					newSet.remove(def.get(currentline));
				
				if(use.containsKey(currentline))
					newSet.addAll(use.get(currentline));
				
				if(currentline == startline)
				{
					if(!in.get(startline).equals(newSet))
						update = true;
					
					in.put(startline, newSet);
				}
				else
				{
					instructLiveSet.put(currentline - 1, newSet);
				}
			}
		}
		
		return update;
	}
}
