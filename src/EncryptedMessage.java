// student name: Declan straney
// student number: 40047482

import java.io.Serializable;

public class EncryptedMessage implements Serializable
{   // this instance variable will store the original, encrypted and decrypted message
	private String message;
	
	// this variable stores the key 
	static String KEY = "cable";

    public EncryptedMessage(String message)
    {	// begin by coding this method first - initialise instance variable message with the original message
		this.message = message;
    }

    public String getMessage()
    {	// code this method next - return the value of instance variable message either encrypted or decrypted 
		return message;
    }

    public void encrypt()
    {	/* read through section 3 of the practical 6 document 
		   to get you started */
		/* Hint: the alphabet is required and have a look at the algorithm in the document 
		 * after encryption what form will the original message take plaintext or ciphertext */
    	String charSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ";
    			String cipherTxt = "";
    
    			for(int i = 0; i < message.length(); i++)
    			{ // find position of plaintext char in character set
    				int plainTxtChar = charSet.indexOf(message.charAt(i));
    				
    				// next key character position
    				int keyPos = i % KEY.length();
    				// find its position in character set
    				int keyChar = charSet.indexOf(KEY.charAt(keyPos));
    				
    				/* add key character to plaintext char to shift plaintext char then divide
    				 * by length of char reference set and get remainder to wrap around
    				 */
    				int chipherTxtChar = (plainTxtChar + keyChar) % charSet.length();
    				// get char at corresponding position in char reference set and add to cipherText
    				char c = charSet.charAt(chipherTxtChar);
    				cipherTxt += c;
    			}
    			message = cipherTxt;
    }

    public void decrypt()
    {	/* read through section 3 of the practical 6 document 
		   to get you started */
		/* Hint: the alphabet is required and have a look at the algorithm in the document 
		 * after decryption what form will the original message take plaintext or ciphertext */   
    	String charSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ";
    	String plainTxt = "";
    	
    	for(int i = 0; i < message.length(); i++)
    	{
    		// find position of ciphertext char
    		int cipherTxtChar = charSet.indexOf(message.charAt(i));
    		
    		// get position of next char
    		int keyPos = i % KEY.length();
    		// find pos in char set
    		int keyChar = charSet.indexOf(KEY.charAt(keyPos));
    		
    		/* subtract original shift from char referene set length to get new shift,
    		 * add shift to ciphertext char then divide by char reference set length and get
    		 * remainder to wrap around
    		 */
    		int plainTxtChar = (cipherTxtChar + (charSet.length() - keyChar)) % charSet.length();
    		
    		// get char at corresponding position in char ref set and add to plaintect
    		char c = charSet.charAt(plainTxtChar);
    		plainTxt += c;
    	}
    	message = plainTxt;
    }
}
