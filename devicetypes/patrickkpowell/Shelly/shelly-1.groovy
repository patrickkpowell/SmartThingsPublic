/**
 *  Shelly1
 *
 *  Copyright 2018 Patrick Powell
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
  definition (name: "Shelly1", namespace: "patrickkpowell", author: "Patrick Powell") {
        capability "switch"
        attribute "ip", "string"
  }


  simulator {
    // TODO: define status and reply messages here
  }

  tiles(scale: 2) {
    // standard tile with actions named
    standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
      state "off", label: '${currentValue}', action: "switch.on",
        icon: "st.switches.switch.off", backgroundColor: "#ffffff"
          state "on", label: '${currentValue}', action: "switch.off",
            icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
      }

      // the "switch" tile will appear in the Things view
      main("switch")
    }
    
    preferences {
        input("ip", "string", title:"Address", description:"Please enter your Shelly's I.P. address", defaultValue:"" , required: false, displayDuringSetup: true)
    }
}

def parse(description) {
    log.debug "Parsing result"
    def msg = parseLanMessage(description)

    def headersAsString = msg.header // => headers as a string
    def headerMap = msg.headers      // => headers as a Map
    def body = msg.body              // => request body as a string
    def status = msg.status          // => http status code of the response
    def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
    def xml = msg.xml                // => any XML included in response body, as a document tree structure
    def data = msg.data              // => either JSON or XML in response body (whichever is specified by content-type header in response)
    log.debug data.ison

    log.debug "Message Headers String "+headersAsString
    log.debug "Message Headers Map "+headerMap
    log.debug "Body "+body
    log.debug "Status "+status
    log.debug "JSON "+json
    log.debug "XML "+xml
    log.debug "Data "+data
    
    if ( data.ison == true ) {
      log.debug "TRUE"
      sendEvent(name: "switch", value: "on", isStateChange: true, displayed: false)
    }
    if ( data.ison == false ) {
      log.debug "FALSE"
      sendEvent(name: "switch", value: "off", isStateChange: true, displayed: false)
    }
}

// handle commands
def off() {
  log.debug "Executing 'off'"
    toggleRelay "turn=off"
}

def on() {
  log.debug "Executing 'on'"
    toggleRelay "turn=on"
}

def toggleRelay(action) {
  sendHubCommand(new physicalgraph.device.HubAction(
    method: "POST",
    path: "/relay/0",
    body: action,
    headers: [
      HOST: getHostAddress(),
      "Content-Type": "application/x-www-form-urlencoded"
    ]
  ))
  result
}

private getHostAddress() {
  log.debug "Using IP: $ip  and PORT: 80 for device: ${device.id}"
  device.deviceNetworkId = convertIPtoHex(ip)+":"+convertPortToHex(80)
  log.debug device.deviceNetworkId
  return ip+":80"
}

private String convertIPtoHex(ipAddress) { 
   String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
   log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
   return hex

}

private String convertPortToHex(port) {
   String hexport = port.toString().format( '%04x', port.toInteger() )
   log.debug hexport
   return hexport
}

def updated() {
   log.debug "updated()"
   unschedule()
   runEvery1Minute("poll")
   poll()
}

def refresh() {
  getStatus()
}

def poll() {
  getStatus()
}

def getStatus() {
  log.debug "Polling"
  sendHubCommand(new physicalgraph.device.HubAction(
    method: "GET",
    path: "/relay/0",
    headers: [
      HOST: getHostAddress(),
      "Content-Type": "application/x-www-form-urlencoded"
    ]
  ))
}

def push() {
  sendEvent(name: "switch", value: "on", isStateChange: true, displayed: false)
  sendEvent(name: "switch", value: "off", isStateChange: true, displayed: false)
  sendEvent(name: "momentary", value: "pushed", isStateChange: true)
}