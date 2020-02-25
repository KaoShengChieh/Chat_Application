## Instructions on how to run server & clients

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
4. Do not run multiple clients in single Client directory, because we have implemented local cache. If you want to run multiple clients, please copy entire directory to other place and then follow the steps in 3rd instruction.

## User & Operator Guide

### Login

This is the starting page of the program. A user  can log in only when he/she has registered an account. "Keep log in" check box enables automatic login when the user opens the chat application next time.

![](https://i.imgur.com/owitALG.png)
---

### Register

User can register a new account here. Note that the registration could fail if the user entered a user name of an existing account or password is not confirmed.

![](https://i.imgur.com/qVtscMD.png)
---

### Home

If login successfully, the program would direct the user to the home page which shows profile and friend list: the tab Profile shows the username and password (lol), and the Friends tab would show the friends you currently have.

![](https://i.imgur.com/iDJnQk2.png)

### Add Friend
Pressing the third button of the bar on the left would direct you to add friend page. You can enter an existing username in order to add that user. If the user name doesn’t exist or the user tries to add himself/herseld, the error message would be shown.

![](https://i.imgur.com/pbIWmIM.png)

### Friend List

After adding a friend successfully, you will see your friend’s name appearing on Friend List. If you wants to chat with a friend, you simply click the name of the friend.

![](https://i.imgur.com/LVHfxY9.png)

![](https://i.imgur.com/vrGrPq4.png)
---

### Chat Room

The users can start chatting now. The send button is on the right-down corner of the chat box. Press it to send the message you filled. Each message would contain the information of sending time. Note that all messages would be saved in the server. Whenever the user opens the chat room, the chat history would keep up to date.

![](https://i.imgur.com/oD7k4nU.png)
---

### Offline


![](https://i.imgur.com/IYMF04l.png)
---

### Message Button

The buttons on the Chats and Friend List page will show the latest message from each friend. Moreover, the icon for message button would be changed to a user button when the user enters the Chats page. 

![](https://i.imgur.com/z3RMIm5.png)
---

### Send File

Put the file you wants to send in the “data” folder under your client folder, and press the paper clip button on the left corner after filling the file name. The file will be sent to your friend’s “data” folder under his/her client folder.

![](https://i.imgur.com/knxyLpF.png)
---

### Logout

You can logout by pressing the last button of the bar on the left, and you would be redirected to the Login page.

![](https://i.imgur.com/72xjuga.png)
---


## System & Program Design

### Architecture of Client Side
Bearing [model-view-controller design pattern](https://en.wikipedia.org/wiki/Model-view-controller) in mind, we divide the related pro-gram logic in application of client side (abbreviated as “Chat Application” or “Chat App” in the rest of report for convenience) into three interconnected components:
1. Model: Local Cache
2. View: Graphical Interface, which is built with Java Swing
3. Controller: Proxy Server

![](https://i.imgur.com/HZVME40.png)

There are also three rules for Proxy Server:
1. Upon user requested (Graphical User Interface event), Proxy Server always takes data from Local Cache first.
2. If Chat App is online, Proxy Server keeps Local Cache up to date.
3. If Chat App is offline, Proxy Server pushes requests into queue until it gets online.

Whether Chat App is online or not is determined by the state of Client Socket.

You may discover that our application can be well-functioning despite it does not connect to Real Server. It virtually takes care of all requests from user. That’s why we call it Proxy Server.

### Schema of Whole Project

This schema is adapted from our project 1 “TCP ping”, in which server provides service with thread per client. To deal with concurrency control, we furthermore build a database controller to ensure log and data are correct. Honestly speaking, however, this sometimes gets our program into deadlock.

![](https://i.imgur.com/Ay8XpbE.png)

Because Server processes request from multiple Chat Apps simultaneously. In order to be interactive, Server maintains a queue as well. When a user's request arrives at Server, it is first pushed into queue. The Client Handler fetches the request from the queue and throw it to Client. The Server protects the queue to avoid competition for shared access by locking the resources.

On the other hand, the request and its implementation are packaged into [Strategy objects](https://en.wikipedia.org/wiki/Strategy_pattern), so Client Handler does not need to know the services provided by the Client when it throws the request.

![](https://i.imgur.com/cbeOspl.png)

### Asynchronous Message

We consider the Client Handler List of current users as a “subject” in the [Observer pattern](https://en.wikipedia.org/wiki/Observer_pattern). We establish “observers” for each Chat Apps connected to the Server. Subjects are subscribed by multiple observers. When a subject's state changes, it must notify all observers who subscribe to it. Observers take actions depending on the state of the subject.

The diagram in next page shows an example on how a message is sent from one to the other.

![](https://i.imgur.com/hVC2kF9.png)

1-4: One receiving the request from Chat App A, Server tries to write logs into Database and sends ACK back to Chat App A if it successes. Client A may reject or defer the request due to concurrency control.

5: Client Handler A calls upon other Client Handlers that there is a new message from User A to User B. Note that it notifies all Client Handlers corresponding to either User A or User B. This is because our Chat App supports single user logging in with multiple machines (e.g. log in with laptop and smartphone simultaneously).

6-7: Server sends the message to Chat App B. And then, the Proxy Server in Chat App of B takes the job.

If B is offline while A is sending message, B will keep up to date whenever it gets online in the future.

### Non-Blocking Synchronous File Transfer

Since the file may be pretty large, we cannot adopt normal I/O stream policy of socket, which works well for messaging, for file transfer. Thus, we alternatively use non-blocking I/O for file transfer.

Besides, it seems impossible to have two I/O policies in single TCP channel. We create a new file channel for this transmission. Once any user wants to send files, the server would pair two Client Handlers and clear the way for file transfer. The diagram below shows an example that User A sends a file to User B.

![](https://i.imgur.com/VekvLyY.png)

Note that both User A and User B must be online. If one of them is offline during transmission, the transmission fails and no log is written into database at all, which result in synchronous transfer.

