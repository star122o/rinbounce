/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.ncp

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.toRadiansD
import net.minecraft.potion.Potion
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object SNCPBHop : SpeedMode("SNCPBHop") {
    private var level = 1
    private var moveSpeed = 0.2873
    private var lastDist = 0.0
    private var timerDelay = 0
    override fun onEnable() {
        mc.timer.timerSpeed = 1f
        lastDist = 0.0
        moveSpeed = 0.0
        level = 4
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        moveSpeed = baseMoveSpeed
        level = 0
    }

    override fun onMotion() {
        val xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX
        val zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ
        lastDist = sqrt(xDist * xDist + zDist * zDist)
    }


    // TODO: Recode this mess
    override fun onMove(event: MoveEvent) {
        ++timerDelay
        timerDelay %= 5
        if (timerDelay != 0) {
            mc.timer.timerSpeed = 1f
        } else {
            if (mc.thePlayer.isMoving) mc.timer.timerSpeed = 32767f
            if (mc.thePlayer.isMoving) {
                mc.timer.timerSpeed = 1.3f
                mc.thePlayer.motionX *= 1.0199999809265137
                mc.thePlayer.motionZ *= 1.0199999809265137
            }
        }
        if (mc.thePlayer.onGround && mc.thePlayer.isMoving) level = 2
        if (round(mc.thePlayer.posY - mc.thePlayer.posY.toInt().toDouble()) == round(0.138)) {
            mc.thePlayer.motionY -= 0.08
            event.y -= 0.09316090325960147
            mc.thePlayer.posY -= 0.09316090325960147
        }
        if (level == 1 && mc.thePlayer.isMoving) {
            level = 2
            moveSpeed = 1.35 * baseMoveSpeed - 0.01
        } else if (level == 2) {
            level = 3
            mc.thePlayer.motionY = 0.399399995803833
            event.y = 0.399399995803833
            moveSpeed *= 2.149
        } else if (level == 3) {
            level = 4
            val difference = 0.66 * (lastDist - baseMoveSpeed)
            moveSpeed = lastDist - difference
        } else if (level == 88) {
            moveSpeed = baseMoveSpeed
            lastDist = 0.0
            level = 89
        } else if (level == 89) {
            if (mc.theWorld.getCollidingBoundingBoxes(
                    mc.thePlayer,
                    mc.thePlayer.entityBoundingBox.offset(0.0, mc.thePlayer.motionY, 0.0)
                ).isNotEmpty() || mc.thePlayer.isCollidedVertically
            ) level = 1
            lastDist = 0.0
            moveSpeed = baseMoveSpeed
            return
        } else {
            if (mc.theWorld.getCollidingBoundingBoxes(
                    mc.thePlayer,
                    mc.thePlayer.entityBoundingBox.offset(0.0, mc.thePlayer.motionY, 0.0)
                ).isNotEmpty() || mc.thePlayer.isCollidedVertically
            ) {
                moveSpeed = baseMoveSpeed
                lastDist = 0.0
                level = 88
                return
            }
            moveSpeed = lastDist - lastDist / 159.0
        }
        moveSpeed = moveSpeed.coerceAtLeast(baseMoveSpeed)

        val movementInput = mc.thePlayer.movementInput
        var forward = movementInput.moveForward
        var strafe = movementInput.moveStrafe
        var yaw = mc.thePlayer.rotationYaw
        if (forward == 0f && strafe == 0f) {
            event.x = 0.0
            event.z = 0.0
        } else if (forward != 0f) {
            if (strafe >= 1f) {
                yaw += (if (forward > 0f) -45 else 45).toFloat()
                strafe = 0f
            } else if (strafe <= -1f) {
                yaw += (if (forward > 0f) 45 else -45).toFloat()
                strafe = 0f
            }
            if (forward > 0f) {
                forward = 1f
            } else if (forward < 0f) {
                forward = -1f
            }
        }
        val mx2 = cos((yaw + 90f).toRadiansD())
        val mz2 = sin((yaw + 90f).toRadiansD())
        event.x = forward * moveSpeed * mx2 + strafe * moveSpeed * mz2
        event.z = forward * moveSpeed * mz2 - strafe * moveSpeed * mx2
        mc.thePlayer.stepHeight = 0.6f

        if (!mc.thePlayer.isMoving) event.zeroXZ()
    }

    private val baseMoveSpeed: Double
        get() {
            var baseSpeed = 0.2873
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) baseSpeed *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(
                Potion.moveSpeed
            ).amplifier + 1)
            return baseSpeed
        }

    private fun round(value: Double): Double {
        var bigDecimal = BigDecimal(value)
        bigDecimal = bigDecimal.setScale(3, RoundingMode.HALF_UP)
        return bigDecimal.toDouble()
    }
}