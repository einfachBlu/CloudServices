package de.blu.connector.common.provider;

import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import lombok.Getter;
import lombok.Setter;

@Singleton
@Getter
@Setter
public final class SelfGameServerInformationProvider {

    private GameServerInformation gameServerInformation;
}
