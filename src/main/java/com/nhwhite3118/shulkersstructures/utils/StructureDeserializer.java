package com.nhwhite3118.shulkersstructures.utils;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.nhwhite3118.shulkerssupersimplestructuresystem.ShulkersSuperSimpleStructureSystem;
import com.nhwhite3118.structures.simplestructure.SimpleStructure;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.StructurePiece;

public class StructureDeserializer {
    /**
     * Deserialise the JSON file for a structure
     */
    private SimpleStructure deserialiseStructure(Reader input) throws JsonSyntaxException {
        JsonParser parser = new JsonParser();
        JsonElement entireFile = parser.parse(input);
        if (!entireFile.isJsonObject())
            throw new JsonSyntaxException("Malformed file");
        JsonObject breedVariantData = entireFile.getAsJsonObject();
        return deserializeTags(entireFile.getAsJsonObject());
    }

    private SimpleStructure deserializeTags(JsonObject jsonObject) {
        if (!jsonObject.isJsonObject()) {
            ShulkersSuperSimpleStructureSystem.LOGGER.error("Malformed json files found. Try parsing your structure jsons through a json syntax checker");
            return null;
        }
        JsonElement structureNameJSON = jsonObject.get("structureName");
        if (structureNameJSON == null) {
            ShulkersSuperSimpleStructureSystem.LOGGER.error("Didn't find the required field structureName in one of your structure json files");
            return null;
        }
        if (!structureNameJSON.isJsonPrimitive() || !structureNameJSON.getAsJsonPrimitive().isString()) {
            ShulkersSuperSimpleStructureSystem.LOGGER.error("The required field structureName has the wrong type (should be a String).");
            return null;
        }
        String structureName = structureNameJSON.getAsString();
        ArrayList<StructurePiece> structurePieces = new ArrayList<StructurePiece>();
        int seed = 0;
        int spawnRate = 0;
        ResourceLocation nbtLocation = null;
        int yOffset = 0;
        List<Biome> biomes = new ArrayList<Biome>();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String elementName = entry.getKey();
            switch (elementName) {
            case "piece":
                StructurePiece piece = deserializePiece(entry.getValue().getAsJsonObject());
                structurePieces.add(piece);
                break;
            case "seed":
                if (!isNumber(entry)) {
                    ShulkersSuperSimpleStructureSystem.LOGGER.error("The required field seed has the wrong type (should be a whole number).");
                    break;
                }
                seed = entry.getValue().getAsInt();
                break;
            case "spawnRate":
                if (!isNumber(entry)) {
                    ShulkersSuperSimpleStructureSystem.LOGGER.error("The required field spawnRate has the wrong type (should be a whole number).");
                    break;
                }
                spawnRate = entry.getValue().getAsInt();
                break;
            case "fileName":
                if (!isString(entry)) {
                    ShulkersSuperSimpleStructureSystem.LOGGER.error("The required field fileName has the wrong type (should be a String).");
                    break;
                }
                nbtLocation = new ResourceLocation(ShulkersSuperSimpleStructureSystem.MODID, entry.getValue().getAsString());
                break;
            case "blockLevelsBelowGround":
                if (!isNumber(entry)) {
                    ShulkersSuperSimpleStructureSystem.LOGGER.error("The field blockLevelsBelowGround has the wrong type (should be a whole number).");
                    break;
                }
                yOffset = -entry.getValue().getAsInt();
                break;
            case "biomes":
                biomes = deserializeBiomes(entry.getValue().getAsJsonArray());
            default:
            }
        }
        SimpleStructure result = new SimpleStructure(NoFeatureConfig.field_236558_a_, spawnRate, nbtLocation, yOffset, seed, structureName,
                (Biome[]) biomes.toArray(), new Category[] {}, Decoration.SURFACE_STRUCTURES);
        return result;
    }

    private boolean isNumber(Map.Entry<String, JsonElement> element) {
        return element.getValue().isJsonPrimitive() && element.getValue().getAsJsonPrimitive().isNumber();
    }

    private boolean isString(Map.Entry<String, JsonElement> element) {
        return element.getValue().isJsonPrimitive() && element.getValue().getAsJsonPrimitive().isString();
    }

    private StructurePiece deserializePiece(JsonObject jsonObject) {
        throw new NotImplementedException("Multiple piece structures not implemented yet");
    }

    private List<Biome> deserializeBiomes(JsonArray biomeArray) {
        List<Biome> biomes = new ArrayList<Biome>();
        for (JsonElement entry : biomeArray) {
            String biomeName = entry.getAsString();
            ResourceLocation biomeResourceLocation = new ResourceLocation(biomeName);
            if (Registry.BIOME.containsKey(biomeResourceLocation)) {
                Optional<Biome> biome = Registry.BIOME.getValue(biomeResourceLocation);
                if (biome.isPresent()) {
                    biomes.add(biome.get());
                }
            }
        }
        return biomes;
    }
}
