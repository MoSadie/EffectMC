/* global $CC, Utils, $SD */

/**
 * Here are a couple of wrappers we created to help ypu quickly setup
 * your plugin and subscribe to events sent by Stream Deck to your plugin.
 */

 /**
  * The 'connected' event is sent to your plugin, after the plugin's instance
  * is registered with Stream Deck software. It carries the current websocket
  * and other information about the current environmet in a JSON object
  * You can use it to subscribe to events you want to use in your plugin.
  */

$SD.on('connected', (jsonObj) => connected(jsonObj));

function connected(jsn) {
    /** subscribe to the willAppear and other events */
    joinServer.onConnect(jsn);
    showTitle.onConnect(jsn);
    showActionMessage.onConnect(jsn);
    sendChatMessage.onConnect(jsn);
    setSkinLayerVisibility.onConnect(jsn);
    receiveChatMessage.onConnect(jsn);
};

/** ACTIONS */

const joinServer = {
    onConnect: function(jsn) {
        $SD.on('io.github.mosadie.mcsde.joinserver.willAppear', (jsonObj) => joinServer.onWillAppear(jsonObj));
        $SD.on('io.github.mosadie.mcsde.joinserver.keyUp', (jsonObj) => joinServer.onKeyUp(jsonObj));
        $SD.on('io.github.mosadie.mcsde.joinserver.sendToPlugin', (jsonObj) => joinServer.onSendToPlugin(jsonObj));
        $SD.on('io.github.mosadie.mcsde.joinserver.didReceiveSettings', (jsonObj) => joinServer.onDidReceiveSettings(jsonObj));
        $SD.on('io.github.mosadie.mcsde.joinserver.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('io.github.mosadie.mcsde.joinserver.propertyInspectorDidDisappear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: red; font-size: 13px;', '[app.js]propertyInspectorDidDisappear:');
        });
    },

    settings:{},
    onDidReceiveSettings: function(jsn) {
        this.settings = Utils.getProp(jsn, 'payload.settings', {});

    },

    /** 
     * The 'willAppear' event is the first event a key will receive, right before it gets
     * showed on your Stream Deck and/or in Stream Deck software.
     * This event is a good place to setup your plugin and look at current settings (if any),
     * which are embedded in the events payload.
     */

    onWillAppear: function (jsn) {
        //console.log("You can cache your settings in 'onWillAppear'", jsn.payload.settings);
        /**
         * "The willAppear event carries your saved settings (if any). You can use these settings
         * to setup your plugin or save the settings for later use. 
         * If you want to request settings at a later time, you can do so using the
         * 'getSettings' event, which will tell Stream Deck to send your data 
         * (in the 'didReceiceSettings above)
         * 
         * $SD.api.getSettings(jsn.context);
        */
        this.settings = jsn.payload.settings;

        // If no settings, fill in some default settings.
        if (!this.settings || Object.keys(this.settings).length === 0) {
            this.settings.minecraftip = 'http://localhost:3000';
            this.settings.serverip = 'localhost';
        }
    },

    onKeyUp: function (jsn) {
        this.doSomeThing(jsn, 'onKeyUp', 'green');
        console.log(jsn);
        
        if (!jsn.payload.settings || !jsn.payload.settings.serverip || jsn.payload.settings.serverip == '') { //TODO check this
            $SD.api.showAlert(jsn.context);
            console.log('No Server IP!');
            return;
        } else if (!jsn.payload.settings || !jsn.payload.settings.minecraftip || jsn.payload.settings.minecraftip == '') {
            $SD.api.showAlert(jsn.context);
            console.log('No Minecraft IP!');
            return;
        }
        
        var url = new URL('/joinserver', jsn.payload.settings.minecraftip);
        
        url.searchParams.set('serverip', jsn.payload.settings.serverip);
        url.searchParams.set('device', jsn.device);
        
        fetch(url).then(response => {
            console.log("DEBUG yay", response);
            if (response.status == 200) $SD.api.showOk(jsn.context);
            else {
                console.log("Request Failed", response);
                $SD.api.showAlert(jsn.context);
            }
        }, reason => {
            console.log("DEBUG fail", reason);
            $SD.api.showAlert(jsn.context);
        });
    },

    onSendToPlugin: function (jsn) {
        /**
         * this is a message sent directly from the Property Inspector 
         * (e.g. some value, which is not saved to settings) 
         * You can send this event from Property Inspector (see there for an example)
         */ 

        const sdpi_collection = Utils.getProp(jsn, 'payload.sdpi_collection', {});
        if (sdpi_collection.value && sdpi_collection.value !== undefined) {
            this.doSomeThing({ [sdpi_collection.key] : sdpi_collection.value }, 'onSendToPlugin', 'fuchsia');            
        }
    },

    /**
     * This snippet shows, how you could save settings persistantly to Stream Deck software
     * It is not used in this example plugin.
     */

    saveSettings: function (jsn, sdpi_collection) {
        console.log('saveSettings:', jsn);
        if (sdpi_collection.hasOwnProperty('key') && sdpi_collection.key != '') {
            if (sdpi_collection.value && sdpi_collection.value !== undefined) {
                this.settings[sdpi_collection.key] = sdpi_collection.value;
                console.log('setSettings....', this.settings);
                $SD.api.setSettings(jsn.context, this.settings);
            }
        }
    },

    /**
     * Finally here's a methood which gets called from various events above.
     * This is just an idea how you can act on receiving some interesting message
     * from Stream Deck.
     */

    doSomeThing: function(inJsonData, caller, tagColor) {
        console.log('%c%s', `color: white; background: ${tagColor || 'grey'}; font-size: 15px;`, `[app.js]doSomeThing from: ${caller}`);
        // console.log(inJsonData);
    }, 


};

