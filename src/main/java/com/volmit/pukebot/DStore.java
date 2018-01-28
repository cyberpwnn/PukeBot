package com.volmit.pukebot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DStore
{
	private File dstore;
	private int lastTicket;
	private boolean save;
	private Map<Long, EffectiveMessage> messages;

	public DStore(File f)
	{
		messages = new HashMap<Long, EffectiveMessage>();
		dstore = f;
		lastTicket = 1;
		save = false;
	}

	public void logMessage(EffectiveMessage m)
	{
		messages.put(M.ms(), m);
		requestSave();
	}

	public void requestSave()
	{
		save = true;
	}

	public void saveRightFuckingNow() throws IOException
	{
		save = false;
		write();
	}

	public int getLastTicket()
	{
		return lastTicket;
	}

	public void setLastTicket(int lastTicket)
	{
		this.lastTicket = lastTicket;
		requestSave();
	}

	public boolean isSaveRequested()
	{
		return save;
	}

	public void write() throws IOException
	{
		int wri = 0;
		FileOutputStream fos = new FileOutputStream(dstore);
		GZIPOutputStream gzo = new GZIPOutputStream(fos);
		DataOutputStream dos = new DataOutputStream(gzo);
		dos.writeInt(lastTicket);
		dos.writeInt(messages.size());

		for(long i : messages.keySet())
		{
			EffectiveMessage e = messages.get(i);
			dos.writeLong(i);
			dos.writeLong(e.getUid());
			dos.writeLong(e.getCid());
			dos.writeLong(e.getMid());
			dos.writeUTF(e.getContent());
			dos.writeInt(e.getAttachmentUrls().size());

			for(String j : e.getAttachmentUrls())
			{
				dos.writeUTF(j);
			}

			wri++;
		}

		Apparatus.inst.log("Wrote " + wri + " logged messages");

		dos.close();
		Apparatus.inst.log("DBS: " + F.fileSize(dstore.length()));
	}

	public void read() throws IOException
	{
		int pru = 0;
		int red = 0;
		messages.clear();
		FileInputStream fin = new FileInputStream(dstore);
		GZIPInputStream gzi = new GZIPInputStream(fin);
		DataInputStream din = new DataInputStream(gzi);
		lastTicket = din.readInt();
		int msgs = din.readInt();

		for(int i = 0; i < msgs; i++)
		{
			EffectiveMessage e = new EffectiveMessage();
			long time = din.readLong();
			long uid = din.readLong();
			long cid = din.readLong();
			long mid = din.readLong();
			String content = din.readUTF();
			int atts = din.readInt();
			List<String> at = new ArrayList<String>();

			for(int j = 0; j < atts; j++)
			{
				at.add(din.readUTF());
			}

			e.setUid(uid);
			e.setCid(cid);
			e.setMid(mid);
			e.setContent(content);
			e.setAttachmentUrls(at);

			if(M.ms() - time > TimeUnit.DAYS.toMillis(7))
			{
				pru++;
				continue;
			}

			red++;
			messages.put(time, e);
		}

		Apparatus.inst.log("Read " + red + " logged messages (pruned & ignored " + pru + ")");

		din.close();
	}

	public File getDstore()
	{
		return dstore;
	}

	public boolean isSave()
	{
		return save;
	}

	public Map<Long, EffectiveMessage> getMessages()
	{
		return messages;
	}
}
