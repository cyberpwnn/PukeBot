package com.volmit.pukebot.command;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandCalc extends CommandBase
{
	public CommandCalc()
	{
		super("calc", new String[] {"/calc", "math", "func"});
		setUsage("<function>");
	}

	@SuppressWarnings("deprecation")
	public void onCommand(final MessageReceivedEvent e, String[] a)
	{
		e.getTextChannel().sendTyping().queue();

		if(a.length == 0)
		{
			e.getTextChannel().sendMessage(sendError("Invalid arguments", "/calc <function>").build()).queue();
			return;
		}

		final String b[] = a;

		Thread tt = new Thread()
		{
			@SuppressWarnings("restriction")
			@Override
			public void run()
			{
				try
				{
					String function = "";

					for(String i : b)
					{
						function += i.replaceAll("\n", " ") + " ";
					}

					function = function.trim().replaceAll("```", "").replaceAll("`", "");
					ScriptEngineManager mgr = new ScriptEngineManager();
					ScriptEngine scriptEngine = mgr.getEngineByName("JavaScript");

					String value = scriptEngine.eval(function).toString();
					e.getTextChannel().sendMessage(send("Ding!", value).build()).queue();
				}

				catch(Throwable e)
				{

				}
			}
		};

		tt.start();

		try
		{
			tt.join(500);

			if(tt.isAlive())
			{
				System.out.println("OVERFLOW");
				e.getTextChannel().sendMessage(sendError("Computational Overflow", "Stop it.").build()).queue();
				tt.interrupt();
				tt.stop();
				tt.destroy();
			}
		}

		catch(Throwable e1)
		{
			e1.printStackTrace();
		}
	}
}
