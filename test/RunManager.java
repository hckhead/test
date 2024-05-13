package com.lgcns.test;

import com.lgcns.test.server.EngineServer;

public class RunManager {

	public static void main(String[] args) throws Exception {

		VariableManager.load();

		StateManager.load();

		EngineServer engineServer = new EngineServer();
		engineServer.start();
	}
}
