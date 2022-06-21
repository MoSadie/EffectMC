package com.mosadie.effectmc.core.handler;

import com.google.gson.JsonObject;
import com.mosadie.effectmc.core.EffectMCCore;

public class OpenBookHandler extends EffectRequestHandler {

    public OpenBookHandler(EffectMCCore core) {
        super(core);
        addCommentProperty("More information on how to configure this on the <a href=\"https://github.com/MoSadie/EffectMC/wiki/open-book\" target=\"_blank\">wiki.</a>");
        addBodyProperty("bookJSON", "", true, "Book JSON", "{}");
    }

    @Override
    public String getEffectName() {
        return "Open Book";
    }

    @Override
    public String getEffectTooltip() {
        return "Open any book.";
    }

    @Override
    EffectResult execute() {
        if (!getProperty("bookJSON").getAsString().equalsIgnoreCase("")) {
            JsonObject book = core.fromJson(getProperty("bookJSON").getAsString());

            if (book == null) {
                core.getExecutor().log("Book invalid");
                return new EffectResult("Invalid Book", false);
            }

            core.getExecutor().log("Opening Book");
            if (core.getExecutor().openBook(book))
                return new EffectResult("Opened Book", true);
            else
                return new EffectResult("Failed to open book.", false);

        }

        return new EffectResult("Book JSON not found!", false);
    }
}
