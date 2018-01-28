package com.volmit.pukebot;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import com.volmit.pukebot.command.CommandCalc;
import com.volmit.pukebot.command.CommandEmbed;
import com.volmit.pukebot.command.CommandHelp;
import com.volmit.pukebot.command.CommandPing;
import com.volmit.pukebot.command.CommandPrune;
import com.volmit.pukebot.command.CommandSave;
import com.volmit.pukebot.command.CommandStatus;
import com.volmit.pukebot.command.ICommand;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;

public class Apparatus implements EventListener
{
	private AuthSet auth;
	private JDA api;
	private List<ICommand> commands;
	private List<String> logs;
	private TextChannel logChannel;
	private TextChannel updateChannel;
	public static Apparatus inst;
	public static DStore data;
	public static int t = 0;
	private List<Long> middels;
	private Map<Long, String> midrens;
	private User myself;
	private Guild volmit;
	private Role support;
	private Role banished;
	private TextChannel garbageChannel;

	public Apparatus(String[] puke) throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException
	{
		try
		{
			inst = this;
			middels = new ArrayList<Long>();
			midrens = new HashMap<Long, String>();
			auth = new AuthSet(puke);
			commands = new ArrayList<ICommand>();
			logs = new ArrayList<String>();
			api = new JDABuilder(AccountType.BOT).setToken(auth.getToken()).addEventListener(this).buildBlocking();
			commands.add(new CommandHelp());
			commands.add(new CommandPing());
			commands.add(new CommandEmbed());
			commands.add(new CommandSave());
			commands.add(new CommandPrune());
			commands.add(new CommandStatus());
			commands.add(new CommandCalc());
			File f = new File("data.btf");
			data = new DStore(f);

			log("Starting Apperatus");

			for(Guild i : getApi().getGuilds())
			{
				if(i.getName().equals("Volmit Support"))
				{
					volmit = i;
					log("  Found guild: " + volmit.getName() + " -> " + volmit.getIdLong());
				}
			}

			for(Role i : volmit.getRoles())
			{
				if(i.getName().equals("Support"))
				{
					log("  Found support role: " + i.getName());
					support = i;
				}

				if(i.getName().equals("Banished"))
				{
					log("  Found banished role: " + i.getName());
					banished = i;
				}
			}

			for(Member i : volmit.getMembers())
			{
				if(i.getUser().isBot())
				{
					User b = i.getUser();

					if(b.getIdLong() == api.getSelfUser().getIdLong())
					{
						myself = b;
						log("  Found myself: " + b.getAsMention());
					}
				}
			}

			for(TextChannel i : this.api.getTextChannels())
			{
				if(i.getName().equals("console"))
				{
					logChannel = i;
					log("  Found text channel for logging.");
				}

				if(i.getName().equals("garbage"))
				{
					garbageChannel = i;
					log("  Found text channel for trash.");
				}

				if(i.getName().equals("events"))
				{
					updateChannel = i;
					log("  Found text channel for updates.");
				}
			}

			log("  Loading Datastore...");

			try
			{
				if(!f.exists())
				{
					log("    Creating data store");
					data.write();
				}

				log("    Reading from data store");
				data.read();
			}

			catch(IOException e1)
			{
				log("    Failed to load datastore: " + e1.getMessage());
			}

			log("  Starting cyclethread");

			new Thread()
			{
				@Override
				public void run()
				{
					while(!interrupted())
					{
						try
						{
							Thread.sleep(1000);
							t++;
						}

						catch(InterruptedException e)
						{

						}

						tick(false);
					}
				}
			}.start();

			log("Apparatus ONLINE!");
			getApi().getPresence().setGame(new Game()
			{
				public String getUrl()
				{
					return "http://volmit.com/";
				}

				public GameType getType()
				{
					return GameType.DEFAULT;
				}

				public String getName()
				{
					return "with Volmit";
				}
			});
		}

		catch(Exception e)
		{
			e.printStackTrace();
			log("Failed to run " + e.getMessage());
		}
	}

