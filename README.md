

                                  AI Backgammon Game (v0.0.1)


INTRODUCTION
------------------------------------------------------------------------------------------------

To run application:

$./gradlew run

Video: https://youtu.be/Azts7E8-UK4

Portable Backgammon is a Backgammon game split into 2 distinct sections:

- LOCAL PLAY - allows you to play against a friend at the same computer as you, or play against 
yourself or a (very dumb) computer opponent.
- NETWORK PLAY - allows you to meet a user in the chat lobby and challenge them to a game across
the internet 

*A quick note from the author added in 2014:

I developed this in 2009 and have just decided to release the code, because I remember looking hard for
some existing stuff when I wrote this. It has been designed with ease of porting in mind, I have actually
got an Android port somewhere, that took me literally an afternoon to get going using this source. 
The trick is to adjust only the HAL.java (High-level Abstraction Layer I think I called it :))
file and keep the others as is - this file is supposed to contain the specifics and the others are
meant to be generic and can be largely left alone.

Writing a backgammon game seems like an easy task, but is actually pretty tough, there's no doubt
plenty of bugs in here and stuff that could be improved, feel free to do that, I am pretty much
done with this project, which is why I have decided to release the code and see what happens!
If I recall this was delivered to my client and I said, hey test it see if theres any bugs theres
bound to be, and he refused to test it until it was "bug free". Ultimately I didn't get my pay because
I asked him to test his product and he refused, lol.. welcome to the world of freelancing, by the
way if you need an Android app developer/freelancer contact me here www.garethmurfin.co.uk 

Be sure to check out "additional things you can do" below for a list of keys you can press in game
to see the debug window and other things like Bot VS Bot, and how fast they think etc. As a final
note the one thing I'd love to see in this game is proper real hardcore Backgammon AI in my "bots"
who are currently dumb dumb creatures :)

Enjoy,
Gaz.


STATE OF DEVELOPMENT:
------------------------------------------------------------------------------------------------
Please note this program is still in the beta phase and has bugs in it, I am hoping the community can
help compile a list of bugs that we can fix. 

The local play is largely "bug free" but there is still some situations in which it behaves differently
to as expected (ie, wrong), hopefully those of you who are Gammon fans can play against each other,
yourself or the automated opponent to spot and list any flaws you find. 

Network play is new, and still very buggy.

For this reason we are only asking you to test Local Play, not network play. If you wish to go ahead
and try messing with network play please do and report back what happens, but as you will read
below its still very much in development, basically the chat lobby works but once linking up to
play another player it might well not quite work right now. Also there are server issues since
the Server machine is actually my home PC, and my IP could change, or tomcat might not be running etc,
once the local version is perfect and we have a server in place and working networked version we
will be asking people to focus on testing this next.

INSTALL:
------------------------------------------------------------------------------------------------
Currently you must download the ZIP archive, extract it and double click PortableBackgammon.jar
or runme.bat (Windows) 

if the game does not load then you must go and install the latest java from:
http://www.java.com/en/download/

Running problems?
The command to run the game is actually: java -jar PortableBackgammon.jar
if java is working on your system then this should work for mac/linux/windows in the command
line interface.

Would be great to hear how it works on all operating systems by the way.


LOCAL PLAY - HOW TO PLAY:
------------------------------------------------------------------------------------------------
Once you launch the Backgammon app you will see a splash screen briefly, wait until you see
"please select local play or network play" and click "local play". On the next screen it says
"play against computer or human" - choose what you wish here and the board appears.

White always has the opening roll to see who goes first (you). If you selected a computer
opponent then as soon as it is his/her turn they will automatically take control of a new
mouse pointer that pops up and carry out his moves until his turn is over. Buttons that
appear in the middle should be clicked, like "Roll" for instance to roll your dice.

additional things you can do: 
- The Double button does nothing yet
- Resign button resigns you (automatic lose)
- Press "T" to go through the available themes
- Q quits
- Press F1 and the window becomes resizable, so you can make it bigger/smaller, everything should
  scale to whatever size you use. press F1 again to lock window size (on some machines it flashes
  when in resizable mode hence the use of F1 to turn on off)
- S toggles sound
- F toggles FULL AUTO PLAY (ie computer vs computer) (in top right it shows the move he is going to carry out)
  If you want to get more adventurous in FULL AUTO PLAY mode then try pressing "D" this goes into DEBUG MODE 
  and should bring up a menu (this is for beta testers only and wont be in final release) from here you
  can adjust TIME_DELAY_BETWEEN_CLICKS which is the milliseconds the computer player waits after each move,
  if this is set very low (please note he may become erratic and erroneous if you go too low < 100) then he will
  delay less and the game will play out faster - to play it even faster press "J" this means the bot 
  jumps to his destination and doesnt have to move the mouse pointer there, with this option you can see 
  computer vs computer play at extremely high rate - this means you can follow the game play out and see 
  if it behaves correctly etc and doesnt crash or whatever - I have been testing myself by watching games 
  and playing them for many many many many hours (thats why we need your help!). 
  This auto play feature will also be used for a "demo" to show users a game in action at some point.
  (adjusting ROBOT_DELAY_AFTER_CLICKS has no affect now)
  
