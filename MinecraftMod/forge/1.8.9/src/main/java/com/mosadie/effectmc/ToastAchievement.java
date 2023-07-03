package com.mosadie.effectmc;

import net.minecraft.init.Blocks;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class ToastAchievement extends Achievement {
	
	String title;
	String description;

	public ToastAchievement(String title, String description) {
		super(title, description, 0, 0, Blocks.air, null);
		this.title = title;
		this.description = title + " " + description;
	}

	@Override
	public IChatComponent getStatName() {
		return new ChatComponentText(description);
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	

}
