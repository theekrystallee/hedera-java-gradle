package org.example;

import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.Client;
import io.github.cdimascio.dotenv.Dotenv;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.AccountBalance;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.TransferTransaction;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;

import java.util.concurrent.TimeoutException;

public class HederaExamples {
        public static void main(String[] args)
                        throws TimeoutException, PrecheckStatusException, ReceiptStatusException {

                // Grab your Hedera testnet account ID and private key
                AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
                PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY"));

                // Create your connection to the Hedera network
                Client client = Client.forTestnet();

                // Set your account as the client's operator
                client.setOperator(myAccountId, myPrivateKey);

                // Set default max transaction fee & max query payment
                client.setDefaultMaxTransactionFee(new Hbar(100));
                client.setDefaultMaxQueryPayment(new Hbar(50));

                // Generate a new key pair
                PrivateKey newAccountPrivateKey = PrivateKey.generateED25519();
                PublicKey newAccountPublicKey = newAccountPrivateKey.getPublicKey();

                // Create new account and assign the public key
                TransactionResponse newAccount = new AccountCreateTransaction()
                                .setKey(newAccountPublicKey)
                                .setInitialBalance(Hbar.fromTinybars(1000))
                                .execute(client);

                // Get the new account ID
                AccountId newAccountId = newAccount.getReceipt(client).accountId;

                System.out.println("\nNew account ID: " + newAccountId);

                // Check the new account's balance
                AccountBalance accountBalance = new AccountBalanceQuery()
                                .setAccountId(newAccountId)
                                .execute(client);

                System.out.println("New account balance is: " + accountBalance.hbars);

                // Transfer HBAR
                TransactionResponse sendHbar = new TransferTransaction()
                                .addHbarTransfer(myAccountId, Hbar.fromTinybars(-1000))
                                .addHbarTransfer(newAccountId, Hbar.fromTinybars(1000))
                                .execute(client);

                System.out.println("\nThe transfer transaction was: " + sendHbar.getReceipt(client).status);

                // Request the cost of the query
                Hbar queryCost = new AccountBalanceQuery()
                                .setAccountId(newAccountId)
                                .getCost(client);

                System.out.println("\nThe cost of this query: " + queryCost);

                // Check the new account's balance
                AccountBalance accountBalanceNew = new AccountBalanceQuery()
                                .setAccountId(newAccountId)
                                .execute(client);

                System.out.println("The account balance after the transfer: " + accountBalanceNew.hbars + "\n");
        }
}