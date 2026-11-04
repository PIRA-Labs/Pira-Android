package com.taak.watch.signedtest

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OneTimeGrantStoreTest {
    private val proposal = ActionProposal(Capability.TEXT_TO_SPEECH, parameters = mapOf("text" to "Hello"))
    @Test fun grantCanOnlyBeClaimedOnce() {
        val store = InMemoryGrantStore(); val issued = store.issue(proposal, nowMs = 10)
        assertNotNull(store.claim(issued.nonce, nowMs = 11)); assertNull(store.claim(issued.nonce, nowMs = 12))
    }
    @Test fun expiredGrantCannotBeClaimed() {
        val store = InMemoryGrantStore(ttlMs = 5); val issued = store.issue(proposal, nowMs = 10)
        assertNull(store.claim(issued.nonce, nowMs = 15))
    }
    @Test fun coordinatorCannotAuthorizeTheSameGrantTwice() {
        val store = InMemoryGrantStore(); val coordinator = TtsActionCoordinator(PolicyEngine(), store)
        val action = TtsProposal("Hello"); val grant = coordinator.request(action)
        kotlin.test.assertEquals(true, coordinator.authorizeAndConsume(action, grant.nonce).allowed)
        kotlin.test.assertEquals(false, coordinator.authorizeAndConsume(action, grant.nonce).allowed)
    }
}
