package com.bitey.app.core.database.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PlateEntryWithTags(
    @Embedded
    val entry: PlateEntryEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "tagId",
        associateBy = Junction(
            value = EntryTagCrossRef::class,
            parentColumn = "entryId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)
