package com.mosadie.effectmc.core.export;

import com.google.gson.*;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.lang.reflect.Type;
import java.util.List;

public class EffectListSerializer implements JsonSerializer<List<Effect>> {
    @Override
    public JsonElement serialize(List<Effect> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        for (Effect effect : src) {
            array.add(context.serialize(effect));
        }
        return array;
    }
}
