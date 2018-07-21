import java.io.*;
import java.security.*;
import java.nio.charset.StandardCharsets;

/*
* Nick Bild
* 2018-02-28
* Generate, update, and validate LockBlock blockchains.
*/
public class LockBlock {

	public static void main(String[] args) {
		int status;

		if (args.length < 1) {
			usage();
			return;
		}

		if (args[0].equals("init")) { // Initialize new blockchain.
			status = initBlock(args[1]);

			if (status != 0) {
				System.out.println("Error initializing blockchain!");
			}

		} else if (args[0].equals("add")) { // Add new block to blockchain.
			status = addBlock(args[1], args[2], args[3]);

			if (status != 0) {
				System.out.println("Error adding new block to blockchain!");
			}

		} else if (args[0].equals("validate")) { // Validate blockchain.
			status = validateChain(args[1]);

			if (status != 0) {
				System.out.println("Error validating blockchain!");
			} else {
				System.out.println("Blockchain is valid.");
			}

		} else {
			usage();

		}
	}

	// Display usage information.
	public static void usage() {
		System.out.println("Usage:");
		System.out.println("\tjava LockBlock init <NEW-BLOCKCHAIN-FILE-NAME>");
		System.out.println("\tjava LockBlock add <BLOCKCHAIN-FILE-NAME> <USERNAME> <PUBLIC-RSA-KEY-FILE>");
		System.out.println("\tjava LockBlock validate <BLOCKCHAIN-FILE-NAME>");

	}

	// Initialize a new blockchain.
	public static int initBlock(String fileName) {
		try {
			// Create genesis block.
			String genesis = "========\ngenesis block previous hash\ngenesis block username\ngenesis block public key";

			// Hash entire message.
			String encodedhash = genSHA256(genesis);

			// Write blockchain to file.
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(genesis + "\n" + encodedhash + "\n");
			writer.close();

		} catch (IOException ioe) {
			System.out.println("Error writing to " + fileName + ": " + ioe);
			return 1;

		}

		return 0;

	}

	// Add block to existing blockchain.
	public static int addBlock(String fileName, String userName, String publicKey) {
		String previousHash = "";
		String keyStr = "";

		// Retrieve hash of previous block.
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String line;

			while ((line = in.readLine()) != null) {
				previousHash = line;
			}

		} catch (Exception e) {
			System.out.println("Error reading previous block's hash: " + e);
			return 1;

		}

		// Get public key string (RSA key expected).
		try {
			String line = "";

			BufferedReader in = new BufferedReader(new FileReader(publicKey));
			while ((line = in.readLine()) != null) {
                                line = line.replace("\n", "").replace("\r", ""); // Remove newline characters.

				// Skip the beginning/end delimiters.
				if (line.startsWith("-----BEGIN") || line.startsWith("-----END")) {
					continue;
				}

				keyStr += line;
			}

		} catch (Exception e) {
			System.out.println("Error reading public key: " + e);
			return 1;

		}

		String block = "========\n" + previousHash + "\n" + userName + "\n" + keyStr;

		// Hash entire message.
		String encodedhash = genSHA256(block);

		block = block + "\n" + encodedhash + "\n";

		// Add new block to existing blockchain.
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
			writer.append(block);
			writer.close();

		} catch (IOException ioe) {
                        System.out.println("Error writing to " + fileName + ": " + ioe);
                        return 1;

                }

		return 0;		

	}

	// Validate blockchain.
	public static int validateChain(String fileName) {
		try {
			int genesis = 1;
			String line = "";
			String previousBlockHash = "";
                        BufferedReader in = new BufferedReader(new FileReader(fileName));

			// Read in blockchain file.
			while ((line = in.readLine()) != null) {
				line = line.replace("\n", "").replace("\r", ""); // Remove newline characters.

				String prevHash, userName, publicKey, hash = "";

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

					// Does hash match data?
					String assembled = "========\n" + prevHash + "\n" + userName + "\n" + publicKey;
					String rehash = genSHA256(assembled);

					if (!hash.equals(rehash)) {
						System.out.println("Validation error! Hash value:\n" + hash + "\ndoes not match recomputed hash for this data:\n" + rehash);
						return 1;

					}

					// If it's not the gensis block, make sure that the previous hash record
					// matches the actual hash of the previous record.
					if (genesis == 0) {
						if (!previousBlockHash.equals(prevHash)) {
							System.out.println("Validation error! The previous block hash:\n" + prevHash + "\ndoes not match the actual hash of the previous block:\n" + previousBlockHash);
							return 1;

						}
					}

					previousBlockHash = hash; // Remember this hash for when we look at the next block.
				}

				genesis = 0; // A record has been processed, so we are no longer looking at the genesis block.

			}

                } catch (Exception e) {
                        System.out.println("Error reading public key: " + e);
                        return 1;

                }

		return 0;
	}

	// Generate SHA256 hash of string.
	public static String genSHA256(String input) {
		String encodedhash = "";

		try {
			// Hash message.
	                MessageDigest mdigest = MessageDigest.getInstance("SHA-256");
	                mdigest.update(input.getBytes());

	                // Convert hash to hex.
	                byte byteData[] = mdigest.digest();
	                StringBuffer sb = new StringBuffer();
	                for (int i=0; i<byteData.length; i++) {
	                        sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	                }
	                encodedhash = sb.toString();

		} catch (NoSuchAlgorithmException nae) {
			System.out.println("Error generating hash: " + nae);

		}

		return encodedhash;

	}

}

