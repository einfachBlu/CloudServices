package de.blu.common.network.packet.listener;

import de.blu.common.network.packet.packets.Packet;

public interface PacketListener {

    void onPacketReceived(Packet packet, boolean hadCallback);
}