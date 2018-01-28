package com.volmit.pukebot.command;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.core.EmbedBuilder;

public abstract class CommandBase implements ICommand
{
	private String name;
	private List<String> aliases;
	private String usage;

	public CommandBase(String name, String[] names)
	{
		this.name = name;
		this.aliases = new ArrayList<String>();

		for(String i : names)
		{
			aliases.add(i);
		}

		usage = "";
	}

	public void setUsage(String s)
	{
		this.usage = s;
	}

	public String getUsage()
	{
		return usage;
	}

	public EmbedBuilder send(String title, String description)
	{
		return sendColored(title, description, new Color(0, 255, 144));
	}

	public EmbedBuilder sendError(String title, String description)
	{
		return sendColored(title, description, new Color(255, 80, 0));
	}

	public EmbedBuilder sendColored(String title, String description, Color c)
	{
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(title);
		eb.setColor(c);
		eb.setDescription(description);
		return eb;
	}

	public String getName()
	{
		return name;
	}

	public String[] getAliases()
	{
		return aliases.toArray(new String[aliases.size()]);
	}
}
