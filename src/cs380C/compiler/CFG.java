package cs380C.compiler;

import java.util.*;

public class CFG
{
	private String[] arrayCmdlist;
	private LinkedList<String> cmdlist = new LinkedList<String>();
	private LinkedList<Integer> functions = new LinkedList<Integer>();
	private LinkedHashMap<Integer, SortedSet<Integer>> nodes = new LinkedHashMap<Integer, SortedSet<Integer>>();
	private HashMap<Integer, SortedSet<Integer>> edges = new HashMap<Integer, SortedSet<Integer>>();
	
	public CFG(LinkedList<String> input)
	{
		cmdlist = input;
		arrayCmdlist = input.toArray(new String[cmdlist.size()]);
		edges.put(-1, new TreeSet<Integer>());
		generateCFG();
		edges.remove(-1);
	}
	public String toString()
	{	
		StringBuilder str = new StringBuilder();
		for(Integer id : functions)
		{
			str.append("Function: " + id.toString() + "\n");
			str.append("Basic blocks:");
			for(Integer node : nodes.get(id))
				str.append(" " + node);
			str.append("\nCFG:\n");
			for(Integer node : nodes.get(id))
			{
				str.append(node + " ->");
				for(Integer edge : edges.get(node))
					str.append(" " + edge);
				str.append("\n");
			}
		}
		return str.toString();
	}
	private void generateCFG() 
	{
		int numline = 1;
		int currentFunction = -1;
		
		for(String line : cmdlist)
		{
			String[] cmd = line.split(":")[1].trim().split("\\s");
			
			if(cmd[0].equals("enter"))
			{
				currentFunction = numline;
				functions.add(currentFunction);
				nodes.put(currentFunction, new TreeSet<Integer>());
				nodes.get(currentFunction).add(numline);
				edges.put(numline, new TreeSet<Integer>());
			}
			else if(cmd[0].equals("br"))
			{
				int jmpline = Integer.valueOf(cmd[1].substring(1, cmd[1].length() - 1));
				
				if(nodes.get(currentFunction).contains(numline))
				{
					edges.get(getPrevBlock(currentFunction, numline)).add(numline);
				}
				
				if(edges.get(getPrevBlock(currentFunction, getPrevBlock(currentFunction, numline))).size() == 0)
				{
						edges.get(getPrevBlock(currentFunction, getPrevBlock(currentFunction, numline))).add(getPrevBlock(currentFunction, numline));
				}
				
				nodes.get(currentFunction).add(numline + 1);
				edges.put(numline + 1, new TreeSet<Integer>());
				
				if(!nodes.get(currentFunction).contains(jmpline))
				{
					nodes.get(currentFunction).add(jmpline);
					if(arrayCmdlist[jmpline - 2].split(":")[1].trim().split("\\s")[0].equals("enter"))
					{
						edges.put(jmpline, new TreeSet<Integer>(edges.get(jmpline - 1)));
						edges.put(jmpline - 1, new TreeSet<Integer>());
						edges.get(jmpline - 1).add(jmpline);
					}
					else
					{
						edges.put(jmpline, new TreeSet<Integer>());
						edges.get(getPrevBlock(currentFunction, numline + 1)).add(jmpline);
					}
					edges.get(getPrevBlock(currentFunction, numline + 1)).add(jmpline);
				}
				else
				{						
					edges.get(getPrevBlock(currentFunction, numline + 1)).add(jmpline);
				}
			}
			else if(cmd[0].equals("call"))
			{
				nodes.get(currentFunction).add(numline + 1);
				edges.put(numline + 1, new TreeSet<Integer>());
				edges.get(getPrevBlock(currentFunction, numline)).add(numline + 1);
			}
			else if(cmd[0].equals("blbc") || cmd[0].equals("blbs"))
			{
				int startline = startCondition(numline - 1);
				int endline = numline + 1;
				int jmpline = Integer.valueOf(cmd[2].substring(1, cmd[2].length() - 1));
				
				if(arrayCmdlist[startline - 2].split(":")[1].trim().split("\\s")[0].equals("enter"))
					--startline;
				else
					nodes.get(currentFunction).add(startline);
				
				if(!edges.containsKey(startline))
					edges.put(startline, new TreeSet<Integer>());
				
				edges.get(startline).add(endline);
				edges.get(startline).add(jmpline);
				
				nodes.get(currentFunction).add(endline);
				if(!edges.containsKey(endline))
					edges.put(endline, new TreeSet<Integer>());
				
				nodes.get(currentFunction).add(jmpline);
				if(!edges.containsKey(jmpline))
					edges.put(jmpline, new TreeSet<Integer>());
				
				if(edges.get(getPrevBlock(currentFunction, startline)).size() == 0)
					edges.get(getPrevBlock(currentFunction, startline)).add(startline);
			}
			else if(cmd[0].equals("ret"))
			{
				if(nodes.get(currentFunction).contains(numline) && edges.get(getPrevBlock(currentFunction, numline)).size() == 0)
					edges.get(getPrevBlock(currentFunction, numline)).add(numline);
			}
			++numline;
		}
	}
	public int getPrevBlock(int currentFunction, int numline) 
	{
		SortedSet<Integer> prevSet = nodes.get(currentFunction).headSet(numline);
		if(prevSet.size() > 0)
			return prevSet.last();
		else
			return -1;
	}
	public int getNextBlock(int currentFunction, int numline) 
	{
		SortedSet<Integer> nextSet = nodes.get(currentFunction).tailSet(numline);
		if(nextSet.size() > 0)
			return nextSet.first();
		else
			return -1;
	}
	private int startCondition(int numline) 
	{
		String[] cmd = arrayCmdlist[numline].split(":")[1].trim().split("\\s");
		if(!arrayCmdlist[numline].contains("(") && !arrayCmdlist[numline].contains(")"))
		{
			return numline + 1;
		}
		else
		{
			int left = -1;
			int right = -1;
			
			if(cmd.length > 1)
				left = lineRef(cmd[1]);
			
			if(cmd.length > 2)
				right = lineRef(cmd[2]);
			
			if(left != -1 && right != -1)
			{
				return Math.min(startCondition(left), startCondition(right));
			}
			else if(left != -1)
			{
				return startCondition(left);
			}
			else if(right != -1)
			{
				return startCondition(right);
			}
		}
		
		return -1;
	}
	private int lineRef(String line) 
	{
		if(!line.contains("(") && !line.contains(")"))
			return -1;
		
		return Integer.valueOf(line.substring(1, line.length() - 1)) - 1;
	}
	public SortedSet<Integer> getNodes(int function)
	{
		return nodes.get(function);
	}
	public SortedSet<Integer> getEdges(int node)
	{
		return edges.get(node);
	}
	public Iterator<Integer> getIterator() {
		return functions.iterator();
	}

}
