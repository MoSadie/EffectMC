package com.mosadie.effectmc.plugingen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.handler.EffectRequestHandler;
import com.mosadie.effectmc.core.handler.RootHandler;
import com.mosadie.effectmc.core.property.EffectProperty;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringSubstitutor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreamDeckPluginGen {

    public static void main(String[] args) throws IOException, URISyntaxException {
        EffectMCCore core = new EffectMCCore(null, null, null);
        System.out.println("EffectMC Core Effect Count: " + core.getEffects().size());
        File currDir = new File(".");
        System.out.println("CWD: " + currDir.getAbsolutePath());

        // Create Plugin Folder
        System.out.println("Creating plugin dir");
        File pluginDir = new File(currDir, "com.mosadie.effectmc.sdPlugin");

        if (pluginDir.exists()) {
            System.out.println("Deleting existing plugin dir");
            FileUtils.deleteDirectory(pluginDir);
        }

        if (!pluginDir.mkdir()) {
            throw new IOException("Failed to create plugin folder!");
        }

        // Write Static Resources
        writeStaticResources(pluginDir);

        // Write manifest
        writeManifest(pluginDir, core.getEffects());
        
        // Write app.js
        writeAppJS(pluginDir, core.getEffects());

        // Write Property Inspectors
        writePI(pluginDir, core.getEffects());
    }

    private static void writePI(File pluginDir, List<EffectRequestHandler> effects) throws URISyntaxException, IOException {
        System.out.println("Generating and Writing Property Inspectors");
        File templateDir = new File(StreamDeckPluginGen.class.getClassLoader().getResource("template").toURI());

        String piTemplate = FileUtils.readFileToString(new File(templateDir, "pi.html.template"), StandardCharsets.UTF_8);

        for(EffectRequestHandler effect : effects) {
            System.out.println("Generating Property Inspector for " + effect.getEffectName());
            Map<String, String> replaceMap = new HashMap<>();
            StringBuilder propertiesBuilder = new StringBuilder();

            for (String key : effect.getProperties().keySet()) {
                propertiesBuilder.append(effect.getProperty(key).getSDHTMLInput()).append("\n");
            }

            replaceMap.put("properties", propertiesBuilder.toString());
            StringSubstitutor piStringSub = new StringSubstitutor(replaceMap);

            System.out.println("Writing Property Inspector for " + effect.getEffectName());
            FileUtils.write(new File(pluginDir, "propertyinspector/" + effect.getEffectSlug() + "_pi.html"), piStringSub.replace(piTemplate), Charset.defaultCharset());
        }
    }

    private static void writeAppJS(File pluginDir, List<EffectRequestHandler> effects) throws URISyntaxException, IOException {
        System.out.println("Generating app.js");
        File templateDir = new File(StreamDeckPluginGen.class.getClassLoader().getResource("template").toURI());
        Map<String, String> replaceMap = new HashMap<>();

        // Create "actions" and "onConnect" replace string
        StringBuilder actionsStringBuilder = new StringBuilder();
        StringBuilder onConnectBuilder = new StringBuilder();

        // Load the strings to replace
        String appJsTemplate = FileUtils.readFileToString(new File(templateDir, "app.js.main.template"), StandardCharsets.UTF_8);
        String actionStringTemplate = FileUtils.readFileToString(new File(templateDir, "app.js.action.template"), StandardCharsets.UTF_8);

        for (EffectRequestHandler effect : effects) {
            System.out.println("Generating app.js section for " + effect.getEffectName());
            Map<String, EffectProperty> props = effect.getProperties();
            Map<String, String> actionReplaceMap = new HashMap<>();
            actionReplaceMap.put("constName", effect.getEffectSlug());
            actionReplaceMap.put("lowercase", effect.getEffectSlug().toLowerCase());
            actionReplaceMap.put("slug", effect.getEffectSlug());

            onConnectBuilder.append(actionReplaceMap.get("constName")).append(".onConnect(jsn);\n");

            // Create "settingsDefault", "settingsIfCheck", and "setParams" replacement.
            StringBuilder settingsDefaultsBuilder = new StringBuilder();
            StringBuilder settingsIfCheckBuilder = new StringBuilder();
            StringBuilder setParamsBuilder = new StringBuilder();
            boolean bodyProp = false;
            for (String key : props.keySet()) {
                EffectProperty prop = effect.getProperty(key);
                if (prop.getPropType().equals(EffectProperty.PropertyType.BODY)) {
                    bodyProp = true;
                }
                if (prop.getPropType() != EffectProperty.PropertyType.COMMENT) {
                    settingsDefaultsBuilder.append("this.settings.").append(key).append("= '").append(prop.getAsString()).append("';\n");

                    if (prop.isRequired())
                        settingsIfCheckBuilder.append("if (!jsn.payload.settings.").append(key).append(") { $SD.api.showAlert(jsn.context); console.log('Missing ").append(key).append("'); return }\n");

                    if (prop.getPropType() != EffectProperty.PropertyType.BODY)
                        setParamsBuilder.append("url.searchParams.set('").append(key).append("', (jsn.payload.settings.").append(key).append(" ? jsn.payload.settings.").append(key).append(" : ''));\n");
                }
            }
            actionReplaceMap.put("settingsDefaults", settingsDefaultsBuilder.toString());
            actionReplaceMap.put("setParams", setParamsBuilder.toString());
            actionReplaceMap.put("settingsIfCheck", settingsIfCheckBuilder.toString());

            // Create "sendRequest" replacement.
            if (!bodyProp) {
                actionReplaceMap.put("sendRequest", "fetch(url).then(response => {\n" +
                        "            console.log(\"DEBUG yay\", response);\n" +
                        "            if (response.status == 200) $SD.api.showOk(jsn.context);\n" +
                        "            else {\n" +
                        "                console.log(\"Request Failed\", response);\n" +
                        "                $SD.api.showAlert(jsn.context);\n" +
                        "            }\n" +
                        "        }, reason => {\n" +
                        "            console.log(\"DEBUG fail\", reason);\n" +
                        "            $SD.api.showAlert(jsn.context);\n" +
                        "        });");
            } else {
                StringBuilder sendRequestBuilder = new StringBuilder();
                sendRequestBuilder.append("fetch(url, {method: 'POST', body: encodeURIComponent(jsn.payload.settings ? jsn.payload.settings : '')}).then(response => {\n" +
                        "            console.log(\"DEBUG yay\", response);\n" +
                        "            if (response.status == 200) $SD.api.showOk(jsn.context);\n" +
                        "            else {\n" +
                        "                console.log(\"Request Failed\", response);\n" +
                        "                $SD.api.showAlert(jsn.context);\n" +
                        "            }\n" +
                        "        }, reason => {\n" +
                        "            console.log(\"DEBUG fail\", reason);\n" +
                        "            $SD.api.showAlert(jsn.context);\n" +
                        "        });");
                actionReplaceMap.put("sendRequest", sendRequestBuilder.toString());
            }

            StringSubstitutor stringSub = new StringSubstitutor(actionReplaceMap);
            actionsStringBuilder.append(stringSub.replace(actionStringTemplate));

        }

        replaceMap.put("onConnect", onConnectBuilder.toString());
        replaceMap.put("actions", actionsStringBuilder.toString());

        StringSubstitutor appJsStringSub = new StringSubstitutor(replaceMap);
        String appJs = appJsStringSub.replace(appJsTemplate);

        System.out.println("Writing app.js");
        FileUtils.write(new File(pluginDir, "app.js"), appJs, Charset.defaultCharset());
    }

    private static void writeManifest(File pluginDir, List<EffectRequestHandler> effects) throws IOException {
        System.out.println("Generating Manifest");
        Manifest manifest = new Manifest();
        manifest.SDKVersion = 2;
        manifest.Author = "MoSadie";
        manifest.CodePath = "index.html";
        manifest.PropertyInspectorPath = "propertyinspector/index.html";
        manifest.Description = "Trigger effects in Minecraft. Minecraft mod required.";
        manifest.Name = "EffectMC";
        manifest.Category = "EffectMC";
        manifest.CategoryIcon = "images/joinserver/actionImage";
        manifest.Icon = "images/joinserver/keyIcon";
        manifest.URL = "https://github.com/MoSadie/EffectMC";
        manifest.Version = RootHandler.class.getPackage().getImplementationVersion();

        Manifest.OS windows = new Manifest.OS();
        windows.Platform = "windows";
        windows.MinimumVersion = "10";

        Manifest.OS mac = new Manifest.OS();
        mac.Platform = "mac";
        mac.MinimumVersion = "10.11";

        manifest.OS = new Manifest.OS[] {mac, windows};

        Manifest.Software software = new Manifest.Software();
        software.MinimumVersion = "4.1";
        manifest.Software = software;

        List<Manifest.Action> actionList = new ArrayList<>();

        for (EffectRequestHandler effect : effects) {
            Manifest.Action action = new Manifest.Action();

            action.Icon = "images/" + effect.getEffectSlug() + "/actionImage";
            action.Name = effect.getEffectName();
            action.States = new Manifest.Action.State[] { new Manifest.Action.State("images/" + effect.getEffectSlug() + "/keyIcon")};
            action.Tooltip = effect.getEffectTooltip();
            action.UUID = "com.mosadie.effectmc." + effect.getEffectSlug();
            action.PropertyInspectorPath = "propertyinspector/" + effect.getEffectSlug() + "_pi.html";

            actionList.add(action);
        }

        manifest.Actions = actionList.toArray(new Manifest.Action[0]);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(manifest);

        System.out.println("Writing manifest");
        FileUtils.write(new File(pluginDir, "manifest.json"), json, Charset.defaultCharset());
    }

    private static void writeStaticResources(File pluginDir) throws URISyntaxException, IOException {
        System.out.println("Copying static resources");
        File staticDir = new File(StreamDeckPluginGen.class.getClassLoader().getResource("static").toURI());

        FileUtils.copyDirectory(staticDir, pluginDir, new ExcludePDNFiles());
    }

    private static class ExcludePDNFiles implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            return !pathname.getAbsolutePath().endsWith(".pdn");
        }
    }

    static class Manifest {
        Action[] Actions;
        int SDKVersion;
        String Author;
        String CodePath;
        String PropertyInspectorPath;
        String Description;
        String Name;
        String Category;
        String CategoryIcon;
        String Icon;
        String URL;
        String Version;
        OS[] OS;
        Software Software;

        static class Action {
            String Icon;
            String Name;
            State[] States;
            String Tooltip;
            String UUID;
            String PropertyInspectorPath;

            static class State {
                String Image;

                public State(String image) {
                    this.Image = image;
                }
            }
        }

        static class OS {
            String Platform;
            String MinimumVersion;
        }

        static class Software {
            String MinimumVersion;
        }
    }
}
