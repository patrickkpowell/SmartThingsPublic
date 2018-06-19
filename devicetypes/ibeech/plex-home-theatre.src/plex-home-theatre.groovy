metadata {
	definition (name: "Plex Home Theatre", namespace: "ibeech", author: "ibeech") {
    	capability "Switch"
		capability "musicPlayer"
        
        command "scanNewClients"
        command "setPlaybackIcon", ["string"]
        command "setPlaybackTitle", ["string"]
        command "setVolumeLevel", ["number"]        
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
        
        multiAttributeTile(name:"status", type: "generic", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
            attributeState "playing", label:'Playing', action:"music Player.pause", icon:"st.Electronics.electronics16", nextState:"paused", backgroundColor:"#79b821"
            attributeState "stopped", label:'Stopped', action:"music Player.play", icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
            attributeState "paused", label:'Paused', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#FFA500"
        }        
            tileAttribute ("device.trackDescription", key: "SECONDARY_CONTROL") {
                attributeState "trackDescription", label:'${currentValue}'
            }

            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action: "setVolumeLevel"
            }
        }
        
        standardTile("next", "device.status", width: 2, height: 2, decoration: "flat") {
        	state "next", label:'', action:"music Player.nextTrack", icon:"st.sonos.next-btn", backgroundColor:"#ffffff"
        }
        
        standardTile("previous", "device.status", width: 2, height: 2, decoration: "flat") {
        	state "previous", label:'', action:"music Player.previousTrack", icon:"st.sonos.previous-btn", backgroundColor:"#ffffff"
        }	
        
        standardTile("fillerTile", "device.status", width: 5, height: 1, decoration: "flat") {
        	state "default", label:'', action:"", icon:"", backgroundColor:"#ffffff"
        }
        
        standardTile("stop", "device.status", width: 2, height: 2, decoration: "flat") {
            state "default", label:'', action:"music Player.stop", icon:"st.sonos.stop-btn", backgroundColor:"#ffffff"
            state "grouped", label:'', action:"music Player.stop", icon:"st.sonos.stop-btn", backgroundColor:"#ffffff"
        }
        
        standardTile("scanNewClients", "device.status", width: 5, height: 1, decoration: "flat") {
            state "default", label:'Scan New Clients', action:"scanNewClients", icon:"state.icon", backgroundColor:"#ffffff"
            state "grouped", label:'', action:"scanNewClients", icon:"state.icon", backgroundColor:"#ffffff"
        }
        
        main "status"
        details (["status", "previous", "stop", "next", "currentSong"])
    }
}

// parse events into attributes
def parse(String description) {
	log.debug "Virtual siwtch parsing '${description}'"
}

def play() {
	log.debug "Executing 'on'"	     
    
    
    sendEvent(name: "switch", value: device.deviceNetworkId + ".play");    
    sendEvent(name: "switch", value: "on");    
    sendEvent(name: "status", value: "playing");
}

def pause() {
	log.debug "Executing 'pause'"
	    
	sendEvent(name: "switch", value: device.deviceNetworkId + ".pause");     
    sendEvent(name: "switch", value: "off");
    sendEvent(name: "status", value: "paused");
}

def stop() {
	log.debug "Executing 'off'"
	    
	sendEvent(name: "switch", value: device.deviceNetworkId + ".stop");     
    sendEvent(name: "switch", value: "off");
    sendEvent(name: "status", value: "stopped");
    setPlaybackTitle("Stopped");
}

def previousTrack() {
	log.debug "Executing 'previous': "
    
    setPlaybackTitle("Skipping previous");
    sendCommand("previous");    
}

def nextTrack() {
	log.debug "Executing 'next'"

	setPlaybackTitle("Skipping next");
	sendCommand("next");
}

def scanNewClients() {
	log.debug "Executing 'scanNewClients'"        
    sendCommand("scanNewClients");
}

def setVolumeLevel(level) {
	log.debug "Executing 'setVolumeLevel(" + level + ")'"
    sendEvent(name: "level", value: level);
    sendCommand("setVolume." + level);
}

def sendCommand(command) {
	
    def lastState = device.currentState('switch').getValue();
    sendEvent(name: "switch", value: device.deviceNetworkId + "." + command);
    sendEvent(name: "switch", value: lastState);
}

def setPlaybackState(state) {

	log.debug "Executing 'setPlaybackState'"
    switch(state) {
        case "stopped":
        sendEvent(name: "switch", value: "off");
        sendEvent(name: "status", value: "stopped");
        break;

        case "playing":
        sendEvent(name: "switch", value: "on");
        sendEvent(name: "status", value: "playing");
        break;

        case "paused":
        sendEvent(name: "switch", value: "off");
        sendEvent(name: "status", value: "paused");
    }
}

def setPlaybackTitle(text) {
	log.debug "Executing 'setPlaybackTitle'"
    
    sendEvent(name: "trackDescription", value: text)
}

def setPlaybackIcon(iconUrl) {
	log.debug "Executing 'setPlaybackIcon'"
    
    state.icon = iconUrl;
    
    //sendEvent(name: "scanNewClients", icon: iconUrl)
    //sendEvent(name: "scanNewClients", icon: iconUrl)
    
    log.debug "Icon set to " + state.icon
}

def playbackType(type) {
	sendEvent(name: "playbackType", value: type);
}