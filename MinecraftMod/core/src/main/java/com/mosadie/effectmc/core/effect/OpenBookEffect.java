package com.mosadie.effectmc.core.effect;

import com.google.gson.JsonObject;
import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class OpenBookEffect extends Effect {

    public OpenBookEffect() {
        super();
        getPropertyManager().addCommentProperty("More information on how to configure this on the <a href=\"https://github.com/MoSadie/EffectMC/wiki/open-book\" target=\"_blank\">wiki.</a>");
        getPropertyManager().addBodyProperty("bookJSON", "", true, "Book JSON", "{}");
        getPropertyManager().lock();
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
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        if (!getPropAsString(args, "bookJSON").equalsIgnoreCase("")) {
            JsonObject book = core.fromJson(getPropAsString(args, "bookJSON"));

            if (book == null) {
                core.getExecutor().log("Book invalid");
                return new EffectResult("Invalid Book", EffectResult.Result.ERROR);
            }

            core.getExecutor().log("Opening Book");
            if (core.getExecutor().openBook(book))
                return new EffectResult("Opened Book", EffectResult.Result.SUCCESS);
            else
                return new EffectResult("Failed to open book.", EffectResult.Result.ERROR);

        }

        return new EffectResult("Book JSON not found!", EffectResult.Result.SUCCESS);
    }
}
