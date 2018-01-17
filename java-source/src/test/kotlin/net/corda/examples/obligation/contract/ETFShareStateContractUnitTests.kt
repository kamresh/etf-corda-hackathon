package com.cts.corda.finblockers.contract

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import com.cts.corda.finblockers.ETFShareState
import net.corda.finance.DOLLARS
import net.corda.finance.POUNDS
import net.corda.testing.*
import org.junit.After
import org.junit.Before

/**
 * A base class to reduce the boilerplate when writing obligation contract tests.
 */
abstract class ETFShareStateContractUnitTests {
    @Before
    fun setup() {
        setCordappPackages("com.cts.corda.finblockers", "net.corda.testing.contracts")
    }

    @After
    fun tearDown() {
        unsetCordappPackages()
    }

    protected class DummyState : ContractState {
        override val participants: List<AbstractParty> get() = listOf()
    }

    protected class DummyCommand : CommandData

    protected val oneDollarObligation = ETFShareState(1.POUNDS, ALICE, BOB)
    protected val tenDollarObligation = ETFShareState(10.DOLLARS, ALICE, BOB)
    protected val tenDollarObligationWithNewLender = ETFShareState(10.DOLLARS, CHARLIE, BOB)
    protected val tenDollarObligationWithNewBorrower = ETFShareState(10.DOLLARS, ALICE, ALICE)
    protected val tenDollarObligationWithNewAmount = ETFShareState(0.DOLLARS, ALICE, BOB)
}
