package dev.sisby.dominoes;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dominoes implements ModInitializer {
	public static final String ID = "dominoes";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[Dominoes] i uhhh prefer the madagascar themed ones actually. with the little car");
	}
}
