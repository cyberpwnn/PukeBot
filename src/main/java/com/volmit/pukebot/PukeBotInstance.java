package com.volmit.pukebot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class PukeBotInstance extends ListenerAdapter
{
	private AuthSet auth;
	private JDA api;
	private int mt = 600;
	private List<Role> rgb;
	
	public PukeBotInstance(String[] puke) throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException
	{
		auth = new AuthSet(puke);
		rgb = new ArrayList<Role>();
		api = new JDABuilder(AccountType.BOT).setToken(auth.getToken()).addEventListener(this).buildBlocking();
		
		new Thread(new Runnable()
		{
			public void run()
			{
				float h = 1f;
				float s = 1f;
				float b = 1f;
				
				while(!Thread.interrupted())
				{
					Color color = Color.getHSBColor(h, s, b);
					
					h += 0.016;
					
					if(h > 1f)
					{
						h = 0f;
					}
					
					for(Role i : rgb)
					{
						i.getManager().setColor(color).complete();
					}
					
					try
					{
						Thread.sleep(200);
					}
					
					catch(InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	@Override
	public void onMessageReceived(final MessageReceivedEvent e)
	{
		if(e.getChannel().getName().startsWith("discussion-") && !e.getAuthor().isBot())
		{
			e.getTextChannel().getManager().setTopic("Will close in t:" + mt).complete();
		}
		
		if(e.getMessage().getContent().toLowerCase().startsWith("!rgb on"))
		{
			for(Role i : e.getMessage().getMentionedRoles())
			{
				if(!rgb.contains(i))
				{
					rgb.add(i);
					e.getTextChannel().sendMessage("RGB Payload enabled for " + i.getAsMention()).complete();
				}
			}
		}
		
		if(e.getMessage().getContent().toLowerCase().startsWith("!rgb off"))
		{
			for(Role i : e.getMessage().getMentionedRoles())
			{
				if(rgb.contains(i))
				{
					rgb.remove(i);
					e.getTextChannel().sendMessage("RGB Payload disabled for " + i.getAsMention()).complete();
				}
			}
		}
		
		if(e.getMessage().getContent().toLowerCase().startsWith("!sandwich"))
		{
			e.getMessage().addReaction(SYM.SYMBOL_HEART + "").complete();
		}
		
		if(e.getMessage().getContent().toLowerCase().startsWith("!close"))
		{
			if(!e.getGuild().getMember(e.getAuthor()).hasPermission(Permission.MANAGE_CHANNEL))
			{
				e.getTextChannel().sendMessage("No.").complete();
				return;
			}
			
			if(e.getChannel().getName().startsWith("discussion-"))
			{
				e.getChannel().sendMessage("Closing Discussion").complete();
				
				new Thread(new Runnable()
				{
					public void run()
					{
						try
						{
							Thread.sleep(1000);
						}
						
						catch(InterruptedException e)
						{
							e.printStackTrace();
						}
						
						e.getTextChannel().delete().complete();
					}
				}).start();
			}
		}
		
		if(e.getMessage().getContent().toLowerCase().startsWith("!1v1"))
		{
			if(!e.getGuild().getMember(e.getAuthor()).hasPermission(Permission.MANAGE_CHANNEL))
			{
				e.getTextChannel().sendMessage("No.").complete();
				return;
			}
			
			String name = "discussion-" + UUID.randomUUID().toString().split("-")[2];
			e.getChannel().sendMessage("Created Channel **#" + name + "**").complete();
			final TextChannel c = (TextChannel) e.getGuild().getController().createTextChannel(name).complete();
			c.getManager().setTopic("Will close in t:" + mt).complete();
			
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						Message om = null;
						
						while(!Thread.interrupted())
						{
							String t = c.getTopic();
							
							if(t != null && t.contains(":"))
							{
								int tl = Integer.valueOf(t.split(":")[1]);
								tl--;
								
								System.out.println(tl);
								
								if(tl > 30)
								{
									if(om != null)
									{
										om.delete().complete();
									}
									
									om = null;
								}
								
								c.getManager().setTopic("Will close in t:" + tl).complete();
								
								if(tl == 30)
								{
									om = c.sendMessage("Warning: Channel will close in 30 seconds.").complete();
								}
								
								if(tl <= 30 && tl > 0 && om != null)
								{
									om.editMessage("Warning: Channel will close in **" + tl + "** seconds.").complete();
								}
								
								if(tl == 0)
								{
									c.delete().complete();
									return;
								}
							}
							
							Thread.sleep(500);
						}
					}
					
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}).start();
			
			for(Member i : e.getGuild().getMembers())
			{
				if(!e.getMessage().isMentioned(i.getUser()) && !i.getUser().equals(e.getAuthor()))
				{
					c.createPermissionOverride(i).complete().getManager().deny(Permission.ALL_CHANNEL_PERMISSIONS).complete();
				}
			}
		}
	}
}