NETWORK PLAY - HOW TO PLAY:
------------------------------------------------------------------------------------------------
WARNING, network play is extremely buggy right now and probably is not worth testing yet unless you're
*really* into the idea of helping us out (local play testing is going to be more fruitful I guess), seeing as
a) we dont have a fixed IP for the server so it might not connect 
b) if you manage to start a game with someone, it will likely not work properly right now (still in 
development but it would be good if anyone can let me know if they are able to connect etc and what happens)

Once you launch the Backgammon app you will see a splash screen briefly, wait until you see
"please select local play or network play" and click "network play". Next a screen comes up for
entering your name, type your name in here and press enter.

PLEASE NOTE: You will see the chat server address at the bottom of the screen, the multiplayer
system actually works by speaking to a "Java Servlet" which is a program that resides on a 
server providing services, in this case it is providing the service of a chatroom so that players
can chat and locate each other then launch a direct connection between their clients to play the game.

This server is currently running on my own personal PC, and since the IP of this PC can change
the program grabs a small text file from my webserver telling it the IP of my machine then connects
to that - CURRENTLY this file can EASILY be out of date (like if my pc reconnects to the net and my
ip address changes, I have to manually edit the ip on my server to tell the client) and the 
networking simply wont work, we need to get the Servlet migrated onto a server with a static IP
address (or a redirector service) and the ability to run Servlets for this to work properly.

Now it will attempt to connect to the server, and identify your own IP address, if all goes
well you will appear in the Portable Backgammon chat lobby, with a list of users on the right
and the chat text in the middle. 

Chat text scrolls up in the main window, and new users appear on the right, currently everyones IP address
is tacked onto their username for various reasons. There is a user called ChanServ with a green dot next
to his name, he is the controller of the room (maybe he needs a snazzy Backgammon related name)
and will provide various services at some point (this is for a potental future version), such as:
 - kicking/banning naughty users (+potentially censoring swearing)
 - linking people who wish to play
 - providing broadcasts from admin
 - registering names/new channels
 - providing news about stuff
 - allowing people to leave "messages" for each other on the gammon network
 - requesting "Backgammon rank" in the scoreboard

 There is also a web version of the chat room (implemented as a Java Applet) so that Admin can enter
 the chat room (denoted by no IP tacked onto their name and a green or gold dot) to chat and help 
 users or remove users who are misbehaving (perhaps we will ask a few people to help us here by
 hanging out in the lobby as an admin from time to time to make sure its ok).


INTELLIGENCE
------------------------------------------------------------------------------------------------
When a game contains a Computer player users usually assume there is some level of intelligence in there 
controlling the actions, and this is often the case. With this game there is no intelligence at all, it 
is simply an automated player who knows all potential moves they can make and then simply selects one
at random, making him really quite dumb. Implementing intelligence is a huge huge task that is the basis
of a PHD alone in many respects especially if it is to beat decent players, so this has been left as
a future feature for now, with potential levels of intelligence being:
DUMB (current level), STUPID, AVERAGE, CRAFTY, GENIUS.


TECHNICAL DETAILS
------------------------------------------------------------------------------------------------

**CLIENT**

Client is written in Java, can be deployed as a "Webstart" application allowing it to run from a browser
or it can be downloaded and run as standalone program from desktop. The client supports all operating 
systems which have a Java run time environment, so good news for Mac and Linux users 
(this means a windows user can play a mac users etc with no problem for instance).

Programmer notes (networking):
Client grabs: http://www.alphasoftware.org/backgammon/news.txt for news string
and http://www.alphasoftware.org/backgammon/serverip.txt for server ip, then uses this ip to connect to the
chat servlet running on tomcat server at <server ip>, local files are located on server in: 
C:\apache-tomcat-6.0.18\webapps\ROOT\WEB-INF\classes\GammonChatServlet.class

**SERVER**

Web server currently in use is TOMCAT 6.0.18, this runs the servlet GammonChatServlet, which is
connected to by clients (who grab IP from serverip.txt) and also the web admin chat Applet

Programmer notes:
Run C:\apache-tomcat-6.0.18\bin\startup.bat to start tomcat (http://localhost:8080/ will become active)
Clients will grab ip from ipserver.txt then be able to connect.
http://localhost:8080/chat2/GammonChatApplet.html is admin web chat
