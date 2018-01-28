package com.volmit.pukebot.command;

import java.io.IOException;

import com.volmit.pukebot.Apparatus;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandSave extends CommandBase
{
	public CommandSave()
	{
		super("saveall", new String[] {"save"});
	}

	public void onCommand(MessageReceivedEvent e, String[] a)
	{
		e.getTextChannel().sendTyping().queue();
		if(!e.getMember().hasPermission(Permission.ADMINISTRATOR))
		{
			e.getTextChannel().sendMessage(sendError("No", "You are not an administrator of Apparatus.").build()).queue();
			return;
		}

		try
		{
			Apparatus.data.saveRightFuckingNow();
		}

		catch(IOException e1)
		{
			e1.printStackTrace();
			e.getTextChannel().sendMessage(sendError("Failed!", "Something happened: " + e1.getMessage()).build()).queue();
			return;
		}

		e.getMessage().delete().queue();
		e.getTextChannel().sendMessage(send("Saved All", "Persistent data flushed to disk.").build()).queue();
	}
}
