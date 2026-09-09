package com.bitey.app.core.database.di;

import com.bitey.app.core.database.BiteyDatabase;
import com.bitey.app.core.database.dao.SavedCollageDao;
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
public final class DatabaseModule_ProvideSavedCollageDaoFactory implements Factory<SavedCollageDao> {
  private final Provider<BiteyDatabase> databaseProvider;

  public DatabaseModule_ProvideSavedCollageDaoFactory(Provider<BiteyDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SavedCollageDao get() {
    return provideSavedCollageDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideSavedCollageDaoFactory create(
      Provider<BiteyDatabase> databaseProvider) {
    return new DatabaseModule_ProvideSavedCollageDaoFactory(databaseProvider);
  }

  public static SavedCollageDao provideSavedCollageDao(BiteyDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSavedCollageDao(database));
  }
}
