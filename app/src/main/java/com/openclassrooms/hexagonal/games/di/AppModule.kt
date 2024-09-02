package com.openclassrooms.hexagonal.games.di

import android.content.Context
import com.openclassrooms.hexagonal.games.data.repository.InjectedContext
import com.openclassrooms.hexagonal.games.data.service.PostApi
import com.openclassrooms.hexagonal.games.data.service.PostFireStoreAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * This class acts as a Dagger Hilt module, responsible for providing dependencies to other parts of the application.
 * It's installed in the SingletonComponent, ensuring that dependencies provided by this module are created only once
 * and remain available throughout the application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {
  /**
   * Provides a Singleton instance of PostApi using a PostFakeApi implementation for testing purposes.
   * This means that whenever a dependency on PostApi is requested, the same instance of PostFakeApi will be used
   * throughout the application, ensuring consistent data for testing scenarios.
   *
   * @return A Singleton instance of PostFakeApi.
   */
  @Provides
  @Singleton
  fun providePostApi(): PostApi {
    //return PostFakeApi()
    return PostFireStoreAPI() // Branchement dans l'appli de la base de données FireStore
  }


  @Provides
  @Singleton
  fun provideConnectivityChecker(@ApplicationContext context: Context): InjectedContext {
    return InjectedContext(context) // Branchement dans l'appli de la base de données FireStore
  }

}
