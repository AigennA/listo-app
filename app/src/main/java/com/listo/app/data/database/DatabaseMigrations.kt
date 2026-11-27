package com.listo.app.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Every schema change that keeps user data. Tests in androidTest run them
 * against the exported schemas in app/schemas.
 */
object DatabaseMigrations {

    /** v2 -> v3 added the optional per-list budget column. */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shopping_lists ADD COLUMN budget REAL")
        }
    }

    /** v3 -> v4 added a scheduled date and a reminder timestamp per list. */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shopping_lists ADD COLUMN scheduledDate INTEGER")
            db.execSQL("ALTER TABLE shopping_lists ADD COLUMN reminderAt INTEGER")
        }
    }

    /** v4 -> v5 added stores and their aisle maps; existing tables are untouched. */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `stores` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                    "`color` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `store_aisles` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storeId` INTEGER NOT NULL, " +
                    "`categoryId` INTEGER NOT NULL, `x` INTEGER NOT NULL, `y` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`storeId`) REFERENCES `stores`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
                    "FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_store_aisles_storeId_categoryId` " +
                    "ON `store_aisles` (`storeId`, `categoryId`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_store_aisles_categoryId` ON `store_aisles` (`categoryId`)"
            )
        }
    }

    /** v5 -> v6 lets a list remember the store it is shopped in. */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shopping_lists ADD COLUMN storeId INTEGER")
        }
    }

    /** v6 -> v7 gives items a numeric amount and a unit; the old quantity text stays as it was. */
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shopping_items ADD COLUMN amount REAL")
            db.execSQL("ALTER TABLE shopping_items ADD COLUMN unit TEXT")
        }
    }

    /** v7 -> v8 gives every store map an entrance and a checkout, at their default cells. */
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE stores ADD COLUMN entranceX INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE stores ADD COLUMN entranceY INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE stores ADD COLUMN checkoutX INTEGER NOT NULL DEFAULT 5")
            db.execSQL("ALTER TABLE stores ADD COLUMN checkoutY INTEGER NOT NULL DEFAULT 8")
        }
    }

    const val LATEST = 8

    val ALL: Array<Migration> =
        arrayOf(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
}