const showTitle = {
    onConnect: function(jsn) {
        $SD.on('io.github.mosadie.mcsde.showtitle.willAppear', (jsonObj) => showTitle.onWillAppear(jsonObj));
        $SD.on('io.github.mosadie.mcsde.showtitle.keyUp', (jsonObj) => showTitle.onKeyUp(jsonObj));
        $SD.on('io.github.mosadie.mcsde.showtitle.sendToPlugin', (jsonObj) => showTitle.onSendToPlugin(jsonObj));
        $SD.on('io.github.mosadie.mcsde.showtitle.didReceiveSettings', (jsonObj) => showTitle.onDidReceiveSettings(jsonObj));
        $SD.on('io.github.mosadie.mcsde.showtitle.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('io.github.mosadie.mcsde.showtitle.propertyInspectorDidDisappear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: red; font-size: 13px;', '[app.js]propertyInspectorDidDisappear:');
        });
    },

    settings:{},
    onDidReceiveSettings: function(jsn) {
        this.settings = Utils.getProp(jsn, 'payload.settings', {});

    },

    /** 
     * The 'willAppear' event is the first event a key will receive, right before it gets
     * showed on your Stream Deck and/or in Stream Deck software.
     * This event is a good place to setup your plugin and look at current settings (if any),
     * which are embedded in the events payload.
     */

    onWillAppear: function (jsn) {
        //console.log("You can cache your settings in 'onWillAppear'", jsn.payload.settings);
        /**
         * "The willAppear event carries your saved settings (if any). You can use these settings
         * to setup your plugin or save the settings for later use. 
         * If you want to request settings at a later time, you can do so using the
         * 'getSettings' event, which will tell Stream Deck to send your data 
         * (in the 'didReceiceSettings above)
         * 
         * $SD.api.getSettings(jsn.context);
        */
        this.settings = jsn.payload.settings;

        // If no settings, fill in some default settings.
        if (!this.settings || Object.keys(this.settings).length === 0) {
            this.settings.title = '§9Hello!';
            this.settings.subtitle = '';
        }
    },

    onKeyUp: function (jsn) {
        this.doSomeThing(jsn, 'onKeyUp', 'green');
        console.log(jsn);
        
        if (!jsn.payload.settings || !jsn.payload.settings.minecraftip || jsn.payload.settings.minecraftip == '') {
            $SD.api.showAlert(jsn.context);
            console.log('No Minecraft IP!');
            return;
        }
        
        var url = new URL('/showtitle', jsn.payload.settings.minecraftip);
        
        url.searchParams.set('title', (jsn.payload.settings.title ? jsn.payload.settings.title : ''));
        url.searchParams.set('subtitle', (jsn.payload.settings.subtitle ? jsn.payload.settings.subtitle : ''));
        url.searchParams.set('device', jsn.device);
        
        fetch(url).then(response => {
            console.log("DEBUG yay", response);
            if (response.status == 200) $SD.api.showOk(jsn.context);
            else {
                console.log("Request Failed", response);
                $SD.api.showAlert(jsn.context);
            }
        }, reason => {
            console.log("DEBUG fail", reason);
            $SD.api.showAlert(jsn.context);
        });
    },

    onSendToPlugin: function (jsn) {
        /**
         * this is a message sent directly from the Property Inspector 
         * (e.g. some value, which is not saved to settings) 
         * You can send this event from Property Inspector (see there for an example)
         */ 

        const sdpi_collection = Utils.getProp(jsn, 'payload.sdpi_collection', {});
        if (sdpi_collection.value && sdpi_collection.value !== undefined) {
            this.doSomeThing({ [sdpi_collection.key] : sdpi_collection.value }, 'onSendToPlugin', 'fuchsia');            
        }
    },

    /**
     * This snippet shows, how you could save settings persistantly to Stream Deck software
     * It is not used in this example plugin.
     */

    saveSettings: function (jsn, sdpi_collection) {
        console.log('saveSettings:', jsn);
        if (sdpi_collection.hasOwnProperty('key') && sdpi_collection.key != '') {
            if (sdpi_collection.value && sdpi_collection.value !== undefined) {
                this.settings[sdpi_collection.key] = sdpi_collection.value;
                console.log('setSettings....', this.settings);
                $SD.api.setSettings(jsn.context, this.settings);
            }
        }
    },

    /**
     * Finally here's a methood which gets called from various events above.
     * This is just an idea how you can act on receiving some interesting message
     * from Stream Deck.
     */

    doSomeThing: function(inJsonData, caller, tagColor) {
        console.log('%c%s', `color: white; background: ${tagColor || 'grey'}; font-size: 15px;`, `[app.js]doSomeThing from: ${caller}`);
        // console.log(inJsonData);
    }, 


};

