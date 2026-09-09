package com.bitey.app.core.database.di;

import com.bitey.app.core.database.BiteyDatabase;
import com.bitey.app.core.database.dao.PlateEntryDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class DatabaseModule_ProvidePlateEntryDaoFactory implements Factory<PlateEntryDao> {
  private final Provider<BiteyDatabase> databaseProvider;

  public DatabaseModule_ProvidePlateEntryDaoFactory(Provider<BiteyDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public PlateEntryDao get() {
    return providePlateEntryDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvidePlateEntryDaoFactory create(
      Provider<BiteyDatabase> databaseProvider) {
    return new DatabaseModule_ProvidePlateEntryDaoFactory(databaseProvider);
  }

  public static PlateEntryDao providePlateEntryDao(BiteyDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePlateEntryDao(database));
  }
}
