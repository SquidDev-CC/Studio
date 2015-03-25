# CC-Studio [![Build Status](https://travis-ci.org/SquidDev-CC/Studio.svg?branch=master)](https://travis-ci.org/SquidDev-CC/Studio)
The CC Emulator for devlopers

## Why?
I pretty much rely on just emulators, and many wonderful emulators exists, 
such as [CCEmuRedux](http://www.computercraft.info/forums2/index.php?/topic/18789-ccemuredux-development/).

However all existing emulators have one problem, they are as unfriendly to developers as CC is itself, 
no `debug` API, `Too long without yielding errors` and all other issues occur.

The point of CC-Studio is to emulate CC as closely as possible, whilst still allowing freedom
to experiment.

## What?
I'm planning to implement.

 - Support for `debug` API.
 - Customisable `Too long without yielding`. This will support displaying an error message,  but not terminating the program. ([From this thread](http://www.computercraft.info/forums2/index.php?/topic/20535-additional-config-options/))
 - Optional `org.luaj.vm2.luajc.LuaJC` compiler. ([From this thread](http://www.computercraft.info/forums2/index.php?/topic/21489-performance-increaseswitching-compiler/))
 - Peripherals - Probably wireless rednet
 - Plugin API. This is mostly so it is easier to add peripherals, but also would allow support for better utilities, such as a profiler.
