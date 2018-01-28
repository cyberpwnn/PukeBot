package com.volmit.pukebot.command;

import com.volmit.pukebot.Apparatus;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandHelp extends CommandBase
{
	public CommandHelp()
	{
		super("help", new String[] {"?", "h"});
	}

	public void onCommand(MessageReceivedEvent e, String[] a)
	{
		e.getTextChannel().sendTyping().queue();
		EmbedBuilder eb = send("Apparatus Help", "This may be useful depending on if you can read or not.");

		for(ICommand i : Apparatus.getInst().getCommands())
		{
			String ss = "";

			for(String j : i.getAliases())
			{
				ss += ", /" + j;
			}

			if(ss.length() >= 2)
			{
				ss = ss.substring(2);
			}

			eb.addField("/" + i.getName() + " " + i.getUsage() + "", "Or " + ss, true);
		}

		e.getMessage().delete().queue();
		e.getTextChannel().sendMessage(eb.build()).queue();
	}
}
