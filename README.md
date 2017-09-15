# Drain-Blame
Android app for identifying apps which drain the battery.

Spawns a background service which collects data on lengths of 1% battery intervals and which apps were active during them, and uses this information to classify apps as being *high-*, *medium-*, or *low-drain*.

## Architecture Overview
On startup, `MainActivity` is displayed, performs initial setup:
* Gets a reference to `ApplicationGlobals` - global static for referencing main app components; on first call, instantiates the main components of the background monitor.
* Sends start command to `MainService` (command ignored if service is already running) - the background service. Items in `ApplicationGlobals` are separate from this to allow interval data to be accessed when monitor service is disabled. Service creates a notification to avoid being killed.

`MainService` also registers receivers for battery and screen state. `BatteryReceiver` registered for `ACTION_BATTERY_CHANGED` events. When event fires, checks whether it was relevant to Drain Blame, and if so calls appropriate method in `IntervalHandler`.

`IntervalHandler` monitors battery intervals. When battery level changes, saves data for previous interval, starts recording current one. Data saved as `Interval` object.

Recording intervals has a special case to cope with partial length intervals (interval immediately following charger disconnection, or starting from 100% battery) - starts polling processes to build up list of active apps, but doesn't record interval data.

Within each interval, active processes are polled: A `ProcessPoller` is created which calls method of `ProcessHandler` every 30 seconds (the polling interval).
`ProcessHandler` parses the `/proc` directory, records all running processes and their CPU tick info.

When a new process is discovered, `AppHandler` is called to get a reference to an `App` object for that process (multiple processes can share the same `App`), based on the process name.

Every time the `/proc` directory is parsed, any new CPU ticks for each process are attributed to their `App` objects; processes restarting with the same name are detected using the `starttime` entry in the `/proc/[pid]/stat` file.

At end of each interval, `IntervalHandler` gets list of active apps from `AppHandler` and resets all CPU tick values in preparation for the next interval. The list of active apps is saved in the `Interval` object, along with other data on that interval.


Every time `MainActivity` is displayed, the `Classifier` is called, being passed the list of recorded intervals. If the energy characteristics of any apps could be classified, these are then displayed.

## File Parsing
Interval data is stored to a file in the app directory: `Android/data/com.moss.drainblame/files/intervalData`. Data is saved periodically (called in `IntervalHandler`), on app startup (`ApplicationGlobals`), and on background service shutdown. It can also be read or written from the options menu in `MainActivity`.

### File structure:
First line is version number; if version number doesn't match, file will not be read. Each subsequent line is one battery interval. Data fields within each interval are separated by a single space:
* Battery level
* Interval length
* Screen on time
* Total network bytes transmitted/received
* List of active apps: App name, CPU ticks, flag (set to true if this is a process for which an app could not be identified)

e.g.

```
6
81 2307933 0 2201173 com.google.android.gms 1495 false com.facebook.katana 544 false
80 2832219 3630 130510 com.facebook.katana 650 false
```

## To Do
* Improve classification algorithm; probably by collecting data on app activities within each 30-second sample interval, rather than whole battery intervals.
* Determine CPU tick threshold for active apps on a per-device basis, rather than as a hardcoded value - probably by running a benchmark and measuring the CPU ticks used.
* Add support for newer versions of Android, which block access to the `/proc` directory; make use of the `UsageStatsManager`.
