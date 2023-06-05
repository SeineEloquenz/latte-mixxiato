package edu.kit.tm.ps.latte_mixxiato.lib.endpoint.packet;

import com.robertsoultanaev.javasphinx.SerializationUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public record Packet(UUID uuid, UUID bucketId, int packetsInMessage, int sequenceNumber, byte[] payload) {

    public static final int HEADER_SIZE = 40;

    public static Packet parse(byte[] message) {
        byte[] headerBytes = Arrays.copyOfRange(message, 0, HEADER_SIZE);
        ByteBuffer byteBuffer = ByteBuffer.wrap(headerBytes);
        long uuidHigh = byteBuffer.getLong();
        long uuidLow = byteBuffer.getLong();

        long bucketIdHigh = byteBuffer.getLong();
        long bucketIdLow = byteBuffer.getLong();

        int packetsInMessage = byteBuffer.getInt();
        int sequenceNumber = byteBuffer.getInt();
        final var uuid = new UUID(uuidHigh, uuidLow);
        final var bucketId = new UUID(bucketIdHigh, bucketIdLow);
        byte[] payload = Arrays.copyOfRange(message, HEADER_SIZE, message.length);

        return new Packet(uuid, bucketId, packetsInMessage, sequenceNumber, payload);
    }

    private ByteBuffer headerBuffer() {
        final var packetHeader = ByteBuffer.allocate(Packet.HEADER_SIZE);
        packetHeader.putLong(uuid.getMostSignificantBits());
        packetHeader.putLong(uuid.getLeastSignificantBits());
        packetHeader.putLong(bucketId.getMostSignificantBits());
        packetHeader.putLong(bucketId.getLeastSignificantBits());
        packetHeader.putInt(packetsInMessage);
        packetHeader.putInt(sequenceNumber);
        return packetHeader;
    }

    public byte[] toBytes() {
        return SerializationUtils.concatenate(headerBuffer().array(), payload);
    }
}