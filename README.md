Chat Application
===

## What is it?

This a desktop chat application.

Yes. That's it.

You've known how to use it without even taking a glance at [User & Operator Guide](#user--operator-guide) below since it is cut from the same cloth as other chat applications like <img src="https://lh3.googleusercontent.com/74iMObG1vsR3Kfm82RjERFhf99QFMNIY211oMvN636_gULghbRBMjpVFTjOK36oxCbs=s360-rw" width="18"> or <img src="https://lh3.googleusercontent.com/rkBi-WHAI-dzkAIYjGBSMUToUoi6SWKoy9Fu7QybFb6KVOJweb51NNzokTtjod__MzA=s360-rw" width="20">.

Nonetheless, it is worthwhile to look at [System & Program Design](#system--program-design), in which I will bring you into details about how I build the system, why offline browsing is possible, how the server handles concurrency issue,  what will happen if multiple devices log in with same account, how the application sends any type of the file, and so forth.

## Table of Contents
- [What is it?](#what-is-it)
- [Instructions on How to Run Server & Clients](#instructions-on-how-to-run-server--clients)
- [User & Operator Guide](#user--operator-guide)
- [System & Program Design](#system--program-design)
  - [Architecture of Client Side](#architecture-of-client-side)
  - [Schema of Whole Project](#schema-of-whole-project)
  - [Asynchronous Message](#asynchronous-message)
  - [Non-Blocking Synchronous File Transfer](#non-blocking-synchronous-file-transfer)

## Instructions on How to Run Server & Clients

1. Install [Java SE Development Kit 11](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
2. For Server side, execute the command below:
```
$ cd Server
$ ./compile.sh
$ ./run.sh
```
3. For Client side, execute the command below:
```
$ cd Client
$ ./compile.sh
$ ./run.sh
```
4. Do not run multiple clients in single Client directory, because I have implemented local cache. If you want to run multiple clients, please copy entire directory to other place and then follow the steps in 3rd instruction.

## User & Operator Guide

### Login

This is the starting page of the program. A user can log in only when he/she has registered an account. "Keep log in" check box enables automatic login when the user opens the chat application next time.

<img src="https://i.imgur.com/owitALG.png" width="500">

### Register

User can register a new account here. Note that the registration could fail if the user entered a user name of an existing account or password is not confirmed.

<img src="https://i.imgur.com/qVtscMD.png" width="500">

### Home

If login successfully, the program would direct the user to the home page which shows profile and friend list: the tab Profile shows the username and password (lol), and the Friends tab would show the friends you currently have.

<img src="https://i.imgur.com/iDJnQk2.png" width="300">

### Add Friend
Pressing the third button of the bar on the left would direct you to add friend page. You can enter an existing username in order to add that user. If the user name doesn’t exist or the user tries to add himself/herseld, the error message would be shown.

<img src="https://i.imgur.com/pbIWmIM.png" width="400">

### Friend List

After adding a friend successfully, you will see your friend’s name appearing on Friend List. If you wants to chat with a friend, you simply click the name of the friend.

<img src="https://i.imgur.com/LVHfxY9.png" width="600">

<img src="https://i.imgur.com/vrGrPq4.png" width="600">

### Chat Room

The users can start chatting now. The send button is on the right-down corner of the chat box. Press it to send the message you filled. Each message would contain the information of sending time. Note that all messages would be saved in the server. Whenever the user opens the chat room, the chat history would keep up to date.

<img src="https://i.imgur.com/oD7k4nU.png" width="600">

### Offline

If you are not connect to network, the bar on the top will appear to inform you. You can still use the application except sending message and adding friends. The application would try reconnecting automatically.

<img src="https://i.imgur.com/IYMF04l.png" width="600">

### Message Button

The buttons on the Chats and Friend List page will show the latest message from each friend. Moreover, the icon for message button would be changed to a user button when the user enters the Chats page. 

<img src="https://i.imgur.com/z3RMIm5.png" width="600">

### Send File

Put the file you wants to send in the “data” folder under your client folder, and press the paper clip button on the left corner after filling the file name. The file will be sent to your friend’s “data” folder under his/her client folder.

<img src="https://i.imgur.com/knxyLpF.png" width="600">

### Logout

You can log out by pressing the last button of the bar on the left, and you would be redirected to the Login page.

<img src="https://i.imgur.com/hYcjUt8.png" width="400">

## System & Program Design

### Architecture of Client Side
Bearing [model-view-controller design pattern](https://en.wikipedia.org/wiki/Model-view-controller) in mind, I divide the related program logic in application of client side (abbreviated as “Chat Application” or “Chat App” in remaining article for convenience) into three interconnected components:
* **Model**: Local Cache
* **View**: Graphical Interface, which is built with Java Swing
* **Controller**: Proxy Server

<img src="https://i.imgur.com/HZVME40.png" width="600">

There are also three rules for Proxy Server:
1. Upon user requested (Graphical User Interface event), Proxy Server always takes data from Local Cache first.
2. If Chat App is online, Proxy Server keeps Local Cache up to date.
3. If Chat App is offline, Proxy Server pushes requests into queue until it gets online.

Whether Chat App is online or not is determined by the state of Client Socket.

You may discover that my application can be well-functioning despite it does not connect to Real Server. It virtually takes care of all requests from user. That’s why I call it Proxy Server.

### Schema of Whole Project

In this schema, server provides service with thread per client. To deal with concurrency control, I furthermore build a database controller to ensure log and data are correct. Honestly speaking, however, this sometimes gets the program into deadlock.

![](https://i.imgur.com/Ay8XpbE.png)

Because Server processes request from multiple Chat Apps simultaneously. In order to be interactive, Server maintains a queue as well. When a user's request arrives at Server, it is first pushed into queue. The Client Handler fetches the request from the queue and throw it to Client. The Server protects the queue to avoid competition for shared access by locking the resources.

On the other hand, the request and its implementation are packaged into [Strategy objects](https://en.wikipedia.org/wiki/Strategy_pattern), so Client Handler does not need to know the services provided by the Client when it throws the request.

<img src="https://i.imgur.com/cbeOspl.png" width="400">

### Asynchronous Message

Considering the Client Handler List of current users as a “subject” in the [Observer pattern](https://en.wikipedia.org/wiki/Observer_pattern), I establish “observers” for each Chat Apps connected to the Server. Subjects are subscribed by multiple observers. When a subject's state changes, it must notify all observers who subscribe to it. Observers take actions depending on the state of the subject.

The diagram below shows an example on how a message is sent from one to the other.

![](https://i.imgur.com/hVC2kF9.png)

1-4: One receiving the request from Chat App A, Server tries to write logs into Database and sends ACK back to Chat App A if it successes. Client A may reject or defer the request due to concurrency control.

5: Client Handler A calls upon other Client Handlers that there is a new message from User A to User B. Note that it notifies all Client Handlers corresponding to either User A or User B. This is because my Chat App supports single user logging in with multiple machines (e.g. log in with laptop and smartphone simultaneously).

6-7: Server sends the message to Chat App B. And then, the Proxy Server in Chat App of B takes the job.

If B is offline while A is sending message, B will keep up to date whenever it gets online in the future.

### Non-Blocking Synchronous File Transfer

Since the file may be pretty large, I cannot adopt normal I/O stream policy of socket, which works well for messaging, for file transfer. Thus, I alternatively use non-blocking I/O for file transfer.

Besides, it seems impossible to have two I/O policies in single TCP channel. I create a new file channel for this transmission. Once any user wants to send files, the server would pair two Client Handlers and clear the way for file transfer. The diagram below shows an example that User A sends a file to User B.

![](https://i.imgur.com/VekvLyY.png)

Note that both User A and User B must be online. If one of them is offline during transmission, the transmission fails and no log is written into database at all. That’s why it is a synchronous transfer.
