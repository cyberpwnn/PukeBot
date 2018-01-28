package com.volmit.pukebot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface ICommand
{
	public String getName();

	public String[] getAliases();

	public void onCommand(MessageReceivedEvent e, String[] a);

	public String getUsage();
}