const showActionMessage = {
    onConnect: function(jsn) {
        $SD.on('io.github.mosadie.mcsde.showactionmessage.willAppear', (jsonObj) => showActionMessage.onWillAppear(jsonObj));
        $SD.on('io.github.mosadie.mcsde.showactionmessage.keyUp', (jsonObj) => showActionMessage.onKeyUp(jsonObj));
        $SD.on('io.github.mosadie.mcsde.showactionmessage.sendToPlugin', (jsonObj) => showActionMessage.onSendToPlugin(jsonObj));
        $SD.on('io.github.mosadie.mcsde.showactionmessage.didReceiveSettings', (jsonObj) => showActionMessage.onDidReceiveSettings(jsonObj));
        $SD.on('io.github.mosadie.mcsde.showactionmessage.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('io.github.mosadie.mcsde.showactionmessage.propertyInspectorDidDisappear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: red; font-size: 13px;', '[app.js]propertyInspectorDidDisappear:');
        });
    },

    settings:{},
    onDidReceiveSettings: function(jsn) {
        this.settings = Utils.getProp(jsn, 'payload.settings', {});

    },

    /** 
     * The 'willAppear' event is the first event a key will receive, right before it gets
     * showed on your Stream Deck and/or in Stream Deck software.
     * This event is a good place to setup your plugin and look at current settings (if any),
     * which are embedded in the events payload.
     */

    onWillAppear: function (jsn) {
        //console.log("You can cache your settings in 'onWillAppear'", jsn.payload.settings);
        /**
         * "The willAppear event carries your saved settings (if any). You can use these settings
         * to setup your plugin or save the settings for later use. 
         * If you want to request settings at a later time, you can do so using the
         * 'getSettings' event, which will tell Stream Deck to send your data 
         * (in the 'didReceiceSettings above)
         * 
         * $SD.api.getSettings(jsn.context);
        */
        this.settings = jsn.payload.settings;

        // If no settings, fill in some default settings.
        if (!this.settings || Object.keys(this.settings).length === 0) {
            this.settings.message = '§9Hello!';
        }
    },

    onKeyUp: function (jsn) {
        this.doSomeThing(jsn, 'onKeyUp', 'green');
        console.log(jsn);
        
        if (!jsn.payload.settings || !jsn.payload.settings.minecraftip || jsn.payload.settings.minecraftip == '') {
            $SD.api.showAlert(jsn.context);
            console.log('No Minecraft IP!');
            return;
        }
        
        var url = new URL('/showactionmessage', jsn.payload.settings.minecraftip);
        
        url.searchParams.set('message', (jsn.payload.settings.message ? jsn.payload.settings.message : ''));
        url.searchParams.set('device', jsn.device);
        
        fetch(url).then(response => {
            console.log("DEBUG yay", response);
            if (response.status == 200) $SD.api.showOk(jsn.context);
            else {
                console.log("Request Failed", response);
                $SD.api.showAlert(jsn.context);
            }
        }, reason => {
            console.log("DEBUG fail", reason);
            $SD.api.showAlert(jsn.context);
        });
    },

    onSendToPlugin: function (jsn) {
        /**
         * this is a message sent directly from the Property Inspector 
         * (e.g. some value, which is not saved to settings) 
         * You can send this event from Property Inspector (see there for an example)
         */ 

        const sdpi_collection = Utils.getProp(jsn, 'payload.sdpi_collection', {});
        if (sdpi_collection.value && sdpi_collection.value !== undefined) {
            this.doSomeThing({ [sdpi_collection.key] : sdpi_collection.value }, 'onSendToPlugin', 'fuchsia');            
        }
    },

    /**
     * This snippet shows, how you could save settings persistantly to Stream Deck software
     * It is not used in this example plugin.
     */

    saveSettings: function (jsn, sdpi_collection) {
        console.log('saveSettings:', jsn);
        if (sdpi_collection.hasOwnProperty('key') && sdpi_collection.key != '') {
            if (sdpi_collection.value && sdpi_collection.value !== undefined) {
                this.settings[sdpi_collection.key] = sdpi_collection.value;
                console.log('setSettings....', this.settings);
                $SD.api.setSettings(jsn.context, this.settings);
            }
        }
    },

    /**
     * Finally here's a methood which gets called from various events above.
     * This is just an idea how you can act on receiving some interesting message
     * from Stream Deck.
     */

    doSomeThing: function(inJsonData, caller, tagColor) {
        console.log('%c%s', `color: white; background: ${tagColor || 'grey'}; font-size: 15px;`, `[app.js]doSomeThing from: ${caller}`);
        // console.log(inJsonData);
    }, 


};

