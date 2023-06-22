package com.folioreader.ui.screens.highlights;

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
public final class HighlightsViewModel_Factory implements Factory<HighlightsViewModel> {
  @Override
  public HighlightsViewModel get() {
    return newInstance();
  }

  public static HighlightsViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static HighlightsViewModel newInstance() {
    return new HighlightsViewModel();
  }

  private static final class InstanceHolder {
    private static final HighlightsViewModel_Factory INSTANCE = new HighlightsViewModel_Factory();
  }
}
