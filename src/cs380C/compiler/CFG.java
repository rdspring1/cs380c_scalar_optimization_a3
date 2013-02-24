package cs380C.compiler;

import java.util.*;

public class CFG 
{
	private static String[] arrayOutput;
	private static LinkedList<String> cmdlist = new LinkedList<String>();
	private static LinkedList<Integer> functions = new LinkedList<Integer>();
	private static LinkedHashMap<Integer, SortedSet<Integer>> nodes = new LinkedHashMap<Integer, SortedSet<Integer>>();
	private static HashMap<Integer, SortedSet<Integer>> edges = new HashMap<Integer, SortedSet<Integer>>();
	
	public static String translator(LinkedList<String> input)
	{
		StringBuilder str = new StringBuilder();
		cmdlist = input;
		arrayOutput = input.toArray(new String[cmdlist.size()]);
		
		generateCFG();
		
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
	private static void generateCFG() 
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
					edges.get(getprevBlock(currentFunction, numline)).add(numline);
				}
				
				if(edges.get(getprevBlock(currentFunction, getprevBlock(currentFunction, numline))).size() == 0)
				{
						edges.get(getprevBlock(currentFunction, getprevBlock(currentFunction, numline))).add(getprevBlock(currentFunction, numline));
				}
				
				nodes.get(currentFunction).add(numline + 1);
				edges.put(numline + 1, new TreeSet<Integer>());
				
				if(!nodes.get(currentFunction).contains(jmpline))
				{
					nodes.get(currentFunction).add(jmpline);
					edges.put(jmpline, new TreeSet<Integer>());
					edges.get(getprevBlock(currentFunction, numline + 1)).add(jmpline);
				}
				else
				{
					edges.get(getprevBlock(currentFunction, numline + 1)).add(jmpline);
				}
			}
			else if(cmd[0].equals("call"))
			{
				nodes.get(currentFunction).add(numline + 1);
				edges.put(numline + 1, new TreeSet<Integer>());
				edges.get(getprevBlock(currentFunction, numline)).add(numline + 1);
			}
			else if(cmd[0].equals("blbc") || cmd[0].equals("blbs"))
			{
				int startline = startCondition(numline - 1);
				int endline = numline + 1;
				int jmpline = Integer.valueOf(cmd[2].substring(1, cmd[2].length() - 1));
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
				
				if(edges.get(getprevBlock(currentFunction, startline)).size() == 0)
					edges.get(getprevBlock(currentFunction, startline)).add(startline);
			}
			++numline;
		}
	}
	private static int getprevBlock(int currentFunction, int numline) 
	{
		return nodes.get(currentFunction).headSet(numline).last();
	}
	private static int startCondition(int numline) 
	{
		String[] cmd = arrayOutput[numline].split(":")[1].trim().split("\\s");
		if(!arrayOutput[numline].contains("(") && !arrayOutput[numline].contains(")"))
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
	private static int lineRef(String line) 
	{
		if(!line.contains("(") && !line.contains(")"))
			return -1;
		
		return Integer.valueOf(line.substring(1, line.length() - 1)) - 1;
	}
}
