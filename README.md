[![Build Status](https://ligi.ci.cloudbees.com/job/Bluetooth%20Device%20Picker/badge/icon)](https://ligi.ci.cloudbees.com/job/Bluetooth%20Device%20Picker/)

Project which offers an action to pick a bluetooth device on Android. 
Exposes the action PICK_BLUETOOTH_DEVICE to start the BluetoothDeviceListActivity which scans for Bluetooth Devices, shows them in a ListActivity and returns device info when a device is selected by the user.
This is done to deduplicate code between APPs - the code is graduated from the DUBwise Project and used in the new DUBwise for UAVTalk APP. 
Features:
 - save picked devices ( no need to wait for scan next time )
 - continues scaning ( with marking the recently found ones )

The APP uses the lib tracedroid to be able to send debug logs when problems arise and android-bluetooth ( http://android-bluetooth.googlecode.com ) to be able to work with Android <2.0 which has no native Bluetooth-API
The code is GPLv3.


Credits:
 - Everaldo Coelho for the icon - http://commons.wikimedia.org/wiki/File:Crystal_Project_bluetooth.png
