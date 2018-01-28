package com.volmit.pukebot.command;

import java.awt.Color;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandEmbed extends CommandBase
{
	public CommandEmbed()
	{
		super("embed", new String[] {"emb", "eb"});
		setUsage("<title||desc> [||field:desc;..] [||#color]");
	}

	public void onCommand(MessageReceivedEvent e, String[] a)
	{
		e.getTextChannel().sendTyping().queue();

		String ss = "";

		for(String i : a)
		{
			ss += i + " ";
		}

		if(!ss.contains("||"))
		{
			e.getTextChannel().sendMessage(sendError("Invalid arguments for /" + getName(), "/" + getName() + " " + getUsage()).build()).queue();
			return;
		}

		EmbedBuilder eb = new EmbedBuilder();
		String title = ss.split("\\Q||\\E")[0].trim();
		String desc = ss.split("\\Q||\\E")[1].trim();
		eb.setTitle(title);
		eb.setDescription(desc);
		eb.setColor(new Color(61, 135, 255));

		if(ss.split("\\Q||\\E").length > 2)
		{
			String m = ss.split("\\Q||\\E")[2];

			if(m.contains(";"))
			{
				for(String i : m.split(";"))
				{
					if(i.contains("::"))
					{
						String f = i.split("\\Q::\\E")[0];
						String t = i.split("\\Q::\\E")[1];
						eb.addField(f, t, true);
					}
				}
			}
		}

		if(ss.split("\\Q||\\E").length > 3)
		{
			String m = ss.split("\\Q||\\E")[3];

			try
			{
				eb.setColor(Color.decode(m.toUpperCase().trim()));
			}

			catch(Exception exx)
			{
				e.getTextChannel().sendMessage(sendError("Invalid argument for COLOR", "\"" + m + "\"" + " -> " + exx.getMessage()).build()).queue();
			}
		}

		e.getMessage().delete().queue();
		e.getTextChannel().sendMessage(eb.build()).queue();
	}
}