const sendChatMessage = {
    onConnect: function(jsn) {
        $SD.on('io.github.mosadie.mcsde.sendchatmessage.willAppear', (jsonObj) => sendChatMessage.onWillAppear(jsonObj));
        $SD.on('io.github.mosadie.mcsde.sendchatmessage.keyUp', (jsonObj) => sendChatMessage.onKeyUp(jsonObj));
        $SD.on('io.github.mosadie.mcsde.sendchatmessage.sendToPlugin', (jsonObj) => sendChatMessage.onSendToPlugin(jsonObj));
        $SD.on('io.github.mosadie.mcsde.sendchatmessage.didReceiveSettings', (jsonObj) => sendChatMessage.onDidReceiveSettings(jsonObj));
        $SD.on('io.github.mosadie.mcsde.sendchatmessage.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('io.github.mosadie.mcsde.sendchatmessage.propertyInspectorDidDisappear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: red; font-size: 13px;', '[app.js]propertyInspectorDidDisappear:');
        });
    },

    settings:{},
    onDidReceiveSettings: function(jsn) {
        this.settings = Utils.getProp(jsn, 'payload.settings', {});

    },

    /** 
     * The 'willAppear' event is the first event a key will receive, right before it gets
     * showed on your Stream Deck and/or in Stream Deck software.
     * This event is a good place to setup your plugin and look at current settings (if any),
     * which are embedded in the events payload.
     */

    onWillAppear: function (jsn) {
        //console.log("You can cache your settings in 'onWillAppear'", jsn.payload.settings);
        /**
         * "The willAppear event carries your saved settings (if any). You can use these settings
         * to setup your plugin or save the settings for later use. 
         * If you want to request settings at a later time, you can do so using the
         * 'getSettings' event, which will tell Stream Deck to send your data 
         * (in the 'didReceiceSettings above)
         * 
         * $SD.api.getSettings(jsn.context);
        */
        this.settings = jsn.payload.settings;

        // If no settings, fill in some default settings.
        if (!this.settings || Object.keys(this.settings).length === 0) {
            this.settings.message = '§9Hello!';
        }
    },

    onKeyUp: function (jsn) {
        this.doSomeThing(jsn, 'onKeyUp', 'green');
        console.log(jsn);
        
        if (!jsn.payload.settings || !jsn.payload.settings.minecraftip || jsn.payload.settings.minecraftip == '') {
            $SD.api.showAlert(jsn.context);
            console.log('No Minecraft IP!');
            return;
        }
        
        var url = new URL('/sendchatmessage', jsn.payload.settings.minecraftip);
        
        url.searchParams.set('message', (jsn.payload.settings.message ? jsn.payload.settings.message : ''));
        url.searchParams.set('device', jsn.device);
        
        fetch(url).then(response => {
            console.log("DEBUG yay", response);
            if (response.status == 200) $SD.api.showOk(jsn.context);
            else {
                console.log("Request Failed", response);
                $SD.api.showAlert(jsn.context);
            }
        }, reason => {
            console.log("DEBUG fail", reason);
            $SD.api.showAlert(jsn.context);
        });
    },

    onSendToPlugin: function (jsn) {
        /**
         * this is a message sent directly from the Property Inspector 
         * (e.g. some value, which is not saved to settings) 
         * You can send this event from Property Inspector (see there for an example)
         */ 

        const sdpi_collection = Utils.getProp(jsn, 'payload.sdpi_collection', {});
        if (sdpi_collection.value && sdpi_collection.value !== undefined) {
            this.doSomeThing({ [sdpi_collection.key] : sdpi_collection.value }, 'onSendToPlugin', 'fuchsia');            
        }
    },

    /**
     * This snippet shows, how you could save settings persistantly to Stream Deck software
     * It is not used in this example plugin.
     */

    saveSettings: function (jsn, sdpi_collection) {
        console.log('saveSettings:', jsn);
        if (sdpi_collection.hasOwnProperty('key') && sdpi_collection.key != '') {
            if (sdpi_collection.value && sdpi_collection.value !== undefined) {
                this.settings[sdpi_collection.key] = sdpi_collection.value;
                console.log('setSettings....', this.settings);
                $SD.api.setSettings(jsn.context, this.settings);
            }
        }
    },

    /**
     * Finally here's a methood which gets called from various events above.
     * This is just an idea how you can act on receiving some interesting message
     * from Stream Deck.
     */

    doSomeThing: function(inJsonData, caller, tagColor) {
        console.log('%c%s', `color: white; background: ${tagColor || 'grey'}; font-size: 15px;`, `[app.js]doSomeThing from: ${caller}`);
        // console.log(inJsonData);
    }, 


};

