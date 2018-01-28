package com.volmit.pukebot;

public class PukeBot
{
	private static Apparatus bot;
	
	public static void main(String[] puke)
	{
		try
		{
			bot = new Apparatus(puke);
		}
		
		catch(Exception e)
		{
			System.out.println("Invalid Parameters -token:<email> is expected.");
		}
	}
}