	private void tick(boolean sync)
	{
		if(t % 60 == 0 && data.isSaveRequested())
		{
			try
			{
				log("Saving data store");
				data.saveRightFuckingNow();
			}

			catch(Exception e)
			{
				log("Failed to save: " + e.getMessage());
				e.printStackTrace();
			}
		}

		if(t % 9 == 0)
		{
			logChannel.sendTyping().queue();
			updateChannel.sendTyping().queue();
		}

		if(t % 20 == 0 && !middels.isEmpty())
		{
			log("There are " + middels.size() + " unidentified deleted messsages(s) pending...");

			for(long i : data.getMessages().keySet())
			{
				for(long j : middels)
				{
					if(data.getMessages().get(i).getMid() == j)
					{
						EffectiveMessage e = data.getMessages().get(i);
						log("  Found message reference @" + j);
						log("  Resolving Channel @" + e.getCid() + "...");
						TextChannel tc = null;
						User u = null;

						for(TextChannel k : getApi().getTextChannels())
						{
							if(k.getIdLong() == e.getCid())
							{
								log("    Resolved CHANNEL #" + k.getName());
								tc = k;
							}
						}

						if(tc != null)
						{
							log("  Resolving User @" + e.getUid() + "...");
							u = getApi().getUserById(e.getUid());

							if(u != null)
							{
								log("    Resolved USER @" + u.getName());
							}

							else
							{
								log("    Could not resolve CHANNEL");
							}
						}

						else
						{
							log("    Could not resolve CHANNEL");
						}

						if(u != null && tc != null)
						{
							log("  Reading Audit logs for potential non-self delete...");
						}

						EmbedBuilder eb = new EmbedBuilder();
						eb.setTitle(u != null ? "Someone deleted a message by " + u.getName() : "?" + e.getUid() + " deleted a message");
						eb.setDescription(e.getContent());
						eb.addField("Created At", F.stamp(i), true);
						eb.addField("Deleted At", F.stamp(M.ms() - 10000), true);
						eb.setColor(new Color(229, 244, 66));

						for(int k = 0; k < e.getAttachmentUrls().size(); k++)
						{
							eb.addField("Attachment #" + (k + 1), e.getAttachmentUrls().get(k), false);
						}

						MessageEmbed m = eb.build();
						updateChannel.sendMessage(m).queue();
					}
				}
			}

			middels.clear();
		}

		if(t % 20 == 0 && !midrens.isEmpty())
		{
			log("There are " + midrens.size() + " unidentified edited messsages(s) pending...");

			for(long i : data.getMessages().keySet())
			{
				for(long j : midrens.keySet())
				{
					if(data.getMessages().get(i).getMid() == j)
					{
						EffectiveMessage e = data.getMessages().get(i);
						log("  Found message reference @" + j);
						log("  Resolving Channel @" + e.getCid() + "...");
						TextChannel tc = null;
						User u = null;

						for(TextChannel k : getApi().getTextChannels())
						{
							if(k.getIdLong() == e.getCid())
							{
								log("    Resolved CHANNEL #" + k.getName());
								tc = k;
							}
						}

						if(tc != null)
						{
							log("  Resolving User @" + e.getUid() + "...");
							u = getApi().getUserById(e.getUid());

							if(u != null)
							{
								log("    Resolved USER @" + u.getName());
							}

							else
							{
								log("    Could not resolve CHANNEL");
							}
						}

						else
						{
							log("    Could not resolve CHANNEL");
						}

						EmbedBuilder eb = new EmbedBuilder();
						eb.setTitle(u != null ? "Someone edited a message by " + u.getName() : "?" + e.getUid() + " deleted a message");
						eb.setDescription(e.getContent());
						eb.addField("Created At", F.stamp(i), true);
						eb.addField("Edited At", F.stamp(M.ms() - 10000), true);
						eb.addField("Edited To", midrens.get(j), false);
						eb.setColor(new Color(244, 65, 226));

						for(int k = 0; k < e.getAttachmentUrls().size(); k++)
						{
							eb.addField("Attachment #" + (k + 1), e.getAttachmentUrls().get(k), false);
						}

						MessageEmbed m = eb.build();
						updateChannel.sendMessage(m).queue();
					}
				}
			}

			midrens.clear();
		}

		if(t % 5 == 0)
		{
			if(!logs.isEmpty())
			{
				String m = "```";

				for(String i : logs)
				{
					m += "@APS -> " + i + "\n";
				}

				m += "```";
				MessageBuilder mb = new MessageBuilder();
				mb.append(m);

				if(sync)
				{
					logChannel.sendMessage(mb.build()).complete();
				}

				else
				{
					logChannel.sendMessage(mb.build()).queue();
				}

				logs.clear();
			}
		}
	}

