import java.io.*;
import java.lang.Runtime;

/*
* Nick Bild
* 2018-03-01
* User server challenge response.
*/
public class LockBlockLogin {

	public static void main(String[] args) {
		// Call appropriate method for user request.
		if (args[0].equals("password")) {
			String response = userResponse(args[1], args[2]);
			System.out.println("Response string:\n" + response);

		} else {
			usage();

		}

	}

	// Display usage information.
	public static void usage() {
		System.out.println("Usage:");
		System.out.println("\tjava LockBlockLogin password <PRIVATE-KEY-FILE-NAME> <CHALLENGE-TEXT>");
	}

	// Initiate a user challenge.
	public static String userResponse(String privateKey, String challenge) {
		// Get the one time password.

		String otp = "";
		try {
			// Decrypt the challenge string with the user's private key.
			// TODO: The decryption should really be done with a Java native solution so that the software isn't
			// dependent on openssl, etc. binaries or a Linux shell being present.
			String[] cmd = {
				"/bin/sh",
				"-c",
				"echo -n '" + challenge + "' | base64 -d | openssl rsautl -decrypt -inkey " + privateKey
			};
			Process pr = Runtime.getRuntime().exec(cmd);

			// Get the encrypted output.
			BufferedReader output = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = null;
			while ((line = output.readLine()) != null) {
				line = line.replace("\n", "").replace("\r", ""); // Remove newline characters.
				otp += line;
			}

		} catch (Exception e) {
			System.out.println("Error determining OTP: " + e);
			return null;

		}

		return otp;
	}

}

