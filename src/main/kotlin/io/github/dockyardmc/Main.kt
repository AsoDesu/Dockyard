package io.github.dockyardmc

import io.github.dockyardmc.bounds.Bound
import io.github.dockyardmc.commands.Commands
import io.github.dockyardmc.datagen.EventsDocumentationGenerator
import io.github.dockyardmc.datagen.VerifyPacketIds
import io.github.dockyardmc.events.Events
import io.github.dockyardmc.events.PlayerJoinEvent
import io.github.dockyardmc.events.PlayerLeaveEvent
import io.github.dockyardmc.extentions.broadcastMessage
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.player.GameMode
import io.github.dockyardmc.player.add
import io.github.dockyardmc.registry.Blocks
import io.github.dockyardmc.registry.PotionEffects
import io.github.dockyardmc.utils.DebugScoreboard
import io.github.dockyardmc.world.WorldManager

// This is just testing/development environment.
// To properly use dockyard, visit https://dockyardmc.github.io/Wiki/wiki/quick-start.html

fun main(args: Array<String>) {

    if (args.contains("validate-packets")) {
        VerifyPacketIds()
        return
    }

    if (args.contains("event-documentation")) {
        EventsDocumentationGenerator()
        return
    }

    val server = DockyardServer {
        withIp("0.0.0.0")
        withMaxPlayers(50)
        withPort(25565)
        useMojangAuth(true)
        useDebugMode(false)
        withImplementations {
            dockyardCommands = true
            npcCommand = true
        }
    }

    Events.on<PlayerJoinEvent> {
        val player = it.player

        DockyardServer.broadcastMessage("<yellow>${player} joined the game.")
        player.gameMode.value = GameMode.CREATIVE
        player.permissions.add("dockyard.all")

        DebugScoreboard.sidebar.viewers.add(player)

        player.addPotionEffect(PotionEffects.NIGHT_VISION, 99999, 0, false)
        player.addPotionEffect(PotionEffects.SPEED, 99999, 3, false)
    }

    Events.on<PlayerLeaveEvent> {
        DockyardServer.broadcastMessage("<yellow>${it.player} left the game.")
    }

    Commands.add("/reset") {
        execute {
            val platformSize = 30

            val world = WorldManager.mainWorld

            world.batchBlockUpdate {
                for (x in 0 until platformSize) {
                    for (z in 0 until platformSize) {
                        setBlock(x, 0, z, Blocks.STONE)
                        for (y in 1 until 20) {
                            setBlock(x, y, z, Blocks.AIR)
                        }
                    }
                }
            }
        }
    }

    server.start()
}