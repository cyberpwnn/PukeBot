package com.volmit.pukebot;

public class PukeBot
{
	private static PukeBotInstance bot;
	
	public static void main(String[] puke)
	{
		try
		{
			bot = new PukeBotInstance(puke);
		}
		
		catch(Exception e)
		{
			System.out.println("Invalid Parameters -token:<email> is expected.");
		}
	}
}
