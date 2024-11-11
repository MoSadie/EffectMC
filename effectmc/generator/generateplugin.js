// Steps to generating the plugin
// 1 - Generate the manifest
// 2 - Generate the action ts file for each effect
// 3 - Generate the plugin ts file
// 4 - Generate the pi for each effect

import { readFileSync, existsSync, mkdirSync, writeFileSync, rmSync } from 'fs';

// Read the list of Effects from the effects.json file

const effects = JSON.parse(readFileSync('effects.json', 'utf8'));

const pluginFolder = 'com.mosadie.effectmc.sdPlugin';
const srcFolder = 'src';

// Create/empty the src folder
if (!existsSync(srcFolder)) {
    mkdirSync(srcFolder);
} else {
    rmSync(srcFolder, { recursive: true });
    mkdirSync(srcFolder);
}

// Generate the manifest
const manifest = {
    Name: 'EffectMC',
    Version: '3.1.0.0',
    Author: 'MoSadie',
    Actions: effects.map((effect) => ({
        Name: effect.name,
        UUID: `com.mosadie.effectmc.${effect.id}`,
        Icon: `imgs/actions/${effect.id}/actionImage`,
        Tooltip: effect.tooltip,
        PropertyInspectorPath: `ui/${effect.id}.html`,
        Controllers: ['Keypad'],
        States: [
            {
                Image: `imgs/actions/${effect.id}/keyIcon`,
                TitleAlignment: 'middle',
            },
        ],
    })),
    Category: 'EffectMC',
    CategoryIcon: 'imgs/plugin/category-icon',
    CodePath: 'bin/plugin.js',
    Description: 'Trigger effects in Minecraft. Minecraft mod required.',
    Icon: 'imgs/plugin/marketplace',
    SDKVersion: 2,
    Software: {
        MinimumVersion: '6.5',
    },
    OS: [
        {
            Platform: 'mac',
            MinimumVersion: '10.15',
        },
        {
            Platform: 'windows',
            MinimumVersion: '10',
        },
    ],
    Nodejs: {
        Version: '20',
        Debug: 'enabled',
    },
    UUID: 'com.mosadie.effectmc',
};

// Write manifest to the plugin folder (this folder contains contents already, so no need to empty it)
if (!existsSync(`${pluginFolder}`)) {
    mkdirSync(`${pluginFolder}`);
}

writeFileSync(`${pluginFolder}/manifest.json`, JSON.stringify(manifest, null, 4));

// Generate the action ts files using the template
const actionTemplate = readFileSync('generator/templates/effect-action.ts.template', 'utf8');

// Create the actions folder
if (!existsSync(`${srcFolder}/actions`)) {
    mkdirSync(`${srcFolder}/actions`);
}

effects.forEach((effect) => {
    let action = actionTemplate
        .replace(/{{effectId}}/g, effect.id)
        .replace(/{{effectName}}/g, effect.name)

    const properties = effect.properties.map((property) => {
        if (property.TYPE !== "COMMENT") {
        return `    ${property.id}?: ${property.sdPropType === 'string' || property.sdPropType === 'number' ? 'string' : property.sdPropType};`;
        } else {
            return `    // ${property.comment}`;
        }
    });

    action = action.replace('{{properties}}', properties.join('\n'));

    const defaultProperties = effect.properties.map((property) => {
        if (property.TYPE !== "COMMENT") {
            //return `        if(ev.payload.settings.${property.id} == undefined) { ev.payload.settings.${property.id} = ${typeof property.defaultValue === 'string' ? `"${property.defaultValue}"` : property.defaultValue}; }`;
            return `            ev.payload.settings.${property.id} = ${typeof property.defaultValue === 'string' || typeof property.defaultValue === 'number' ? `"${property.defaultValue}"` : property.defaultValue};`;
        } else {
            return `        // ${property.comment}`;
        }
    });

    action = action.replace('{{defaults}}', defaultProperties.join('\n'));


    writeFileSync(`${srcFolder}/actions/${effect.id}.ts`, action);
});

// Generate the plugin ts file using the template

const pluginTemplate = readFileSync('generator/templates/plugin.ts.template', 'utf8');

const imports = effects.map((effect) => {
    return `import { Effect${effect.id} } from "./actions/${effect.id}";`;
});

const registerActions = effects.map((effect) => {
    return `streamDeck.actions.registerAction(new Effect${effect.id}());`;
});

const plugin = pluginTemplate
    .replace('{{import}}', imports.join('\n'))
    .replace('{{register}}', registerActions.join('\n'));

writeFileSync(`${srcFolder}/plugin.ts`, plugin);

// Generate the pi for each effect using the template

const piTemplate = readFileSync('generator/templates/effect-pi.html.template', 'utf8');

// Create the ui folder
if (!existsSync(`${pluginFolder}/ui`)) {
    mkdirSync(`${pluginFolder}/ui`);
}

effects.forEach((effect) => {
    let pi = piTemplate
        .replace(/{{effectId}}/g, effect.id)
        .replace(/{{effectName}}/g, effect.name);

    const properties = effect.properties.map((property) => {
        switch (property.TYPE) {
            case 'STRING':
            case 'INTEGER':
            case 'DOUBLE':
                return `<sdpi-item label="${property.label}"><sdpi-textfield setting="${property.id}" placeholder="${property.placeholder}"${property.required ? " required" : ""}></sdpi-textfield></sdpi-item>`;

            case 'FLOAT':
                return `<sdpi-item label="${property.label}"><sdpi-textfield setting="${property.id}" placeholder="${property.defaultValue}"${property.required ? " required" : ""}></sdpi-textfield></sdpi-item>`;
            
            case 'BODY':
                return `<sdpi-item label="${property.label}"><sdpi-textarea setting="${property.id}" placeholder="${property.placeholder}"></sdpi-textarea></sdpi-item>`;

            case 'BOOLEAN':
                return `<sdpi-item label="${property.label}"><sdpi-checkbox setting="${property.id}"></sdpi-checkbox></sdpi-item>`;

            case 'SELECTION':
                return `<sdpi-item label="${property.label}"><sdpi-select setting="${property.id}">${property.options.map((option) => `<option value="${option}">${option}</option>`).join('\n')}</sdpi-select></sdpi-item>`;

            case 'COMMENT':
                return `<sdpi-item>${property.comment}</sdpi-item>`;
        }

    });

    pi = pi.replace('{{properties}}', properties.join('\n'));

    writeFileSync(`${pluginFolder}/ui/${effect.id}.html`, pi);
});