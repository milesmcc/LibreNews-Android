# How LibreNews Protects Your Privacy
LibreNews does not track you, your device, or your enabled channels in any way. Furthermore, LibreNews takes the following steps to prevent your devices from being tracked by others:

* Requiring HTTPS on all server connections, to prevent snooping and spoofing
* Using Android's 'alarm clock fuzzing' to foil timing-based association attacks on your device [1]
* Using only your phone's internal storage for data storage, preventing other apps from reading the information
* Being 100% open source (making an independent security audit possible)

<small>[1] Because LibreNews causes your device to periodically send out 'pings' to the notification server, an attacker could theoretically track your device by following any devices which send out a ping at the same frequency to the same IP, even if you were using a VPN. To address this type of attack, LibreNews includes a bit of 'fuzzing' when pulling new notifications from the server (i.e. a 2 minute refresh time could be 1:50, 2:03, etc), making this attack vector significantly less feasable.

While the LibreNews server tracks the number of unique IPs it has received notification requests from, this information is displayed clearly on the server homepage and is not logged. Furthermore, the count is reset every 24 hours on the default notification server.