const setSkinLayerVisibility = {
    onConnect: function(jsn) {
        $SD.on('io.github.mosadie.mcsde.setskinlayervisibility.willAppear', (jsonObj) => setSkinLayerVisibility.onWillAppear(jsonObj));
        $SD.on('io.github.mosadie.mcsde.setskinlayervisibility.keyUp', (jsonObj) => setSkinLayerVisibility.onKeyUp(jsonObj));
        $SD.on('io.github.mosadie.mcsde.setskinlayervisibility.sendToPlugin', (jsonObj) => setSkinLayerVisibility.onSendToPlugin(jsonObj));
        $SD.on('io.github.mosadie.mcsde.setskinlayervisibility.didReceiveSettings', (jsonObj) => setSkinLayerVisibility.onDidReceiveSettings(jsonObj));
        $SD.on('io.github.mosadie.mcsde.setskinlayervisibility.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('io.github.mosadie.mcsde.setskinlayervisibility.propertyInspectorDidDisappear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: red; font-size: 13px;', '[app.js]propertyInspectorDidDisappear:');
        });
    },

    settings:{},
    onDidReceiveSettings: function(jsn) {
        this.settings = Utils.getProp(jsn, 'payload.settings', {});

    },

    /** 
     * The 'willAppear' event is the first event a key will receive, right before it gets
     * showed on your Stream Deck and/or in Stream Deck software.
     * This event is a good place to setup your plugin and look at current settings (if any),
     * which are embedded in the events payload.
     */

    onWillAppear: function (jsn) {
        //console.log("You can cache your settings in 'onWillAppear'", jsn.payload.settings);
        /**
         * "The willAppear event carries your saved settings (if any). You can use these settings
         * to setup your plugin or save the settings for later use. 
         * If you want to request settings at a later time, you can do so using the
         * 'getSettings' event, which will tell Stream Deck to send your data 
         * (in the 'didReceiceSettings above)
         * 
         * $SD.api.getSettings(jsn.context);
        */
        this.settings = jsn.payload.settings;

        // If no settings, fill in some default settings.
        if (!this.settings || Object.keys(this.settings).length === 0) {
            this.settings.section = 'all';
            this.settings.visibility = 'toggle';
        }
    },

    onKeyUp: function (jsn) {
        this.doSomeThing(jsn, 'onKeyUp', 'green');
        console.log(jsn);
        
        if (!jsn.payload.settings || !jsn.payload.settings.minecraftip || jsn.payload.settings.minecraftip == '') {
            $SD.api.showAlert(jsn.context);
            console.log('No Minecraft IP!');
            return;
        }
        
        var url = new URL('/setskinlayervisibility', jsn.payload.settings.minecraftip);
        
        url.searchParams.set('section', (jsn.payload.settings.section ? jsn.payload.settings.section : 'all'));
        url.searchParams.set('device', jsn.device);

        if (jsn.payload.settings.visibility && jsn.payload.settings.visibility != 'toggle') {
            url.searchParams.set('visibility', jsn.payload.settings.visibility);
        }
        
        fetch(url).then(response => {
            console.log("DEBUG yay", response);
            if (response.status == 200) $SD.api.showOk(jsn.context);
            else {
                console.log("Request Failed", response);
                $SD.api.showAlert(jsn.context);
            }
        }, reason => {
            console.log("DEBUG fail", reason);
            $SD.api.showAlert(jsn.context);
        });
    },

    onSendToPlugin: function (jsn) {
        /**
         * this is a message sent directly from the Property Inspector 
         * (e.g. some value, which is not saved to settings) 
         * You can send this event from Property Inspector (see there for an example)
         */ 

        const sdpi_collection = Utils.getProp(jsn, 'payload.sdpi_collection', {});
        if (sdpi_collection.value && sdpi_collection.value !== undefined) {
            this.doSomeThing({ [sdpi_collection.key] : sdpi_collection.value }, 'onSendToPlugin', 'fuchsia');            
        }
    },

    /**
     * This snippet shows, how you could save settings persistantly to Stream Deck software
     * It is not used in this example plugin.
     */

    saveSettings: function (jsn, sdpi_collection) {
        console.log('saveSettings:', jsn);
        if (sdpi_collection.hasOwnProperty('key') && sdpi_collection.key != '') {
            if (sdpi_collection.value && sdpi_collection.value !== undefined) {
                this.settings[sdpi_collection.key] = sdpi_collection.value;
                console.log('setSettings....', this.settings);
                $SD.api.setSettings(jsn.context, this.settings);
            }
        }
    },

    /**
     * Finally here's a methood which gets called from various events above.
     * This is just an idea how you can act on receiving some interesting message
     * from Stream Deck.
     */

    doSomeThing: function(inJsonData, caller, tagColor) {
        console.log('%c%s', `color: white; background: ${tagColor || 'grey'}; font-size: 15px;`, `[app.js]doSomeThing from: ${caller}`);
        // console.log(inJsonData);
    }, 


};

