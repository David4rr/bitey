package com.bitey.app.core.database.di;

import android.content.Context;
import com.bitey.app.core.database.BiteyDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvideBiteyDatabaseFactory implements Factory<BiteyDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideBiteyDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public BiteyDatabase get() {
    return provideBiteyDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideBiteyDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideBiteyDatabaseFactory(contextProvider);
  }

  public static BiteyDatabase provideBiteyDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideBiteyDatabase(context));
  }
}
