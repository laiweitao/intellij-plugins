package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.abc.*;
import com.intellij.flex.uiDesigner.libraries.SwcDependenciesSorter;
import com.intellij.openapi.util.text.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class ComplementSwfBuilder {
  public static void build(String rootPath, String flexVersion) throws IOException {
    final AbcNameFilter sparkInclusionNameFilter = new AbcNameFilterStartsWith("com.intellij.flex.uiDesigner.flex", true);

    final Collection<CharSequence> commonDefinitions = new ArrayList<CharSequence>(1);
    commonDefinitions.add("com.intellij.flex.uiDesigner:SpecialClassForAdobeEngineers");

    final AbcNameFilter air4InclusionNameFilter = new AbcNameFilterByNameSet(SwcDependenciesSorter.OVERLOADED_AIR_SPARK_CLASSES, true);

    // SpriteLoaderAsset must be loaded with framework.swc, because _s000 located in framework.swc
    File source = getSourceFile(rootPath, flexVersion);
    new AbcFilter(null).filter(source, createAbcFile(rootPath, flexVersion),
                               new AbcNameFilterByNameSetAndStartsWith(commonDefinitions, new String[]{"mx.", "spark."}) {
                                 @Override
                                 public boolean accept(CharSequence name) {
                                   return StringUtil.startsWith(name, "com.intellij.flex.uiDesigner.flex:SpriteLoaderAsset") ||
                                          StringUtil.startsWith(name, FlexSdkAbcInjector.STYLE_PROTO_CHAIN) ||
                                          SwcDependenciesSorter.OVERLOADED_MX_CLASSES.contains(name) ||
                                          (super.accept(name) && !sparkInclusionNameFilter.accept(name) &&
                                           !air4InclusionNameFilter.accept(name));
                                 }
                               });
    new AbcFilter(null).filter(source, new File(rootPath + "/complement-flex" + flexVersion + ".swf"), sparkInclusionNameFilter);
    new AbcFilter(null).filter(source, new File(rootPath + "/complement-air4.swf"), air4InclusionNameFilter);
  }

  public static File getSourceFile(String folder, String flexVersion) {
    return new File(folder, "flex-injection-" + flexVersion + "-1.0-SNAPSHOT.swf");
  }

  public static File createAbcFile(String folder, String flexVersion) {
    return new File(folder, generateInjectionName(flexVersion));
  }

  public static String generateInjectionName(String flexSdkVersion) {
    return "flex-injection-" + flexSdkVersion + ".abc";
  }
}