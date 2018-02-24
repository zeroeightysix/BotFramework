# BotFramework
Advanced minecraft bot framework for in-chat commands, features & more, the only limit is your creativity!
Entirely made in java.

## Building
The framework is built using gradle. Simply clone the project and use `gradlew build` to build it.

## For users
Botframework functions a lot like a bukkit server. It'll generate a plugins folder, load any plugins (bots) from there and connect to a server.
All you have to do is put the desired plugins in the plugins folder, fire up botframework and you're ready to go.

## For developers
You'll be making what's known as a `plugin`. Plugins are basically the **content** of your bot.
This includes WHAT your commands do, WHICH commands there are etc..
The framework handles everything such as authentication, chat processing, connection etc, and you do the rest.

# Getting started

## For users
Botframework doesn't have a GUI, and is entirely log-based. This means you'll have to use a command prompt to run it.
Create a `.bat` file in the same directory as your botframework jar. In it, use java to run the framework like so:
`java -jar BotFramework.jar` (Note: change `BotFramework.jar` to what your jar is called)
However, this will get angry at you for missing some information. We have to append:
 - The server to connect to
 - Your credentials
   - The username or email to authenticate with
   - The password to log in with
 - Any additional parameters (reference [Parameters](#Parameters))
 
 We define them this way:
 `java -jar BotFramework.jar -username <name> -password <password> -ip <ip>`
 
 ### Parameters
 
  - Proxy `-proxy` Proxy to connect through
  - Proxy port `-proxyport` Proxy port to connect through
  - Debug mode `-debug` Enables debug mode (more information)
  - IP `-ip` IP to connect to
  - Port `-port` Port to connect through
  - Verify users `-verifyusers` Force authentication. Will disable use of cracked accounts.
  - Skip connection test `-skiptest` Will disable checking for server availability before joining
  - Locale `-locale` Loads another locale. Default `en_us`
  
## For developers

### Prerequisites for the framework to load your plugin
 - A package with name `META-INF.services` containing a file named `me.zeroeightsix.botframework.plugin.Plugin`
   - File contains path to your plugins main class
 - A main class extending `me.zeroeightsix.botframework.plugin.Plugin`
 
### Getting started with your main class
Documentation not yet made :(
Check the example if it's up yet?
