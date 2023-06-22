package com.folioreader.ui.screens.tableOfContent;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "KotlinInternalInJava"
})
public final class TableOfContentViewModel_Factory implements Factory<TableOfContentViewModel> {
  @Override
  public TableOfContentViewModel get() {
    return newInstance();
  }

  public static TableOfContentViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TableOfContentViewModel newInstance() {
    return new TableOfContentViewModel();
  }

  private static final class InstanceHolder {
    private static final TableOfContentViewModel_Factory INSTANCE = new TableOfContentViewModel_Factory();
  }
}
