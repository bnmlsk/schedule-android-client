package io.packet.client;

import java.io.IOException;

import io.packet.ClientPacket;

public class CreateTaskPacket extends ClientPacket {
	public CreateTaskPacket(Integer tableId, Long time, String name, String description, String startDate, String endDate, String startTime, String endTime) throws IOException {
		super(ClientPacket.Type.CREATE_TASK);
		this.writeInt(tableId);
		this.writeLong(time);
		this.writeString(name);
		this.writeString(description);
		this.writeFixedString(startDate);
		this.writeFixedString(endDate);
		this.writeFixedString(startTime);
		this.writeFixedString(endTime);
	}
}