package com.shortstack.hackertracker.models

import androidx.room.Embedded
import androidx.room.Relation
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Chris on 4/4/2018.
 */
@Parcelize
data class DatabaseEvent(
        @Embedded
        val event: Event
) : Parcelable {
    @Relation(parentColumn = "type", entityColumn = "type")
    var type: List<Type> = emptyList()
}

