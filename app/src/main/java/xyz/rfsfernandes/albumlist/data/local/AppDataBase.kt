package xyz.rfsfernandes.albumlist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import xyz.rfsfernandes.albumlist.data.local.entities.AlbumEntity

@Database(
    entities = [
        AlbumEntity::class,
    ], version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract val leBonCoinDAO: LeBonCoinDAO
}
