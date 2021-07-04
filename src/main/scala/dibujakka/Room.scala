package dibujakka

case class Room(id: String,
                name: String,
                totalRounds: Int,
                maxPlayers: Int,
                language: String,
                currentRound: Int,
                playersCount: Int,
                status: String,
                currentWord: String)
