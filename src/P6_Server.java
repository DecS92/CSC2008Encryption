import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;

public class P6_Server extends JFrame
{   JTextArea outputArea;
	ServerSocket serverSocket;

	public P6_Server()
	{	super("P6_Server");
		addWindowListener
		(	new WindowAdapter()
			{	public void windowClosing(WindowEvent e)
				{	System.exit(0);
				}
			}
		);
		Container c = getContentPane();
		c.setLayout(new FlowLayout());

		try 
		{	// get a server socket and bind to port 6000
			serverSocket = new ServerSocket(6000);
		}
		catch(IOException e) // thrown by method ServerSocket
		{	System.out.println(e);
    		System.exit(1);
		}

		// create and add GUI components
		outputArea = new JTextArea(15,30);
		outputArea.setEditable(false);
		outputArea.setLineWrap(true);
		outputArea.setWrapStyleWord(true);
		c.add(outputArea);
		c.add(new JScrollPane(outputArea));

		setSize(350,300);
		setResizable(false);
		setVisible(true);
	}

	void addOutput(String s)
	{	// add a message to JTextArea
		outputArea.append(s + "\n");
		outputArea.setCaretPosition(outputArea.getText().length());
	}

	void waitForData()
	{	try
		{	addOutput("Server is up and waiting for a connection...");

			/* client has attempted to get a connection to server, now create a socket
			   to communicate with this client */
			Socket client = serverSocket.accept();

			// get input and output streams
			ObjectInputStream serverInputStream = new ObjectInputStream(client.getInputStream());
			ObjectOutputStream serverOutputStream = new ObjectOutputStream(client.getOutputStream());

			// read encrypted username and password
			EncryptedMessage uname = (EncryptedMessage)serverInputStream.readObject();
			EncryptedMessage pword = (EncryptedMessage)serverInputStream.readObject();

			// output encrypted username and password
			addOutput("\nLogin Details Received\n----------------------------------------");
			addOutput("encrypted username : " + uname.getMessage());
			addOutput("encrypted password : " + pword.getMessage());

			// decrypt username and password
			uname.decrypt();
			pword.decrypt();

			// output decrypted username and password
			addOutput("decrypted username : " + uname.getMessage());
			addOutput("decrypted password : " + pword.getMessage());

			// arrays containing valid names and passwords
   			String[] names = {"mark", "joan", "bill", "jack"};
			String[] passwords = {"frequency", "secret", "row", "cryptography"};

			boolean valid = false;
			for(int i = 0; i < names.length; i++)
			{	/* test whether corresponding username and password is a match to
			       username and password received from client */
				if(names[i].equals(uname.getMessage()) && passwords[i].equals(pword.getMessage()))
					valid = true;
			}

			if(!valid)
			{	/* if a valid username and password are not found send Boolean value
			       false to client to indicate that they have not logged on to game */
				serverOutputStream.writeObject(new Boolean(false));
				addOutput("Logon denied - Boolean value false send to client");
			}
			else
			{	/* if a valid username and password are found send Boolean value true
			       to client to indicate that they have logged on to game */
				serverOutputStream.writeObject(new Boolean(true));
				addOutput("Logon successful - Boolean value true send to client");
				addOutput("\nGames Rules\n----------------------------------------");

				// game rules
				String gameRules = "On each turn you roll two dice. Each die has six faces, "
						+ "which contain one, two, three, four, five and six spots, respectively. "
						+ "After the dice have come to rest, the sum of the spots on the two upward "
						+ "faces is calculated. If, on the first throw, the sum is seven or eleven, "
						+ "you will win, however if the sum is two, three or twelve, you will lose "
						+ "and the server will win. If the sum is four, five, six, eight, nine or ten "
						+ "on the first throw, that sum becomes your point. To win, you must continue "
						+ "to roll the dice until you roll the same point value. If you roll a seven "
						+ "before making your point you will lose.";

				// send the game rules to client
				serverOutputStream.writeObject(gameRules);
				addOutput("\nGame rules sent to client\n\nGame in play\n--------------------------");

				// start game
				int roll = 1, point = -1;
				boolean gameOver = false;

				while(!gameOver)
				{	// read value of first die
					int die1 = (Integer)serverInputStream.readObject();
					// read value of second die
					int die2 = (Integer)serverInputStream.readObject();
					addOutput("From client : " + die1 + "-" + die2);

					// add dice
					int sum = die1 + die2;

					String result = "You rolled: " + sum;

      			// if this is the first roll
      			if(roll == 1)
      			{	switch(sum)
     				{	// if the total is 7 or 11 the client wins instantly
     					case 7:
         				case 11:
         						result+= " - You Win!";
         						gameOver = true;
         						break;
         				// if the total is 2, 3 or 12 the server wins instantly
         				case 2:
         				case 3:
         				case 12:
         						result += " - Server Wins!";
         						gameOver = true;
         						break;
         				// if the total is 4, 5, 6, 8, 9, 10 this is the client's point
         				default:
         						result += " - You must match this point to win.";
         						point = sum;
         			}
      			}
      			else
      			{	// this is not the first roll
      				if(sum == point)
      				{	// if the total equals the client's point they win
      					result += " - You have matched your point - You Win!";
      					gameOver = true;
      				}

      				if(sum == 7)
      				{	// if the total is 7 the server wins
      					result += " - You have failed to match your point - Server Wins!";
      					gameOver = true;
      				}
      			}
      				// increment rolls
					roll++;
					// send message to client
					serverOutputStream.writeObject(result);
					addOutput("Message sent to client : " + result);
				}
			}
			addOutput("GAME OVER");
			// close input & output streams
			serverInputStream.close();
			serverOutputStream.close();
			// close socket
			client.close();
		}
		catch(IOException e) // thrown by method readObject, writeObject
		{	System.out.println(e);
			System.exit(1);
		}
		catch(ClassNotFoundException e) // thrown by method readObject
		{	System.out.println(e);
			System.exit(1);
		}
	}

	public static void main(String args[])
	{	P6_Server gameServer = new P6_Server();
		gameServer.waitForData();
	}
}