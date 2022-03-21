package jan.dhan.darshak.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jan.dhan.darshak.data.NearbyPointsApi
import jan.dhan.darshak.data.LocationDatabase
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun providesRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    @Provides
    fun providesNearbyPoints(retrofit: Retrofit): NearbyPointsApi {
        return retrofit.create(NearbyPointsApi::class.java)
    }

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): LocationDatabase {
        return Room.databaseBuilder(
            context,
            LocationDatabase::class.java,
            "location_database"
        ).build()
    }

    @Singleton
    @Provides
    fun provideLocationDao(db: LocationDatabase) = db.locationDao()
}