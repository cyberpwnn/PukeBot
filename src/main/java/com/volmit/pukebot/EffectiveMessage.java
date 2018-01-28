package com.volmit.pukebot;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.User;

public class EffectiveMessage
{
	private String content;
	private long uid;
	private long cid;
	private long mid;
	private List<String> attachmentUrls;

	public EffectiveMessage()
	{

	}

	public EffectiveMessage(Message m)
	{
		User u = m.getAuthor();
		content = m.getContent();
		uid = u.getIdLong();
		cid = m.getChannel().getIdLong();
		mid = m.getIdLong();
		attachmentUrls = new ArrayList<String>();

		for(Attachment i : m.getAttachments())
		{
			attachmentUrls.add(i.getUrl());
		}
	}

	public String getContent()
	{
		return content;
	}

	public long getUid()
	{
		return uid;
	}

	public long getCid()
	{
		return cid;
	}

	public long getMid()
	{
		return mid;
	}

	public List<String> getAttachmentUrls()
	{
		return attachmentUrls;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public void setUid(long uid)
	{
		this.uid = uid;
	}

	public void setCid(long cid)
	{
		this.cid = cid;
	}

	public void setMid(long mid)
	{
		this.mid = mid;
	}

	public void setAttachmentUrls(List<String> attachmentUrls)
	{
		this.attachmentUrls = attachmentUrls;
	}
}
