package org.squiddev.studio.modifications.lua;

import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.studio.api.Module;
import org.squiddev.studio.api.Transformer;
import org.squiddev.studio.api.lua.ILuaAPI;
import org.squiddev.studio.api.lua.ILuaAPIFactory;
import org.squiddev.studio.api.lua.ILuaEnvironment;
import org.squiddev.studio.modifications.Config;
import org.squiddev.studio.modifications.lua.socket.SocketAPI;

public class ApiModule implements Module {
	@Override
	public void setupTransformer(Transformer transformer) {
	}

	@Override
	public void setupLua(ILuaEnvironment environment) {
		environment.registerAPI(new ILuaAPIFactory() {
			@Override
			public ILuaAPI create(IComputerAccess computer) {
				return Config.APIs.Socket.enabled ? new SocketAPI() : null;
			}

			@Override
			public String[] getNames() {
				return new String[]{"socket"};
			}
		});

		environment.registerAPI(new DataAPI());
	}
}
