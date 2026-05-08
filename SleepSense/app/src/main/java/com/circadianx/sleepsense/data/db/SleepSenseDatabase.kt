package com.circadianx.sleepsense.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.circadianx.sleepsense.data.local.db.dao.AppBlockOverrideDao
import com.circadianx.sleepsense.data.local.db.dao.ChallengeDao
import com.circadianx.sleepsense.data.local.db.dao.SocialDao
import com.circadianx.sleepsense.data.local.db.dao.ProgressPhotoDao
import com.circadianx.sleepsense.data.local.db.dao.RoutineDao
import com.circadianx.sleepsense.data.local.db.dao.NightDisturbanceDao
import com.circadianx.sleepsense.data.local.db.dao.SleepRecordDao
import com.circadianx.sleepsense.data.local.db.dao.StepDao
import com.circadianx.sleepsense.data.local.db.entity.AppBlockOverrideEntity
import com.circadianx.sleepsense.data.local.db.entity.ChallengeCheckInEntity
import com.circadianx.sleepsense.data.local.db.entity.ChallengeEntity
import com.circadianx.sleepsense.data.local.db.entity.ChallengeRatingEntity
import com.circadianx.sleepsense.data.local.db.entity.GroupChallengeEntity
import com.circadianx.sleepsense.data.local.db.entity.GroupMemberEntity
import com.circadianx.sleepsense.data.local.db.entity.NightDisturbanceEntity
import com.circadianx.sleepsense.data.local.db.entity.ProgressPhotoEntity
import com.circadianx.sleepsense.data.local.db.entity.RoutineCompletionEntity
import com.circadianx.sleepsense.data.local.db.entity.RoutineItemEntity
import com.circadianx.sleepsense.data.local.db.entity.SleepRecordEntity
import com.circadianx.sleepsense.data.local.db.entity.StoryEntity
import com.circadianx.sleepsense.data.local.db.entity.StepDayEntity
import com.circadianx.sleepsense.data.model.ApneaEvent
import com.circadianx.sleepsense.data.model.SleepSession

@Database(
    entities = [
        SleepSession::class,
        ApneaEvent::class,
        SleepRecordEntity::class,
        NightDisturbanceEntity::class,
        RoutineItemEntity::class,
        RoutineCompletionEntity::class,
        StepDayEntity::class,
        AppBlockOverrideEntity::class,
        ChallengeEntity::class,
        ChallengeCheckInEntity::class,
        ChallengeRatingEntity::class,
        ProgressPhotoEntity::class,
        GroupChallengeEntity::class,
        GroupMemberEntity::class,
        StoryEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class SleepSenseDatabase : RoomDatabase() {
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun apneaEventDao(): ApneaEventDao
    abstract fun sleepRecordDao(): SleepRecordDao
    abstract fun nightDisturbanceDao(): NightDisturbanceDao
    abstract fun routineDao(): RoutineDao
    abstract fun stepDao(): StepDao
    abstract fun appBlockOverrideDao(): AppBlockOverrideDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
    abstract fun socialDao(): SocialDao
}
