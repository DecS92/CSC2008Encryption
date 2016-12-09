import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.Random;

public class P6_Client extends JFrame
{   // variables for the GUI components of the game
	Container c;
	// this variable will be instantiated to an object of inner private class ButtonHandler
   	ButtonHandler bHandler;
   	JButton logonButton, startButton, rollButton;
   	JPanel gamePanel, buttonPanel, outputPanel, logonFieldsPanel, logonButtonPanel;
   	JLabel usernameLabel, passwordLabel, dieImage1, dieImage2;
   	JTextArea outputArea;
   	JTextField username;
   	JPasswordField password;
   	ImageIcon[] dice = {new ImageIcon("blank.gif"), new ImageIcon("die_1.gif"), new ImageIcon("die_2.gif"), new ImageIcon("die_3.gif"), new ImageIcon("die_4.gif"), new ImageIcon("die_5.gif"), new ImageIcon("die_6.gif")};

   	// socket to communicate with server
  	Socket socket;
  	// input and output streams for sending objects to and receiving objects from the server
	ObjectInputStream clientInputStream;
	ObjectOutputStream clientOutputStream;

   	public P6_Client() 
   	{	super("P6_Client");
   		addWindowListener
		(	new WindowAdapter()
			{	public void windowClosing(WindowEvent e)
				{	System.exit(0);
				}
			}
		);
		// create and add GUI components
   		c = getContentPane();
   		c.setLayout(new BorderLayout());

      	/* the initial GUI will provide a text field and password field to
      	   enable the user to enter their username and password and attempt to
      	   logon to the game system */
      	logonFieldsPanel = new JPanel();
		logonFieldsPanel.setLayout(new GridLayout(2,2,5,5));
		usernameLabel = new JLabel("Enter Username: ");
		logonFieldsPanel.add(usernameLabel);
		username = new JTextField(10);
		logonFieldsPanel.add(username);

		passwordLabel = new JLabel("Enter Password: ");
		logonFieldsPanel.add(passwordLabel);
		password = new JPasswordField(10);
		logonFieldsPanel.add(password);
		c.add(logonFieldsPanel,BorderLayout.CENTER);

		logonButtonPanel = new JPanel();
		logonButton = new JButton("logon");
		bHandler = new ButtonHandler();
		logonButton.addActionListener(bHandler);
		logonButtonPanel.add(logonButton);
		c.add(logonButtonPanel, BorderLayout.SOUTH);

		setSize(300,125);
		setResizable(false);
		setVisible(true);
   	}

   	void addOutput(String s)
	{	// add a message to the text output area
		outputArea.append(s + "\n");
		outputArea.setCaretPosition(outputArea.getText().length());
	}

   	void setUpRules(boolean loggedOn, String mess)
	{	// remove iniial GUI components (textfield, password field, logon button)
		c.remove(logonFieldsPanel);
		c.remove(logonButtonPanel);

		outputPanel = new JPanel();
		outputPanel.setBackground(Color.WHITE);
		// add text area
		outputArea = new JTextArea(16,30);
		outputArea.setEditable(false);
		outputArea.setLineWrap(true);
		outputArea.setWrapStyleWord(true);
		outputArea.setFont(new Font("Verdana", Font.BOLD, 11));
		// add message to text area
		addOutput(mess);
		outputPanel.add(outputArea);
		outputPanel.add(new JScrollPane(outputArea));
		c.add(outputPanel, BorderLayout.NORTH);

		if(!loggedOn)
			// if logon denied close input and output streams and socket
			closeStreams();
		else
		{	// if logon successful add blank dice images
			gamePanel = new JPanel();
      		gamePanel.setBackground(Color.WHITE);
			gamePanel.setLayout(new GridLayout(1,2));
      		dieImage1 = new JLabel(dice[0], SwingConstants.CENTER);
      		dieImage2 = new JLabel(dice[0], SwingConstants.CENTER);
      		gamePanel.add(dieImage1);
      		gamePanel.add(dieImage2);
      		c.add(gamePanel, BorderLayout.CENTER);
			// add button to remove game rules (and start game)
			buttonPanel = new JPanel();
			buttonPanel.setBackground(Color.WHITE);
			startButton = new JButton("start game");
			startButton.addActionListener(bHandler);
			buttonPanel.add(startButton);
			// add roll button
			rollButton = new JButton("roll dice");
			rollButton.addActionListener(bHandler);
			rollButton.setEnabled(false);
			buttonPanel.add(rollButton);
			c.add(buttonPanel, BorderLayout.SOUTH);
		}
		setSize(400,425);
		setResizable(false);
		setVisible(true);
	}