	public void onMessageDeleted(final MessageDeleteEvent e)
	{
		log("Message @" + e.getMessageIdLong() + " was deleted from #" + e.getTextChannel().getName());
		middels.add(e.getMessageIdLong());
	}

	public void onMessageRenamed(final MessageUpdateEvent e)
	{
		log("Message @" + e.getMessageIdLong() + " was edited in #" + e.getTextChannel().getName());
		midrens.put(e.getMessageIdLong(), e.getMessage().getContent());
	}

	public void onMessageReceived(final MessageReceivedEvent e)
	{
		String cn = e.getMessage().getContent();

		if(cn.startsWith("/"))
		{
			log("-");
			cn = cn.substring(1).replaceAll("\n", " ");
			log("Reconized /");
			String cmd = "";
			List<String> arg = new ArrayList<String>();

			if(cn.contains(" "))
			{
				log("Command DOES contain args");
				String[] s = cn.split(" ");
				cmd = s[0].trim();

				for(int i = 1; i < s.length; i++)
				{
					log("ARG " + i + ": " + s[i].trim());
					arg.add(s[i].trim());
				}
			}

			else
			{
				cmd = cn;
			}

			log("Command SET: " + cmd);

			for(ICommand i : commands)
			{
				if(i.getName().equalsIgnoreCase(cmd))
				{
					log("Command Found: " + i.getName());
					i.onCommand(e, arg.toArray(new String[arg.size()]));
					e.getMessage().addReaction(SYM.SYMBOL_GEAR + "").queue();
					return;
				}
			}

			for(ICommand i : commands)
			{
				for(String j : i.getAliases())
				{
					if(j.equalsIgnoreCase(cmd))
					{
						log("Command Found: " + i.getName());
						i.onCommand(e, arg.toArray(new String[arg.size()]));
						e.getMessage().addReaction(SYM.SYMBOL_GEAR + "").queue();
						return;
					}
				}
			}
		}

		if(!e.getAuthor().isBot())
		{
			data.logMessage(new EffectiveMessage(e.getMessage()));
		}

		if(e.getMessage().getMentionedUsers().contains(myself))
		{
			log("I was mentioned by " + e.getMessage().getAuthor().getName() + " <3");
			e.getMessage().addReaction(SYM.SYMBOL_HEART + "").queue();
		}
	}

	public void log(String s)
	{
		logs.add(s);
	}

	public void onEvent(Event event)
	{
		if(event instanceof MessageReceivedEvent)
		{
			onMessageReceived((MessageReceivedEvent) event);
		}

		if(event instanceof MessageDeleteEvent)
		{
			onMessageDeleted((MessageDeleteEvent) event);
		}

		if(event instanceof MessageUpdateEvent)
		{
			onMessageRenamed((MessageUpdateEvent) event);
		}
	}

	public AuthSet getAuth()
	{
		return auth;
	}

	public JDA getApi()
	{
		return api;
	}

	public List<ICommand> getCommands()
	{
		return commands;
	}

	public List<String> getLogs()
	{
		return logs;
	}

	public TextChannel getLogChannel()
	{
		return logChannel;
	}

	public static Apparatus getInst()
	{
		return inst;
	}

	public TextChannel getUpdateChannel()
	{
		return updateChannel;
	}

	public static DStore getData()
	{
		return data;
	}

	public static int getT()
	{
		return t;
	}

	public List<Long> getMiddels()
	{
		return middels;
	}

	public User getMyself()
	{
		return myself;
	}

	public Guild getVolmit()
	{
		return volmit;
	}

	public Role getSupport()
	{
		return support;
	}

	public Role getBanished()
	{
		return banished;
	}

	public TextChannel getGarbageChannel()
	{
		return garbageChannel;
	}
}
