import java.io.*;
import java.lang.Runtime;
import java.util.Random;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;

import org.sqlite.JDBC;

/*
* Nick Bild
* 2018-02-28
* Authenticate user with LockBlock blockchain.
*/
public class LockBlockAuth {
	// Open database connection.
	public static Connection conn = connectDB();

	public static void main(String[] args) {
		// Call appropriate method for user request.
		if (args[0].equals("validate1")) {
			String challenge = userChallengeStep1(args[1], args[2]);
			System.out.println("Challenge string:\n" + challenge);

		} else if (args[0].equals("validate2")) {
			int result = userChallengeStep2(args[1], args[2]);
			System.out.println(result);

		} else {
			usage();

		}

	}

	// Display usage information.
	public static void usage() {
		System.out.println("Usage:");
		System.out.println("\tjava LockBlockAuth validate1 <BLOCKCHAIN-FILE-NAME> <USERNAME>");
		System.out.println("\tjava LockBlockAuth validate2 <USERNAME> <ONE-TIME-PASSWORD>");
	}

	// SQLite connection creation.
	private static Connection connectDB() {
		String url = "jdbc:sqlite:lockblock.db";
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(url);

		} catch (Exception e) {
			System.out.println(e.getMessage());

		}

		return conn;
	}

	// Initiate a user challenge.
	public static String userChallengeStep1(String blockChain, String username) {
		// Get random alphanumeric string.
		String rand = getRandomAlphaNum(10);

		// Get the encrypted output.
		String encrypted = "";
		try {
			// Write user's public key (from blockchain) to a separate key file.
			// TODO: Work with public key in memory, do not write to flat file.
			String pubkey = getPublicKey(username, blockChain);
			BufferedWriter writer = new BufferedWriter(new FileWriter(username + "_pub.pem"));
			writer.write("-----BEGIN PUBLIC KEY-----\n");

			for (int i=0; i<pubkey.length(); i++) {
                        	writer.write(pubkey.charAt(i));
				if ((i+1) % 64 == 0) { // Every 64 characters, a newline is needed.
					writer.write("\n");
				}
			}

			writer.write("\n-----END PUBLIC KEY-----\n");
                        writer.close();

			// Encrypt the random string with the user's public key.
			// TODO: The encryption should really be done with a Java native solution so that the software isn't
			// dependent on openssl, etc. binaries or a Linux shell being present.
			String[] cmd = {
				"/bin/sh",
				"-c",
				"echo -n '" + rand + "' | openssl rsautl -encrypt -pubin -inkey " + username + "_pub.pem | base64"
			};
			Process pr = Runtime.getRuntime().exec(cmd);

			// Get the encrypted output.
			BufferedReader output = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = null;
			while ((line = output.readLine()) != null) {
				line = line.replace("\n", "").replace("\r", ""); // Remove newline characters.
				encrypted += line;
			}

		} catch (Exception e) {
			System.out.println("Error initiating user challenge: " + e);
			return null;

		}

		// Save username / random string association.
		try {
			// Remove any existing entries for this user.
			String sql = "DELETE FROM challenge WHERE username='" + username + "';";
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql);

			// Add new challenge string for this user.
			sql = "INSERT INTO challenge (username, challenge) VALUES ('" + username + "', '" + rand + "');";
             		stmt = conn.createStatement();
             		stmt.executeUpdate(sql);

		} catch (Exception e) {
			System.out.println("Error recording user/random string pair: " + e);
			return null;

		}

		return encrypted;
	}

	// Step 2 of validation.
	public static int userChallengeStep2(String username, String otp) {
		int status = 1;
		String challenge = "";

		try {
			// Get stored challenge string.
			String sql = "SELECT challenge FROM challenge WHERE username='" + username + "';";
                        Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			challenge = rs.getString("challenge");

			// Delete challenge string from database (one time password).
			sql = "DELETE FROM challenge WHERE username='" + username + "';";
                        stmt = conn.createStatement();
                        stmt.executeUpdate(sql);

		} catch (Exception e) {
			System.out.println("Error validating user: " + e);
			return 1;

		}

		// If challenge not empty string and challenge == otp...
		if (!challenge.equals("") && challenge.equals(otp)) {
			status = 0; // Success.
		}

		return status;
	}

	// Random alphanumeric string generator.
	public static String getRandomAlphaNum(int size) {
        	String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        	StringBuilder charsSB = new StringBuilder();
        	Random rnd = new Random();

        	while (charsSB.length() < size) {
        		int index = (int) (rnd.nextFloat() * chars.length());
            		charsSB.append(chars.charAt(index));
        	}

        	String charsStr = charsSB.toString();
        	return charsStr;

	}

	// Retrieve a user's public key from blockchain.
	public static String getPublicKey(String user, String fileName) {
		String pub = "";

		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));

                        // Read in blockchain file.
			String line = null;
                        while ((line = in.readLine()) != null) {
                                line = line.replace("\n", "").replace("\r", ""); // Remove newline characters.

                                String prevHash, userName, publicKey, hash;
				prevHash = userName = publicKey = hash = "";

                                // This is the start of a new block.
                                // Get data for entire block.
                                if (line.equals("========")) {
                                        line = in.readLine();
                                        prevHash = line.replace("\n", "").replace("\r", "");

                                        line = in.readLine();
                                        userName = line.replace("\n", "").replace("\r", "");

                                        line = in.readLine();
                                        publicKey = line.replace("\n", "").replace("\r", "");

                                        line = in.readLine();
                                        hash = line.replace("\n", "").replace("\r", "");

                                }

				if (user.equals(userName)) {
					pub = publicKey;
					break;
				}
                        }

		} catch (Exception e) {
			System.out.println("Error retrieving public key: " + e);

		}

		return pub;

	}

}

