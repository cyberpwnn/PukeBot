package com.volmit.pukebot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandPing extends CommandBase
{
	public CommandPing()
	{
		super("ping", new String[] {"pingpong", "pong"});
	}

	public void onCommand(MessageReceivedEvent e, String[] a)
	{
		e.getTextChannel().sendTyping().queue();
		e.getMessage().delete().queue();
		e.getTextChannel().sendMessage(send("Pong!", "Yes, I'm here.").build()).queue();
	}
}
