package com.cts.corda.finblockers.flows

import com.cts.corda.finblockers.ETFShareState
import net.corda.finance.POUNDS
import net.corda.testing.chooseIdentity
import org.jgroups.util.Util.assertEquals

class TransferETFShareTests : ETFShareStateTests() {

    @org.junit.Test
    fun `Transfer normal non-anonymous obligation successfully`() {
        // Issue obligation.
        val issuanceTransaction = issueObligation(a, b, 1000.POUNDS, anonymous = false)
        network.waitQuiescent()
        val issuedObligation = issuanceTransaction.tx.outputStates.first() as ETFShareState

        // Transfer obligation.
        val transferTransaction = transferObligation(issuedObligation.linearId, b, c, anonymous = false)
        network.waitQuiescent()
        val transferredObligation = transferTransaction.tx.outputStates.first() as ETFShareState

        // Check the issued obligation with the new lender is the transferred obligation
        assertEquals(issuedObligation.withNewLender(c.info.chooseIdentity()), transferredObligation)

        // Check everyone has the transfer transaction.
        val aObligation = a.services.loadState(transferTransaction.tx.outRef<ETFShareState>(0).ref).data as ETFShareState
        val bObligation = b.services.loadState(transferTransaction.tx.outRef<ETFShareState>(0).ref).data as ETFShareState
        val cObligation = c.services.loadState(transferTransaction.tx.outRef<ETFShareState>(0).ref).data as ETFShareState
        assertEquals(aObligation, bObligation)
        assertEquals(bObligation, cObligation)
    }

    @org.junit.Test
    fun `Transfer anonymous obligation successfully`() {
        // Issue obligation.
        val issuanceTransaction = issueObligation(a, b, 1000.POUNDS, anonymous = true)
        network.waitQuiescent()
        val issuedObligation = issuanceTransaction.tx.outputStates.first() as ETFShareState

        // Transfer obligation.
        val transferTransaction = transferObligation(issuedObligation.linearId, b, c, anonymous = true)
        network.waitQuiescent()
        val transferredObligation = transferTransaction.tx.outputStates.first() as ETFShareState

        // Check the issued obligation with the new lender is the transferred obligation.
        assertEquals(issuedObligation.withNewLender(transferredObligation.lender), transferredObligation)

        // Check everyone has the transfer transaction.
        val aObligation = a.services.loadState(transferTransaction.tx.outRef<ETFShareState>(0).ref).data as ETFShareState
        val bObligation = b.services.loadState(transferTransaction.tx.outRef<ETFShareState>(0).ref).data as ETFShareState
        val cObligation = c.services.loadState(transferTransaction.tx.outRef<ETFShareState>(0).ref).data as ETFShareState
        assertEquals(aObligation, bObligation)
        assertEquals(bObligation, cObligation)

    }

    @org.junit.Test
    fun `Transfer flow can only be started by lender`() {
        // Issue obligation.
        val issuanceTransaction = issueObligation(a, b, 1000.POUNDS, anonymous = false)
        network.waitQuiescent()
        val issuedObligation = issuanceTransaction.tx.outputStates.first() as ETFShareState

        // Transfer obligation.
        kotlin.test.assertFailsWith<IllegalStateException> {
            transferObligation(issuedObligation.linearId, a, c, anonymous = false)
        }
    }

}