	void startGame()
	{	// game begins - add initial roll message
		outputArea.setText("Make your first roll.\n");
		startButton.setEnabled(false);
		rollButton.setEnabled(true);
	}

   	void getConnections()
	{	try
		{	// initialise a socket and get a connection to server
			socket = new Socket(InetAddress.getLocalHost(), 6000);

			// get input and output streams
			clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
			clientInputStream = new ObjectInputStream(socket.getInputStream());
		}
		catch(UnknownHostException e) // thrown by method getLocalHost
		{	System.out.println(e);
			System.exit(1);
		}
		catch(IOException e) // thrown by methods getInputStream, getOutputStream
		{	System.out.println(e);
			System.exit(1);
		}
	}

	void sendLoginDetails()
	{	try
		{	// get username from text field and encrypt
			EncryptedMessage uname = new EncryptedMessage(username.getText());
			uname.encrypt();

			// get password from password field and encrypt
			EncryptedMessage pword = new EncryptedMessage(new String (password.getPassword()));
			pword.encrypt();

			// send encrypted username and password to server
			clientOutputStream.writeObject(uname);
			clientOutputStream.writeObject(pword);
			/* read Boolean value sent by server - it is automatically converted to
			  a primitive boolean value */
			boolean loggedOn = (Boolean)clientInputStream.readObject();

			String mess;
			if(loggedOn)
			{  	// if the client is logged on read the game rules
				String message = (String) clientInputStream.readObject();
				// store the game rules in variable for output
				mess = "Rules of the game\n" + message;
			}
			else
				// if client not logged on create appropraite message
				mess = "Logon unsuccessful";
			// call method to set up game rules GUI
			setUpRules(loggedOn, mess);
		}
		catch(IOException e) // thrown by methods writeObject, readObject
		{	System.out.println(e);
			System.exit(1);
		}
		catch(ClassNotFoundException e) // thrown by method readObject
		{	System.out.println(e);
			System.exit(1);
		}
	}

	void sendDice()
	{	try
		{	// an object of class Random is required to create random numbers for the dice
   			Random randomNumbers = new Random();
			// get two random numbers between 1 and 6 to represent the dice
			int die1 = 1 + randomNumbers.nextInt(6);
      		int die2 = 1 + randomNumbers.nextInt(6);
      		// set the appropriate images
      		dieImage1.setIcon(dice[die1]);
      		dieImage2.setIcon(dice[die2]);
			// send the dice values to the server
			clientOutputStream.writeObject(die1);
			clientOutputStream.writeObject(die2);
			// read message from server
			String result = (String)clientInputStream.readObject();
			// output message from server
			addOutput(result);

			/* if the last character of the message is '!' this indicates a
			   message to end the game */
			if(result.charAt(result.length() - 1) == '!')
			{	// disable roll button
				rollButton.setEnabled(false);
				// close input and output streams
				closeStreams();
			}
			else
				// add message to text output area
				addOutput("Make a roll.");
		}
		catch(IOException e) // thrown by methods writeObject, readObject
		{	System.out.println(e);
			System.exit(1);
		}
		catch(ClassNotFoundException e) // thrown by method readObject
		{	System.out.println(e);
			System.exit(1);
		}
	}

	void closeStreams()
	{	try
		{	// close input & output streams
			clientInputStream.close();
			clientOutputStream.close();
			// close socket
			socket.close();
		}
		catch(IOException e) // thrown by method close
		{	System.out.println(e);
			System.exit(1);
		}
	}

	public static void main(String args[])
	{	P6_Client gameClient = new P6_Client();
		gameClient.getConnections();
	}

	// this inner class handles all the button events which occur during the game
	private class ButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{	if(e.getSource() == logonButton)
				// if the logon button is clicked call method sendLoginDetails
				sendLoginDetails();
			else
			{	if(e.getSource() == startButton)
					// if the continue button is clicked call method setUpGame
					startGame();
				else
				{	if(e.getSource() == rollButton)
						// if the roll button is clicked call method sendDice
						sendDice();
				}
			}
		}
	}  // end of class ButtonHandler
} // end of class P6_Client
