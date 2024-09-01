package com.mosadie.effectmc.core.export;

import com.google.gson.*;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.lang.reflect.Type;

public class EffectSerializer implements JsonSerializer<Effect> {

    @Override
    public JsonElement serialize(Effect src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("id", src.getEffectId());
        object.addProperty("name", src.getEffectName());
        object.addProperty("tooltip", src.getEffectTooltip());

        // Serialize the property manager as normal
        object.add("properties", context.serialize(src.getPropertyManager().getPropertiesList()));

        System.out.println("Effect serialized: " + src.getEffectName());

        return object;
    }
}
