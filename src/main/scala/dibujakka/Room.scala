package dibujakka

case class Room(id: Int,
                name: String,
                totalRounds: Int,
                maxPlayers: Int,
                language: String,
                currentRound: Int,
                playersCount: Int,
                status: String)
