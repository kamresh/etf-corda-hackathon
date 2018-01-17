package com.cts.corda.finblockers.contract

import com.cts.corda.finblockers.ETFShareState
import com.cts.corda.finblockers.ETFCreationContract
import com.cts.corda.finblockers.ETFCreationContract.OBLIGATION_CONTRACT_ID
import net.corda.finance.DOLLARS
import net.corda.finance.POUNDS
import net.corda.finance.SWISS_FRANCS
import net.corda.testing.*
import org.junit.Test

class ETFShareStateContractIssueTests : ETFShareStateContractUnitTests() {

    @Test
    fun `issue obligation transaction must have no inputs`() {
        ledger {
            transaction {
                input(OBLIGATION_CONTRACT_ID) { DummyState() }
                command(ALICE_PUBKEY, BOB_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { oneDollarObligation }
                this `fails with` "No inputs should be consumed when issuing an obligation."
            }
            transaction {
                output(OBLIGATION_CONTRACT_ID) { oneDollarObligation }
                command(ALICE_PUBKEY, BOB_PUBKEY) { ETFCreationContract.Commands.Issue() }
                this.verifies() // As there are no input states.
            }
        }
    }

    @Test
    fun `Issue transaction must have only one output obligation`() {
        ledger {
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { oneDollarObligation } // Two outputs fails.
                output(OBLIGATION_CONTRACT_ID) { oneDollarObligation }
                this `fails with` "Only one obligation state should be created when issuing an obligation."
            }
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { oneDollarObligation } // One output passes.
                this.verifies()
            }
        }
    }

    @Test
    fun `cannot issue zero value obligations`() {
        ledger {
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { ETFShareState(0.POUNDS, ALICE, BOB) } // Zero amount fails.
                this `fails with` "A newly issued obligation must have a positive amount."
            }
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { ETFShareState(100.SWISS_FRANCS, ALICE, BOB) }
                this.verifies()
            }
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { ETFShareState(1.POUNDS, ALICE, BOB) }
                this.verifies()
            }
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { ETFShareState(10.DOLLARS, ALICE, BOB) }
                this.verifies()
            }
        }
    }

    @Test
    fun `lender and borrower must sign issue obligation transaction`() {
        ledger {
            transaction {
                command(DUMMY_KEY_1.public) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { oneDollarObligation }
                this `fails with` "Both lender and borrower together only may sign obligation issue transaction."
            }
            transaction {
                command(ALICE_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { oneDollarObligation }
                this `fails with` "Both lender and borrower together only may sign obligation issue transaction."
            }
            transaction {
                command(BOB_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { oneDollarObligation }
                this `fails with` "Both lender and borrower together only may sign obligation issue transaction."
            }
            transaction {
                command(BOB_PUBKEY, BOB_PUBKEY, BOB_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { oneDollarObligation }
                this `fails with` "Both lender and borrower together only may sign obligation issue transaction."
            }
            transaction {
                command(BOB_PUBKEY, BOB_PUBKEY, MINI_CORP_PUBKEY, ALICE_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { oneDollarObligation }
                this `fails with` "Both lender and borrower together only may sign obligation issue transaction."
            }
            transaction {
                command(BOB_PUBKEY, BOB_PUBKEY, BOB_PUBKEY, ALICE_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { oneDollarObligation }
                this.verifies()
            }
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { oneDollarObligation }
                this.verifies()
            }
        }
    }

    @Test
    fun `lender and borrower cannot be the same`() {
        val borrowerIsLenderObligation = ETFShareState(10.POUNDS, ALICE, ALICE)
        ledger {
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { borrowerIsLenderObligation }
                this `fails with` "The lender and borrower cannot be the same identity."
            }
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { ETFCreationContract.Commands.Issue() }
                output(OBLIGATION_CONTRACT_ID) { oneDollarObligation }
                this.verifies()
            }
        }
    }
}