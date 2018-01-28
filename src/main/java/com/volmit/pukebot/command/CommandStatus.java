package com.volmit.pukebot.command;

import com.volmit.pukebot.F;
import com.volmit.pukebot.Apparatus;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandStatus extends CommandBase
{
	public CommandStatus()
	{
		super("status", new String[] {"stats"});
	}

	public void onCommand(MessageReceivedEvent e, String[] a)
	{
		e.getTextChannel().sendTyping().queue();
		if(!e.getMember().hasPermission(Permission.ADMINISTRATOR))
		{
			e.getTextChannel().sendMessage(sendError("No", "You are not an administrator of Apparatus.").build()).queue();
			return;
		}

		Apparatus.data.getMessages();
		Apparatus.data.requestSave();
		EmbedBuilder eb = send("Apparatus Status", "Current status information for Apparatus");
		eb.addField("Cache", "Size: " + F.fileSize(Apparatus.data.getDstore().length()), true);
		eb.addField("Data", Apparatus.data.getMessages().size() + " messages stored.", true);
		e.getMessage().delete().queue();
		e.getTextChannel().sendMessage(eb.build()).queue();
	}
}