const receiveChatMessage = {
    onConnect: function(jsn) {
        $SD.on('io.github.mosadie.mcsde.receivechatmessage.willAppear', (jsonObj) => receiveChatMessage.onWillAppear(jsonObj));
        $SD.on('io.github.mosadie.mcsde.receivechatmessage.keyUp', (jsonObj) => receiveChatMessage.onKeyUp(jsonObj));
        $SD.on('io.github.mosadie.mcsde.receivechatmessage.sendToPlugin', (jsonObj) => receiveChatMessage.onSendToPlugin(jsonObj));
        $SD.on('io.github.mosadie.mcsde.receivechatmessage.didReceiveSettings', (jsonObj) => receiveChatMessage.onDidReceiveSettings(jsonObj));
        $SD.on('io.github.mosadie.mcsde.receivechatmessage.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('io.github.mosadie.mcsde.receivechatmessage.propertyInspectorDidDisappear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: red; font-size: 13px;', '[app.js]propertyInspectorDidDisappear:');
        });
    },

    settings:{},
    onDidReceiveSettings: function(jsn) {
        this.settings = Utils.getProp(jsn, 'payload.settings', {});

    },

    /** 
     * The 'willAppear' event is the first event a key will receive, right before it gets
     * showed on your Stream Deck and/or in Stream Deck software.
     * This event is a good place to setup your plugin and look at current settings (if any),
     * which are embedded in the events payload.
     */

    onWillAppear: function (jsn) {
        //console.log("You can cache your settings in 'onWillAppear'", jsn.payload.settings);
        /**
         * "The willAppear event carries your saved settings (if any). You can use these settings
         * to setup your plugin or save the settings for later use. 
         * If you want to request settings at a later time, you can do so using the
         * 'getSettings' event, which will tell Stream Deck to send your data 
         * (in the 'didReceiceSettings above)
         * 
         * $SD.api.getSettings(jsn.context);
        */
        this.settings = jsn.payload.settings;

        // If no settings, fill in some default settings.
        if (!this.settings || Object.keys(this.settings).length === 0) {
            this.settings.message = '§9Hello!';
        }
    },

    onKeyUp: function (jsn) {
        this.doSomeThing(jsn, 'onKeyUp', 'green');
        console.log(jsn);
        
        if (!jsn.payload.settings || !jsn.payload.settings.minecraftip || jsn.payload.settings.minecraftip == '') {
            $SD.api.showAlert(jsn.context);
            console.log('No Minecraft IP!');
            return;
        }
        
        var url = new URL('/receivechat', jsn.payload.settings.minecraftip);
        
        url.searchParams.set('message', (jsn.payload.settings.message ? jsn.payload.settings.message : ''));
        url.searchParams.set('device', jsn.device);
        
        fetch(url).then(response => {
            console.log("DEBUG yay", response);
            if (response.status == 200) $SD.api.showOk(jsn.context);
            else {
                console.log("Request Failed", response);
                $SD.api.showAlert(jsn.context);
            }
        }, reason => {
            console.log("DEBUG fail", reason);
            $SD.api.showAlert(jsn.context);
        });
    },

    onSendToPlugin: function (jsn) {
        /**
         * this is a message sent directly from the Property Inspector 
         * (e.g. some value, which is not saved to settings) 
         * You can send this event from Property Inspector (see there for an example)
         */ 

        const sdpi_collection = Utils.getProp(jsn, 'payload.sdpi_collection', {});
        if (sdpi_collection.value && sdpi_collection.value !== undefined) {
            this.doSomeThing({ [sdpi_collection.key] : sdpi_collection.value }, 'onSendToPlugin', 'fuchsia');            
        }
    },

    /**
     * This snippet shows, how you could save settings persistantly to Stream Deck software
     * It is not used in this example plugin.
     */

    saveSettings: function (jsn, sdpi_collection) {
        console.log('saveSettings:', jsn);
        if (sdpi_collection.hasOwnProperty('key') && sdpi_collection.key != '') {
            if (sdpi_collection.value && sdpi_collection.value !== undefined) {
                this.settings[sdpi_collection.key] = sdpi_collection.value;
                console.log('setSettings....', this.settings);
                $SD.api.setSettings(jsn.context, this.settings);
            }
        }
    },

    /**
     * Finally here's a methood which gets called from various events above.
     * This is just an idea how you can act on receiving some interesting message
     * from Stream Deck.
     */

    doSomeThing: function(inJsonData, caller, tagColor) {
        console.log('%c%s', `color: white; background: ${tagColor || 'grey'}; font-size: 15px;`, `[app.js]doSomeThing from: ${caller}`);
        // console.log(inJsonData);
    }, 


};