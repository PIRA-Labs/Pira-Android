package com.taak.watch.signedtest

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PolicyEngineTest {
    private val engine = PolicyEngine()
    @Test fun missingGrantIsDenied() = assertFalse(engine.evaluate(ActionProposal(Capability.CAMERA_LOOK), null).allowed)
    @Test fun expiredGrantIsDenied() = assertFalse(engine.evaluate(ActionProposal(Capability.CAMERA_LOOK), Authorization(Capability.CAMERA_LOOK, 1, "n"), 2).allowed)
    @Test fun reusedGrantIsDenied() = assertFalse(engine.evaluate(ActionProposal(Capability.CAMERA_LOOK), Authorization(Capability.CAMERA_LOOK, 100, "n", used = true), 2).allowed)
    @Test fun mismatchedScopeIsDenied() = assertFalse(engine.evaluate(ActionProposal(Capability.BATTERY_READ), Authorization(Capability.CAMERA_LOOK, 100, "n"), 2).allowed)
    @Test fun validGrantIsAllowed() = assertTrue(engine.evaluate(ActionProposal(Capability.CAMERA_LOOK), Authorization(Capability.CAMERA_LOOK, 100, "n"), 2).allowed)
    @Test fun modifiedParametersAreDenied() = assertFalse(engine.evaluate(ActionProposal(Capability.CAMERA_LOOK, parameters = mapOf("prompt" to "changed")), Authorization(Capability.CAMERA_LOOK, 100, "n", mapOf("prompt" to "original")), 2).allowed)
    @Test fun emergencyStopIsIndependentOfGrant() = assertFalse(PolicyEngine { true }.evaluate(ActionProposal(Capability.CAMERA_LOOK), Authorization(Capability.CAMERA_LOOK, 100, "n"), 2).allowed)
}
