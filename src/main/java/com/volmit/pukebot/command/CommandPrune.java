package com.volmit.pukebot.command;

import com.volmit.pukebot.Apparatus;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandPrune extends CommandBase
{
	public CommandPrune()
	{
		super("prunedata", new String[] {"prune"});
	}

	public void onCommand(MessageReceivedEvent e, String[] a)
	{
		e.getTextChannel().sendTyping().queue();
		if(!e.getMember().hasPermission(Permission.ADMINISTRATOR))
		{
			e.getTextChannel().sendMessage(sendError("No", "You are not an administrator of Apparatus.").build()).queue();
			return;
		}

		Apparatus.data.getMessages().clear();
		Apparatus.data.requestSave();

		e.getMessage().delete().queue();
		e.getTextChannel().sendMessage(send("Poof!", "Messages logged have beend cleared. A datastore save has been requested. You can also use /save to save this change right now.").build()).queue();
	}
}
