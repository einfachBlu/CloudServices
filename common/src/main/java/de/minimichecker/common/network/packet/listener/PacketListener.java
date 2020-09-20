package de.minimichecker.common.network.packet.listener;

import de.minimichecker.common.network.packet.packets.Packet;

public interface PacketListener {

    void onPacketReceived(Packet packet, boolean hadCallback);
}