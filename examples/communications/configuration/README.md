# Module examples_communications_configuration

## Example communication configuration

This is an example app demonstrating how the communication frontend can be configured using static 
configuration resources.
By changing the values in [example-communication-configuration.xml] different configurations can
be changed.

# Setup

Please follow the guide [Installing TomTom Indigo on Hardware](/tomtom-indigo/documentation/integrating-tomtom-indigo/installing-tomtom-indigo-on-hardware) before installing this app.

# Setting this example app to be the system dialer

To be able to use this example app the system dialer need to be set to package name. 
By entering the following commands this will happen:

```cmd
adb shell telecom set-system-dialer com.example.ivi.example.communication.configuration/com.tomtom.ivi.platform.telecom.plugin.service.telecom.IviInCallService
```
