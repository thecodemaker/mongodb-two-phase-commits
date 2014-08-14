
**Resource:** http://docs.mongodb.org/manual/tutorial/perform-two-phase-commits/

Consider a scenario where you want to transfer funds from account A to account B. In a relational database system, you can subtract the funds from A and add the funds to B in a single multi-statement transaction. In MongoDB, you can emulate a two-phase commit to achieve a comparable result.

The examples in this tutorial use the following two collections:

- A collection named accounts to store account information.
- A collection named transactions to store information on the fund transfer transactions.