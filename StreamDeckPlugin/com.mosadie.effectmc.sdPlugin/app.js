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
    triggerDisconnect.onConnect(jsn);
    playSound.onConnect(jsn);
    showToast.onConnect(jsn);
    stopSound.onConnect(jsn);
    openBook.onConnect(jsn);
    narrate.onConnect(jsn);
    loadWorld.onConnect(jsn);
};

/** ACTIONS */

const joinServer = {
    onConnect: function(jsn) {
        $SD.on('com.mosadie.effectmc.joinserver.willAppear', (jsonObj) => joinServer.onWillAppear(jsonObj));
        $SD.on('com.mosadie.effectmc.joinserver.keyUp', (jsonObj) => joinServer.onKeyUp(jsonObj));
        $SD.on('com.mosadie.effectmc.joinserver.sendToPlugin', (jsonObj) => joinServer.onSendToPlugin(jsonObj));
        $SD.on('com.mosadie.effectmc.joinserver.didReceiveSettings', (jsonObj) => joinServer.onDidReceiveSettings(jsonObj));
        $SD.on('com.mosadie.effectmc.joinserver.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('com.mosadie.effectmc.joinserver.propertyInspectorDidDisappear', (jsonObj) => {
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
        $SD.on('com.mosadie.effectmc.showtitle.willAppear', (jsonObj) => showTitle.onWillAppear(jsonObj));
        $SD.on('com.mosadie.effectmc.showtitle.keyUp', (jsonObj) => showTitle.onKeyUp(jsonObj));
        $SD.on('com.mosadie.effectmc.showtitle.sendToPlugin', (jsonObj) => showTitle.onSendToPlugin(jsonObj));
        $SD.on('com.mosadie.effectmc.showtitle.didReceiveSettings', (jsonObj) => showTitle.onDidReceiveSettings(jsonObj));
        $SD.on('com.mosadie.effectmc.showtitle.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('com.mosadie.effectmc.showtitle.propertyInspectorDidDisappear', (jsonObj) => {
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
        $SD.on('com.mosadie.effectmc.showactionmessage.willAppear', (jsonObj) => showActionMessage.onWillAppear(jsonObj));
        $SD.on('com.mosadie.effectmc.showactionmessage.keyUp', (jsonObj) => showActionMessage.onKeyUp(jsonObj));
        $SD.on('com.mosadie.effectmc.showactionmessage.sendToPlugin', (jsonObj) => showActionMessage.onSendToPlugin(jsonObj));
        $SD.on('com.mosadie.effectmc.showactionmessage.didReceiveSettings', (jsonObj) => showActionMessage.onDidReceiveSettings(jsonObj));
        $SD.on('com.mosadie.effectmc.showactionmessage.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('com.mosadie.effectmc.showactionmessage.propertyInspectorDidDisappear', (jsonObj) => {
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
        $SD.on('com.mosadie.effectmc.sendchatmessage.willAppear', (jsonObj) => sendChatMessage.onWillAppear(jsonObj));
        $SD.on('com.mosadie.effectmc.sendchatmessage.keyUp', (jsonObj) => sendChatMessage.onKeyUp(jsonObj));
        $SD.on('com.mosadie.effectmc.sendchatmessage.sendToPlugin', (jsonObj) => sendChatMessage.onSendToPlugin(jsonObj));
        $SD.on('com.mosadie.effectmc.sendchatmessage.didReceiveSettings', (jsonObj) => sendChatMessage.onDidReceiveSettings(jsonObj));
        $SD.on('com.mosadie.effectmc.sendchatmessage.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('com.mosadie.effectmc.sendchatmessage.propertyInspectorDidDisappear', (jsonObj) => {
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
        $SD.on('com.mosadie.effectmc.setskinlayervisibility.willAppear', (jsonObj) => setSkinLayerVisibility.onWillAppear(jsonObj));
        $SD.on('com.mosadie.effectmc.setskinlayervisibility.keyUp', (jsonObj) => setSkinLayerVisibility.onKeyUp(jsonObj));
        $SD.on('com.mosadie.effectmc.setskinlayervisibility.sendToPlugin', (jsonObj) => setSkinLayerVisibility.onSendToPlugin(jsonObj));
        $SD.on('com.mosadie.effectmc.setskinlayervisibility.didReceiveSettings', (jsonObj) => setSkinLayerVisibility.onDidReceiveSettings(jsonObj));
        $SD.on('com.mosadie.effectmc.setskinlayervisibility.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('com.mosadie.effectmc.setskinlayervisibility.propertyInspectorDidDisappear', (jsonObj) => {
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
        $SD.on('com.mosadie.effectmc.receivechatmessage.willAppear', (jsonObj) => receiveChatMessage.onWillAppear(jsonObj));
        $SD.on('com.mosadie.effectmc.receivechatmessage.keyUp', (jsonObj) => receiveChatMessage.onKeyUp(jsonObj));
        $SD.on('com.mosadie.effectmc.receivechatmessage.sendToPlugin', (jsonObj) => receiveChatMessage.onSendToPlugin(jsonObj));
        $SD.on('com.mosadie.effectmc.receivechatmessage.didReceiveSettings', (jsonObj) => receiveChatMessage.onDidReceiveSettings(jsonObj));
        $SD.on('com.mosadie.effectmc.receivechatmessage.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('com.mosadie.effectmc.receivechatmessage.propertyInspectorDidDisappear', (jsonObj) => {
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

const triggerDisconnect = {
    onConnect: function(jsn) {
        $SD.on('com.mosadie.effectmc.triggerdisconnect.willAppear', (jsonObj) => triggerDisconnect.onWillAppear(jsonObj));
        $SD.on('com.mosadie.effectmc.triggerdisconnect.keyUp', (jsonObj) => triggerDisconnect.onKeyUp(jsonObj));
        $SD.on('com.mosadie.effectmc.triggerdisconnect.sendToPlugin', (jsonObj) => triggerDisconnect.onSendToPlugin(jsonObj));
        $SD.on('com.mosadie.effectmc.triggerdisconnect.didReceiveSettings', (jsonObj) => triggerDisconnect.onDidReceiveSettings(jsonObj));
        $SD.on('com.mosadie.effectmc.triggerdisconnect.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('com.mosadie.effectmc.triggerdisconnect.propertyInspectorDidDisappear', (jsonObj) => {
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
            this.settings.message = '';
            this.settings.nextscreen = "main_menu";
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
        
        var url = new URL('/triggerdisconnect', jsn.payload.settings.minecraftip);
        
        url.searchParams.set('title', (jsn.payload.settings.title ? jsn.payload.settings.title : ''));
        url.searchParams.set('message', (jsn.payload.settings.message ? jsn.payload.settings.message : ''));
        url.searchParams.set('nextscreen', (jsn.payload.settings.nextscreen ? jsn.payload.settings.nextscreen : 'main_menu'));
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

const playSound = {
    onConnect: function(jsn) {
        $SD.on('com.mosadie.effectmc.playsound.willAppear', (jsonObj) => playSound.onWillAppear(jsonObj));
        $SD.on('com.mosadie.effectmc.playsound.keyUp', (jsonObj) => playSound.onKeyUp(jsonObj));
        $SD.on('com.mosadie.effectmc.playsound.sendToPlugin', (jsonObj) => playSound.onSendToPlugin(jsonObj));
        $SD.on('com.mosadie.effectmc.playsound.didReceiveSettings', (jsonObj) => playSound.onDidReceiveSettings(jsonObj));
        $SD.on('com.mosadie.effectmc.playsound.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('com.mosadie.effectmc.playsound.propertyInspectorDidDisappear', (jsonObj) => {
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
            this.settings.sound = 'entity.ghast.ambient';
            this.settings.category = 'master';
            this.settings.volume = 1.0;
            this.settings.pitch = 1.0;
            this.settings.repeat = 'false';
            this.settings.repeatDelay = 1.0;
            this.settings.attenuationType = 'none';
            this.settings.x = '0';
            this.settings.y = '0';
            this.settings.z = '0';
            this.settings.relative = 'false';
            this.settings.global = 'false';
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
        
        var url = new URL('/playsound', jsn.payload.settings.minecraftip);
        
        url.searchParams.set('sound', (jsn.payload.settings.sound ? jsn.payload.settings.sound : ''));
        url.searchParams.set('category', (jsn.payload.settings.category ? jsn.payload.settings.category : 'master'));
        url.searchParams.set('volume', (jsn.payload.settings.volume ? jsn.payload.settings.volume : 1));
        url.searchParams.set('pitch', (jsn.payload.settings.pitch ? jsn.payload.settings.pitch : 1));
        url.searchParams.set('repeat', (jsn.payload.settings.repeat ? jsn.payload.settings.repeat : 'false'));
        url.searchParams.set('repeatDelay', (jsn.payload.settings.repeatDelay ? jsn.payload.settings.repeatDelay : 0));
        url.searchParams.set('attenuationType', (jsn.payload.settings.attenuationType ? jsn.payload.settings.attenuationType : 'none'));
        url.searchParams.set('x', (jsn.payload.settings.x ? jsn.payload.settings.x : '0'));
        url.searchParams.set('y', (jsn.payload.settings.y ? jsn.payload.settings.y : '0'));
        url.searchParams.set('z', (jsn.payload.settings.z ? jsn.payload.settings.z : '0'));
        url.searchParams.set('relative', (jsn.payload.settings.relative ? jsn.payload.settings.relative : 'false'));
        url.searchParams.set('global', (jsn.payload.settings.global ? jsn.payload.settings.global : 'false'));

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

const showToast = {
    onConnect: function(jsn) {
        $SD.on('com.mosadie.effectmc.showtoast.willAppear', (jsonObj) => showToast.onWillAppear(jsonObj));
        $SD.on('com.mosadie.effectmc.showtoast.keyUp', (jsonObj) => showToast.onKeyUp(jsonObj));
        $SD.on('com.mosadie.effectmc.showtoast.sendToPlugin', (jsonObj) => showToast.onSendToPlugin(jsonObj));
        $SD.on('com.mosadie.effectmc.showtoast.didReceiveSettings', (jsonObj) => showToast.onDidReceiveSettings(jsonObj));
        $SD.on('com.mosadie.effectmc.showtoast.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('com.mosadie.effectmc.showtoast.propertyInspectorDidDisappear', (jsonObj) => {
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
        
        var url = new URL('/showtoast', jsn.payload.settings.minecraftip);
        
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

const stopSound = {
    onConnect: function(jsn) {
        $SD.on('com.mosadie.effectmc.stopsound.willAppear', (jsonObj) => stopSound.onWillAppear(jsonObj));
        $SD.on('com.mosadie.effectmc.stopsound.keyUp', (jsonObj) => stopSound.onKeyUp(jsonObj));
        $SD.on('com.mosadie.effectmc.stopsound.sendToPlugin', (jsonObj) => stopSound.onSendToPlugin(jsonObj));
        $SD.on('com.mosadie.effectmc.stopsound.didReceiveSettings', (jsonObj) => stopSound.onDidReceiveSettings(jsonObj));
        $SD.on('com.mosadie.effectmc.stopsound.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('com.mosadie.effectmc.stopsound.propertyInspectorDidDisappear', (jsonObj) => {
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
            this.settings.mode = 'all';
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
        
        var url = new URL('/stopsound', jsn.payload.settings.minecraftip);
        
        if ((jsn.payload.settings.mode == "both" || jsn.payload.settings.mode == "name") && jsn.payload.settings.sound) {
            url.searchParams.set('sound', jsn.payload.settings.sound);
        }
        
        if ((jsn.payload.settings.mode == "both" || jsn.payload.settings.mode == "category") && jsn.payload.settings.category) {
            url.searchParams.set('category', jsn.payload.settings.category);
        }
        

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

const openBook = {
    onConnect: function(jsn) {
        $SD.on('com.mosadie.effectmc.openbook.willAppear', (jsonObj) => openBook.onWillAppear(jsonObj));
        $SD.on('com.mosadie.effectmc.openbook.keyUp', (jsonObj) => openBook.onKeyUp(jsonObj));
        $SD.on('com.mosadie.effectmc.openbook.sendToPlugin', (jsonObj) => openBook.onSendToPlugin(jsonObj));
        $SD.on('com.mosadie.effectmc.openbook.didReceiveSettings', (jsonObj) => openBook.onDidReceiveSettings(jsonObj));
        $SD.on('com.mosadie.effectmc.openbook.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('com.mosadie.effectmc.openbook.propertyInspectorDidDisappear', (jsonObj) => {
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
            this.settings.book = '{}';
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
        
        var url = new URL('/openbook', jsn.payload.settings.minecraftip);
        
        url.searchParams.set('device', jsn.device);
        
        fetch(url, {method: 'POST', body: (jsn.payload.settings.book ? jsn.payload.settings.book : '')}).then(response => {
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

const narrate = {
    onConnect: function(jsn) {
        $SD.on('com.mosadie.effectmc.narrate.willAppear', (jsonObj) => narrate.onWillAppear(jsonObj));
        $SD.on('com.mosadie.effectmc.narrate.keyUp', (jsonObj) => narrate.onKeyUp(jsonObj));
        $SD.on('com.mosadie.effectmc.narrate.sendToPlugin', (jsonObj) => narrate.onSendToPlugin(jsonObj));
        $SD.on('com.mosadie.effectmc.narrate.didReceiveSettings', (jsonObj) => narrate.onDidReceiveSettings(jsonObj));
        $SD.on('com.mosadie.effectmc.narrate.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('com.mosadie.effectmc.narrate.propertyInspectorDidDisappear', (jsonObj) => {
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
            this.settings.message = 'Hello!';
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
        
        var url = new URL('/narrate', jsn.payload.settings.minecraftip);
        
        url.searchParams.set('message', (jsn.payload.settings.message ? jsn.payload.settings.message : ''));
        url.searchParams.set('interrupt', (jsn.payload.settings.interrupt ? jsn.payload.settings.interrupt : 'false'));
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

const loadWorld = {
    onConnect: function(jsn) {
        $SD.on('com.mosadie.effectmc.loadworld.willAppear', (jsonObj) => loadWorld.onWillAppear(jsonObj));
        $SD.on('com.mosadie.effectmc.loadworld.keyUp', (jsonObj) => loadWorld.onKeyUp(jsonObj));
        $SD.on('com.mosadie.effectmc.loadworld.sendToPlugin', (jsonObj) => loadWorld.onSendToPlugin(jsonObj));
        $SD.on('com.mosadie.effectmc.loadworld.didReceiveSettings', (jsonObj) => loadWorld.onDidReceiveSettings(jsonObj));
        $SD.on('com.mosadie.effectmc.loadworld.propertyInspectorDidAppear', (jsonObj) => {
            console.log('%c%s', 'color: white; background: black; font-size: 13px;', '[app.js]propertyInspectorDidAppear:');
        });
        $SD.on('com.mosadie.effectmc.loadworld.propertyInspectorDidDisappear', (jsonObj) => {
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
            this.settings.world = 'New World';
        }
    },

    onKeyUp: function (jsn) {
        this.doSomeThing(jsn, 'onKeyUp', 'green');
        console.log(jsn);
        
        if (!jsn.payload.settings || !jsn.payload.settings.world || jsn.payload.settings.world == '') { //TODO check this
            $SD.api.showAlert(jsn.context);
            console.log('No World!');
            return;
        } else if (!jsn.payload.settings || !jsn.payload.settings.minecraftip || jsn.payload.settings.minecraftip == '') {
            $SD.api.showAlert(jsn.context);
            console.log('No Minecraft IP!');
            return;
        }
        
        var url = new URL('/loadworld', jsn.payload.settings.minecraftip);
        
        url.searchParams.set('world', jsn.payload.settings.world);
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